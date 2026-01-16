package com.yedc.android.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.FormIndex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.yedc.android.javarosawrapper.FormController;
import com.yedc.android.storage.StoragePathProvider;
import com.yedc.android.storage.StorageSubdirectory;
import com.yedc.android.support.CollectHelpers;

import java.io.File;

// Verify that a FormIndex can be saved to and restored from a file
@RunWith(AndroidJUnit4.class)
public class SaveFormIndexTaskTest {

    @Before
    public void setup() {
        CollectHelpers.setupDemoProject(); // Allows using StoragePathProvider
    }

    @Test
    public void saveAndReadFormIndexTest() {
        String instanceName = "test.xml";

        // for loadFormIndexFromFile
        File instancePath = new File(new StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES) + File.separator + instanceName);
        FormController formController = mock(FormController.class);
        when(formController.getInstanceFile()).thenReturn(instancePath);

        FormIndex originalFormIndex = FormIndex.createBeginningOfFormIndex();
        File indexFile = SaveFormToDisk.getFormIndexFile(instanceName);
        SaveFormIndexTask.exportFormIndexToFile(originalFormIndex, indexFile);

        FormIndex readFormIndex = SaveFormIndexTask.loadFormIndexFromFile(formController);

        assertEquals(originalFormIndex, readFormIndex);
        assertNotNull(readFormIndex);
        assertEquals(originalFormIndex.getReference(), readFormIndex.getReference());
    }
}
