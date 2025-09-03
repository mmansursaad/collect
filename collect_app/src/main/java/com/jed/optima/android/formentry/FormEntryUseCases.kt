package com.jed.optima.android.formentry

import org.apache.commons.io.FileUtils.readFileToByteArray
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.FormInitializationMode
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.core.model.instance.utils.DefaultAnswerResolver
import org.javarosa.core.reference.ReferenceManager
import org.javarosa.form.api.FormEntryController
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.util.XFormUtils
import com.jed.optima.android.instancemanagement.isEdit
import com.jed.optima.android.javarosawrapper.FailedValidationResult
import com.jed.optima.android.javarosawrapper.FormController
import com.jed.optima.entities.LocalEntityUseCases
import com.jed.optima.entities.storage.EntitiesRepository
import java.io.File

object FormEntryUseCases {

    fun loadFormDef(
        instance: com.jed.optima.forms.instances.Instance,
        formsRepository: com.jed.optima.forms.FormsRepository,
        projectRootDir: File,
        formDefCache: FormDefCache
    ): Pair<FormDef, com.jed.optima.forms.Form>? {
        val form =
            formsRepository.getAllByFormIdAndVersion(instance.formId, instance.formVersion)
                .firstOrNull()
        return if (form == null) {
            null
        } else {
            val formDef = loadFormDef(form, projectRootDir, formDefCache)
            return if (formDef == null) {
                null
            } else {
                Pair(formDef, form)
            }
        }
    }

    fun loadFormDef(
        form: com.jed.optima.forms.Form,
        projectRootDir: File,
        formDefCache: FormDefCache
    ): FormDef? {
        val xForm = File(form.formFilePath)
        if (!xForm.exists()) {
            return null
        }
        val formMediaDir = File(form.formMediaPath)

        com.jed.optima.android.utilities.FormUtils.setupReferenceManagerForForm(
            ReferenceManager.instance(),
            projectRootDir,
            formMediaDir
        )

        return createFormDefFromCacheOrXml(xForm, formDefCache)!!
    }

    fun loadBlankForm(
        form: com.jed.optima.forms.Form,
        formEntryController: FormEntryController,
        instanceFile: File
    ): FormController {
        formEntryController.model.form.initialize(FormInitializationMode.NEW_FORM)

        return com.jed.optima.android.javarosawrapper.JavaRosaFormController(
            File(form.formMediaPath),
            formEntryController,
            instanceFile
        )
    }

    @JvmStatic
    fun loadDraft(
        form: com.jed.optima.forms.Form,
        instance: com.jed.optima.forms.instances.Instance,
        formEntryController: FormEntryController
    ): FormController? {
        val instanceFile = File(instance.instanceFilePath)
        if (!instanceFile.exists()) {
            return null
        }

        importInstance(instanceFile, formEntryController)
        formEntryController.model.form.initialize(FormInitializationMode.DRAFT_FORM_EDIT)

        return com.jed.optima.android.javarosawrapper.JavaRosaFormController(
            File(form.formMediaPath),
            formEntryController,
            instanceFile
        )
    }

    fun saveDraft(
        form: com.jed.optima.forms.Form,
        formController: FormController,
        instancesRepository: com.jed.optima.forms.instances.InstancesRepository
    ): com.jed.optima.forms.instances.Instance {
        saveInstanceToDisk(formController)
        return instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .formId(form.formId)
                .formVersion(form.version)
                .instanceFilePath(formController.getInstanceFile()!!.absolutePath)
                .status(com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE)
                .build()
        )
    }

    @JvmStatic
    fun finalizeDraft(
        formController: FormController,
        instancesRepository: com.jed.optima.forms.instances.InstancesRepository,
        entitiesRepository: EntitiesRepository
    ): com.jed.optima.forms.instances.Instance? {
        val instance =
            getInstanceFromFormController(formController, instancesRepository)!!

        val validationResult = formController.validateAnswers(false)
        val valid = validationResult !is FailedValidationResult

        return if (valid) {
            val newInstance = finalizeFormController(instance, formController, instancesRepository, entitiesRepository)
            saveInstanceToDisk(formController)
            newInstance
        } else {
            instancesRepository.save(
                com.jed.optima.forms.instances.Instance.Builder(instance)
                    .status(com.jed.optima.forms.instances.Instance.STATUS_INVALID)
                    .build()
            )

            null
        }
    }

    @JvmStatic
    fun finalizeFormController(
        instance: com.jed.optima.forms.instances.Instance,
        formController: FormController,
        instancesRepository: com.jed.optima.forms.instances.InstancesRepository,
        entitiesRepository: EntitiesRepository,
    ): com.jed.optima.forms.instances.Instance? {
        formController.finalizeForm()

        val formEntities = formController.getEntities()
        if (!instance.isEdit()) {
            LocalEntityUseCases.updateLocalEntitiesFromForm(
                formEntities,
                entitiesRepository
            )
        }

        val instanceName = formController.getSubmissionMetadata()?.instanceName
        return instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder(instance)
                .status(com.jed.optima.forms.instances.Instance.STATUS_COMPLETE)
                .canEditWhenComplete(formController.isSubmissionEntireForm())
                .displayName(instanceName ?: instance.displayName)
                .canDeleteBeforeSend(formEntities == null)
                .build()
        )
    }

    @JvmStatic
    private fun saveInstanceToDisk(formController: FormController) {
        val payload = formController.getSubmissionXml()
        com.jed.optima.android.utilities.FileUtils.write(formController.getInstanceFile(), payload!!.payloadBytes)
    }

    private fun getInstanceFromFormController(
        formController: FormController,
        instancesRepository: com.jed.optima.forms.instances.InstancesRepository
    ): com.jed.optima.forms.instances.Instance? {
        val instancePath = formController.getInstanceFile()!!.absolutePath
        return instancesRepository.getOneByPath(instancePath)
    }

    private fun createFormDefFromCacheOrXml(xForm: File, formDefCache: FormDefCache): FormDef? {
        val formDefFromCache = formDefCache.readCache(xForm)
        if (formDefFromCache != null) {
            return formDefFromCache
        }

        val lastSavedSrc = com.jed.optima.android.utilities.FileUtils.getOrCreateLastSavedSrc(xForm)
        return XFormUtils.getFormFromFormXml(xForm.absolutePath, lastSavedSrc)?.also {
            formDefCache.writeCache(it, xForm.path)
        }
    }

    private fun importInstance(instanceFile: File, formEntryController: FormEntryController) {
        // convert files into a byte array
        val fileBytes = readFileToByteArray(instanceFile)

        // get the root of the saved and template instances
        val savedRoot = XFormParser.restoreDataModel(fileBytes, null).root
        val templateRoot = formEntryController.model.form.instance.root.deepCopy(true)

        // weak check for matching forms
        if (savedRoot.name != templateRoot.name || savedRoot.mult != 0) {
            return
        }

        // populate the data model
        val tr = TreeReference.rootRef()
        tr.add(templateRoot.name, TreeReference.INDEX_UNBOUND)

        // Here we set the Collect's implementation of the IAnswerResolver.
        // We set it back to the default after select choices have been populated.
        XFormParser.setAnswerResolver(com.jed.optima.android.dynamicpreload.ExternalAnswerResolver())
        templateRoot.populate(savedRoot, formEntryController.model.form)
        XFormParser.setAnswerResolver(DefaultAnswerResolver())

        // FormInstanceParser.parseInstance is responsible for initial creation of instances. It explicitly sets the
        // main instance name to null so we force this again on deserialization because some code paths rely on the main
        // instance not having a name. Must be before the call on setRoot because setRoot also sets the root's name.
        formEntryController.model.form.instance.name = null

        // populated model to current form
        formEntryController.model.form.instance.root = templateRoot

        // fix any language issues
        // :
        // http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
        if (formEntryController.model.languages != null) {
            formEntryController.model.form
                .localeChanged(
                    formEntryController.model.language,
                    formEntryController.model.form.localizer
                )
        }
    }
}
