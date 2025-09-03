package com.jed.optima.android.application.initialization

import org.javarosa.core.model.CoreModelModule
import org.javarosa.core.services.PrototypeManager
import org.javarosa.core.util.JavaRosaCoreModule
import org.javarosa.model.xform.XFormsModule
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.parse.XFormParserFactory
import org.javarosa.xform.util.XFormUtils
import com.jed.optima.android.dynamicpreload.DynamicPreloadXFormParserFactory
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.entities.javarosa.intance.LocalEntitiesExternalInstanceParserFactory
import com.jed.optima.entities.storage.EntitiesRepository
import com.jed.optima.metadata.PropertyManager
import com.jed.optima.projects.ProjectDependencyFactory
import com.jed.optima.settings.SettingsProvider

class JavaRosaInitializer(
    private val propertyManager: PropertyManager,
    private val projectsDataService: ProjectsDataService,
    private val entitiesRepositoryProvider: ProjectDependencyFactory<EntitiesRepository>,
    private val settingsProvider: SettingsProvider
) {

    fun initialize() {
        propertyManager.reload()
        org.javarosa.core.services.PropertyManager
            .setPropertyManager(propertyManager)

        // Register prototypes for classes that FormDef uses
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames)
        PrototypeManager.registerPrototypes(CoreModelModule.classNames)
        XFormsModule().registerModule()

        // When registering prototypes from Collect, a proguard exception also needs to be added
        PrototypeManager.registerPrototype("com.jed.optima.android.logic.actions.setgeopoint.CollectSetGeopointAction")
        XFormParser.registerActionHandler(
            com.jed.optima.android.logic.actions.setgeopoint.CollectSetGeopointActionHandler.ELEMENT_NAME,
            com.jed.optima.android.logic.actions.setgeopoint.CollectSetGeopointActionHandler()
        )

        // Configure default parser factory
        val entityXFormParserFactory =
            com.jed.optima.entities.javarosa.parse.EntityXFormParserFactory(
                XFormParserFactory()
            )
        val dynamicPreloadXFormParserFactory =
            DynamicPreloadXFormParserFactory(entityXFormParserFactory)

        XFormUtils.setXFormParserFactory(dynamicPreloadXFormParserFactory)

        val localEntitiesExternalInstanceParserFactory = LocalEntitiesExternalInstanceParserFactory(
            { entitiesRepositoryProvider.create(projectsDataService.requireCurrentProject().uuid) },
            { true }
        )

        XFormUtils.setExternalInstanceParserFactory(localEntitiesExternalInstanceParserFactory)
    }
}
