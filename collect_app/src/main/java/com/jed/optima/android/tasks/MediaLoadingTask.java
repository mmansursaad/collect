package com.jed.optima.android.tasks;

import static com.jed.optima.settings.keys.ProjectKeys.KEY_IMAGE_SIZE;

import android.net.Uri;
import android.os.AsyncTask;

import com.jed.optima.android.activities.FormFillingActivity;
import com.jed.optima.android.application.Collect;
import com.jed.optima.android.injection.DaggerUtils;
import com.jed.optima.android.utilities.ContentUriHelper;
import com.jed.optima.android.utilities.FileUtils;
import com.jed.optima.android.utilities.ImageCompressionController;
import com.jed.optima.android.widgets.BaseImageWidget;
import com.jed.optima.android.widgets.QuestionWidget;
import com.jed.optima.androidshared.ui.DialogFragmentUtils;
import com.jed.optima.settings.SettingsProvider;

import java.io.File;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class MediaLoadingTask extends AsyncTask<Uri, Void, File> {

    private final File instanceFile;
    @Inject
    SettingsProvider settingsProvider;

    @Inject
    ImageCompressionController imageCompressionController;

    private WeakReference<FormFillingActivity> formFillingActivity;

    public MediaLoadingTask(FormFillingActivity formFillingActivity, File instanceFile) {
        this.instanceFile = instanceFile;
        onAttach(formFillingActivity);
    }

    public void onAttach(FormFillingActivity formFillingActivity) {
        this.formFillingActivity = new WeakReference<>(formFillingActivity);
        DaggerUtils.getComponent(this.formFillingActivity.get()).inject(this);
    }

    @Override
    protected File doInBackground(Uri... uris) {
        if (instanceFile != null) {
            String extension = ContentUriHelper.getFileExtensionFromUri(uris[0]);

            File newFile = FileUtils.createDestinationMediaFile(instanceFile.getParent(), extension);
            FileUtils.saveAnswerFileFromUri(uris[0], newFile, Collect.getInstance());
            QuestionWidget questionWidget = formFillingActivity.get().getWidgetWaitingForBinaryData();

            // apply image conversion if the widget is an image widget
            if (questionWidget instanceof BaseImageWidget) {
                String imageSizeMode = settingsProvider.getUnprotectedSettings().getString(KEY_IMAGE_SIZE);
                imageCompressionController.execute(newFile.getPath(), questionWidget, formFillingActivity.get(), imageSizeMode);
            }
            return newFile;
        }
        return null;
    }

    @Override
    protected void onPostExecute(File result) {
        FormFillingActivity activity = this.formFillingActivity.get();
        DialogFragmentUtils.dismissDialog(FormFillingActivity.TAG_PROGRESS_DIALOG_MEDIA_LOADING, activity.getSupportFragmentManager());
        activity.setWidgetData(result);
    }
}
