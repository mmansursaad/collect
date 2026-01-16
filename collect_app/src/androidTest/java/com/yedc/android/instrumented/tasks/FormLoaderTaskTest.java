package com.yedc.android.instrumented.tasks;

import static org.mockito.Mockito.mock;

import android.app.Application;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.FormDef;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import com.yedc.android.external.FormsContract;
import com.yedc.android.injection.DaggerUtils;
import com.yedc.android.injection.config.AppDependencyComponent;
import com.yedc.android.storage.StoragePathProvider;
import com.yedc.android.storage.StorageSubdirectory;
import com.yedc.android.support.StorageUtils;
import com.yedc.android.support.rules.RunnableRule;
import com.yedc.android.support.rules.TestRuleChain;
import com.yedc.android.tasks.FormLoaderTask;
import com.yedc.android.tasks.FormLoaderTask.FormEntryControllerFactory;
import com.yedc.android.utilities.FormsRepositoryProvider;
import com.yedc.forms.Form;
import com.yedc.forms.instances.Instance;
import com.yedc.projects.Project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class FormLoaderTaskTest {

    private final StoragePathProvider storagePathProvider = new StoragePathProvider();

    private static final String SECONDARY_INSTANCE_EXTERNAL_CSV_FORM = "external_csv_form.xml";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FORM = "simple-search-external-csv.xml";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FILE = "simple-search-external-csv-fruits.csv";
    private static final String SIMPLE_SEARCH_EXTERNAL_DB_FILE = "simple-search-external-csv-fruits.db";

    private final FormEntryControllerFactory formEntryControllerFactory = new FormEntryControllerFactory() {
        @Override
        public FormEntryController create(FormDef formDef, File formMediaDir, Instance instance) {
            return new FormEntryController(new FormEntryModel(formDef));
        }
    };

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(new RunnableRule(() -> {
                try {
                    // Set up demo project
                    AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext());
                    component.projectsRepository().save(Project.Companion.getDEMO_PROJECT());
                    component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID);

                    StorageUtils.copyFormToDemoProject(SECONDARY_INSTANCE_EXTERNAL_CSV_FORM, Arrays.asList("external_csv_cities.csv", "external_csv_countries.csv", "external_csv_neighbourhoods.csv"), true);
                    StorageUtils.copyFormToDemoProject(SIMPLE_SEARCH_EXTERNAL_CSV_FORM, Collections.singletonList(SIMPLE_SEARCH_EXTERNAL_CSV_FILE), true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

    // Validate that importing external data multiple times does not fail due to side effects from import
    @Test
    public void loadSearchFromExternalCSVmultipleTimes() throws Exception {
        final String formPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM;
        final Form form = new FormsRepositoryProvider(ApplicationProvider.getApplicationContext()).create().getOneByPath(formPath);
        final Uri formUri = FormsContract.getUri("DEMO", form.getDbId());

        // initial load with side effects
        FormLoaderTask formLoaderTask = new FormLoaderTask(formUri, FormsContract.CONTENT_ITEM_TYPE, null, null, formEntryControllerFactory, mock(), mock());
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.executeSynchronously();
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.getController());

        File mediaFolder = wrapper.getController().getMediaFolder();
        File dbFile = new File(mediaFolder + File.separator + SIMPLE_SEARCH_EXTERNAL_DB_FILE);
        Assert.assertTrue(dbFile.exists());
        long dbLastModified = dbFile.lastModified();

        // subsequent load should succeed despite side effects from import
        formLoaderTask = new FormLoaderTask(formUri, FormsContract.CONTENT_ITEM_TYPE, null, null, formEntryControllerFactory, mock(), mock());
        wrapper = formLoaderTask.executeSynchronously();
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.getController());
        Assert.assertEquals("expected file modification timestamp to be unchanged", dbLastModified, dbFile.lastModified());
    }
}
