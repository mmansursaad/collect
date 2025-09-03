package com.jed.optima.android.widgets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.jed.optima.settings.keys.ProjectKeys.KEY_FONT_SIZE;

import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.jed.optima.android.formentry.questions.QuestionDetails;
import com.jed.optima.android.injection.config.AppDependencyModule;
import com.jed.optima.android.support.CollectHelpers;
import com.jed.optima.android.utilities.ApplicationConstants;
import com.jed.optima.android.utilities.MediaUtils;
import com.jed.optima.android.widgets.base.FileWidgetTest;
import com.jed.optima.android.widgets.support.FakeQuestionMediaManager;
import com.jed.optima.android.widgets.support.FakeWaitingForDataRegistry;
import com.jed.optima.android.widgets.utilities.FileRequester;
import com.jed.optima.android.widgets.utilities.QuestionFontSizeUtils;
import com.jed.optima.androidshared.system.IntentLauncher;
import org.robolectric.shadows.ShadowToast;

import java.io.File;
import java.io.IOException;

public class ExImageWidgetTest extends FileWidgetTest<ExImageWidget> {
    @Mock
    FileRequester fileRequester;

    private MediaUtils mediaUtils;

    @Before
    public void setup() {
        mediaUtils = mock(MediaUtils.class);
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public MediaUtils providesMediaUtils(IntentLauncher intentLauncher) {
                return mediaUtils;
            }
        });
        when(mediaUtils.isImageFile(any())).thenReturn(true);
    }

    @Override
    public StringData getInitialAnswer() {
        return new StringData("image1.png");
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData("image2.png");
    }

    @NonNull
    @Override
    public ExImageWidget createWidget() {
        return new ExImageWidget(activity, new QuestionDetails(formEntryPrompt, readOnlyOverride),
                new FakeQuestionMediaManager(), new FakeWaitingForDataRegistry(), fileRequester, dependencies);
    }

    @Test
    public void whenWidgetCreated_shouldLaunchButtonBeVisible() {
        assertThat(getWidget().binding.launchExternalAppButton.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void whenWidgetCreated_shouldLaunchButtonHaveProperName() {
        assertThat(getWidget().binding.launchExternalAppButton.getText(), is("Launch"));
    }

    @Test
    public void whenFontSizeChanged_CustomFontSizeShouldBeUsed() {
        settingsProvider.getUnprotectedSettings().save(KEY_FONT_SIZE, "30");

        assertThat((int) getWidget().binding.launchExternalAppButton.getTextSize(), is(QuestionFontSizeUtils.getFontSize(settingsProvider.getUnprotectedSettings(), QuestionFontSizeUtils.FontSize.BODY_LARGE)));
    }

    @Test
    public void whenThereIsNoAnswer_shouldImageViewBeHidden() {
        assertThat(getWidget().binding.imageView.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenThereIsAnswer_shouldImageViewBeDisplayed() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        assertThat(getWidget().binding.imageView.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void whenAnswerCleared_shouldImageViewBeHidden() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExImageWidget widget = getWidget();
        widget.clearAnswer();
        assertThat(getWidget().binding.imageView.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenLaunchButtonClicked_exWidgetIntentLauncherShouldBeStarted() {
        getWidget().binding.launchExternalAppButton.performClick();
        verify(fileRequester).launch(activity, ApplicationConstants.RequestCodes.EX_IMAGE_CHOOSER, formEntryPrompt);
    }

    @Test
    public void whenImageViewClicked_shouldFileViewerByCalled() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExImageWidget widget = getWidget();
        widget.binding.imageView.performClick();
        verify(mediaUtils).openFile(activity, widget.answerFile, "image/*");
    }

    @Test
    public void whenSetDataCalledWithNull_shouldExistedAnswerBeRemoved() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExImageWidget widget = getWidget();
        widget.setData(null);
        assertThat(widget.getAnswer(), is(nullValue()));
        assertThat(widget.binding.imageView.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenUnsupportedFileTypeAttached_shouldNotThatFileBeAdded() throws IOException {
        ExImageWidget widget = getWidget();
        File answer = File.createTempFile("doc", ".pdf");
        when(mediaUtils.isImageFile(answer)).thenReturn(false);
        widget.setData(answer);
        assertThat(widget.getAnswer(), is(nullValue()));
        assertThat(widget.binding.imageView.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenUnsupportedFileTypeAttached_shouldTheFileBeRemoved() throws IOException {
        ExImageWidget widget = getWidget();
        File answer = File.createTempFile("doc", ".pdf");
        when(mediaUtils.isImageFile(answer)).thenReturn(false);
        widget.setData(answer);
        verify(mediaUtils).deleteMediaFile(answer.getAbsolutePath());
    }

    @Test
    public void whenUnsupportedFileTypeAttached_shouldToastBeDisplayed() throws IOException {
        ExImageWidget widget = getWidget();
        File answer = File.createTempFile("doc", ".pdf");
        when(mediaUtils.isImageFile(answer)).thenReturn(false);
        widget.setData(answer);
        assertThat(ShadowToast.getTextOfLatestToast(), is("Application returned an invalid file type"));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExImageWidget widget = getWidget();
        assertThat(widget.binding.launchExternalAppButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.imageView.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExImageWidget widget = getWidget();
        assertThat(widget.binding.launchExternalAppButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.imageView.getVisibility(), is(View.VISIBLE));
    }
}
