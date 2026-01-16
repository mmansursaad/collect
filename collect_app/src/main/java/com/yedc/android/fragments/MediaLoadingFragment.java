package com.yedc.android.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import com.yedc.android.activities.FormFillingActivity;
import com.yedc.android.javarosawrapper.FormController;
import com.yedc.android.tasks.MediaLoadingTask;

public class MediaLoadingFragment extends Fragment {

    private MediaLoadingTask mediaLoadingTask;

    public void beginMediaLoadingTask(Uri uri, FormController formController) {
        mediaLoadingTask = new MediaLoadingTask((FormFillingActivity) getActivity(), formController.getInstanceFile());
        mediaLoadingTask.execute(uri);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (mediaLoadingTask != null) {
            mediaLoadingTask.onAttach((FormFillingActivity) getActivity());
        }
    }

    public boolean isMediaLoadingTaskRunning() {
        return mediaLoadingTask != null && mediaLoadingTask.getStatus() == AsyncTask.Status.RUNNING;
    }
}
