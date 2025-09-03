package com.jed.optima.android.formentry.saving;

import android.net.Uri;

import com.jed.optima.android.javarosawrapper.FormController;
import com.jed.optima.android.tasks.SaveFormToDisk;
import com.jed.optima.android.tasks.SaveToDiskResult;
import com.jed.optima.android.utilities.MediaUtils;
import com.jed.optima.entities.storage.EntitiesRepository;
import com.jed.optima.forms.instances.InstancesRepository;

import java.util.ArrayList;

public class DiskFormSaver implements FormSaver {

    @Override
    public SaveToDiskResult save(Uri instanceContentURI, FormController formController, MediaUtils mediaUtils, boolean shouldFinalize, boolean exitAfter,
                                 String updatedSaveName, ProgressListener progressListener, ArrayList<String> tempFiles, String currentProjectId, EntitiesRepository entitiesRepository, InstancesRepository instancesRepository) {
        SaveFormToDisk saveFormToDisk = new SaveFormToDisk(formController, mediaUtils, exitAfter, shouldFinalize,
                updatedSaveName, instanceContentURI, tempFiles, currentProjectId, entitiesRepository, instancesRepository);
        return saveFormToDisk.saveForm(progressListener);
    }
}
