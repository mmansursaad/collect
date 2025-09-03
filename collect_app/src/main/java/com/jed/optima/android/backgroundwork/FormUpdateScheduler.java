package com.jed.optima.android.backgroundwork;

public interface FormUpdateScheduler {

    void scheduleUpdates(String projectId);

    void cancelUpdates(String projectId);
}
