package com.jed.optima.android.formentry.saving;

import android.net.Uri;

import com.jed.optima.android.javarosawrapper.FormController;
import com.jed.optima.android.tasks.SaveToDiskResult;
import com.jed.optima.android.utilities.MediaUtils;
import com.jed.optima.entities.storage.EntitiesRepository;
import com.jed.optima.forms.instances.InstancesRepository;

import java.util.ArrayList;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, FormController formController, MediaUtils mediaUtils, boolean shouldFinalize, boolean exitAfter,
                          String updatedSaveName, ProgressListener progressListener, ArrayList<String> tempFiles, String currentProjectId, EntitiesRepository entitiesRepository, InstancesRepository instancesRepository);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
