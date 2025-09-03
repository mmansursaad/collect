package com.jed.optima.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.graphics.Bitmap;

import androidx.test.espresso.Espresso;

import com.jed.optima.android.support.ActivityHelpers;
import com.jed.optima.testshared.WaitFor;
import com.jed.optima.androidtest.DrawableMatcher;

public class QRCodePage extends Page<QRCodePage> {
    @Override
    public QRCodePage assertOnPage() {
        assertText(com.jed.optima.strings.R.string.reconfigure_with_qr_code_settings_title);
        return this;
    }

    public QRCodePage clickScanFragment() {
        onView(withText(com.jed.optima.strings.R.string.scan_qr_code_fragment_title)).perform(click());
        return this;
    }

    public QRCodePage clickView() {
        // Switching tabs doesn't seem to work sometimes
        WaitFor.waitFor(() -> {
            onView(withText(com.jed.optima.strings.R.string.view_qr_code_fragment_title)).perform(click());
            onView(withText(com.jed.optima.strings.R.string.barcode_scanner_prompt)).check(doesNotExist());
            return null;
        });

        return this;
    }

    public QRCodePage assertImageViewShowsImage(int resourceid, Bitmap image) {
        onView(withId(resourceid)).check(matches(DrawableMatcher.withBitmap(image)));
        return this;
    }

    public QRCodePage clickOnMenu() {
        return tryAgainOnFail(this, () -> {
            Espresso.openActionBarOverflowOrOptionsMenu(ActivityHelpers.getActivity());
            onView(withText(getTranslatedString(com.jed.optima.strings.R.string.import_qrcode_sd))).check(matches(isDisplayed()));
        });
    }
}
