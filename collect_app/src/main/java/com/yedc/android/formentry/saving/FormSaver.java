package com.yedc.android.formentry.saving;

import android.net.Uri;

import com.yedc.android.javarosawrapper.FormController;
import com.yedc.android.tasks.SaveToDiskResult;
import com.yedc.android.utilities.MediaUtils;
import com.yedc.entities.storage.EntitiesRepository;
import com.yedc.forms.instances.InstancesRepository;

import java.util.ArrayList;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, FormController formController, MediaUtils mediaUtils, boolean shouldFinalize, boolean exitAfter,
                          String updatedSaveName, ProgressListener progressListener, ArrayList<String> tempFiles, String currentProjectId, EntitiesRepository entitiesRepository, InstancesRepository instancesRepository);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
