package com.jed.optima.android.support.pages;

public class CancelRecordingDialog extends Page<CancelRecordingDialog> {

    private final String formName;

    CancelRecordingDialog(String formName) {
        this.formName = formName;
    }

    @Override
    public CancelRecordingDialog assertOnPage() {
        assertText(com.jed.optima.strings.R.string.stop_recording_confirmation);
        return this;
    }

    public FormEntryPage clickOk() {
        clickOKOnDialog();
        return new FormEntryPage(formName).assertOnPage();
    }
}
