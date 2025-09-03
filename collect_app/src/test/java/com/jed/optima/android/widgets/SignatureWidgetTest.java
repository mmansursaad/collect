package com.jed.optima.android.widgets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static com.jed.optima.android.support.CollectHelpers.setupFakeReferenceManager;
import static org.robolectric.Shadows.shadowOf;
import static java.util.Collections.singletonList;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.javarosa.core.reference.ReferenceManager;
import org.junit.Test;
import com.jed.optima.android.R;
import com.jed.optima.android.formentry.questions.QuestionDetails;
import com.jed.optima.android.injection.config.AppDependencyModule;
import com.jed.optima.android.support.CollectHelpers;
import com.jed.optima.android.support.MockFormEntryPromptBuilder;
import com.jed.optima.android.utilities.QuestionMediaManager;
import com.jed.optima.android.widgets.base.FileWidgetTest;
import com.jed.optima.android.widgets.support.FakeQuestionMediaManager;
import com.jed.optima.android.widgets.support.FakeWaitingForDataRegistry;
import com.jed.optima.android.widgets.support.SynchronousImageLoader;
import com.jed.optima.draw.DrawActivity;
import com.jed.optima.imageloader.ImageLoader;
import com.jed.optima.shared.TempFiles;

import java.io.File;
import java.io.IOException;

/**
 *  @author James Knight
 */
public class SignatureWidgetTest extends FileWidgetTest<SignatureWidget> {

    private File currentFile;

    @NonNull
    @Override
    public SignatureWidget createWidget() {
        QuestionMediaManager fakeQuestionMediaManager = new FakeQuestionMediaManager() {
            @Override
            public File getAnswerFile(String fileName) {
                File result;
                if (currentFile == null) {
                    result = super.getAnswerFile(fileName);
                } else {
                    result = fileName.equals(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER) ? currentFile : null;
                }
                return result;
            }
        };
        return new SignatureWidget(activity, new QuestionDetails(formEntryPrompt, readOnlyOverride),
                fakeQuestionMediaManager, new FakeWaitingForDataRegistry(), TempFiles.getPathInTempDir(), dependencies);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().binding.signButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.isReadOnly()).thenReturn(false);

        assertThat(getSpyWidget().binding.signButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenThereIsNoAnswer_hideImageViewAndErrorMessage() {
        SignatureWidget widget = createWidget();

        assertThat(widget.getImageView().getVisibility(), is(View.GONE));
        assertThat(widget.getImageView().getDrawable(), nullValue());

        assertThat(widget.getErrorTextView().getVisibility(), is(View.GONE));
    }

    @Test
    public void whenTheAnswerImageCanNotBeLoaded_hideImageViewAndShowErrorMessage() throws IOException {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ImageLoader providesImageLoader() {
                return new SynchronousImageLoader(true);
            }
        });

        String imagePath = File.createTempFile("current", ".bmp").getAbsolutePath();
        currentFile = new File(imagePath);

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
                .build();

        SignatureWidget widget = createWidget();

        assertThat(widget.getImageView().getVisibility(), is(View.GONE));
        assertThat(widget.getImageView().getDrawable(), nullValue());

        assertThat(widget.getErrorTextView().getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void whenPromptHasDefaultAnswer_showsInImageView() throws Exception {
        String imagePath = File.createTempFile("default", ".bmp").getAbsolutePath();

        ReferenceManager referenceManager = setupFakeReferenceManager(singletonList(
                new Pair<>(DrawWidgetTest.DEFAULT_IMAGE_ANSWER, imagePath)
        ));
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }

            @Override
            public ImageLoader providesImageLoader() {
                return new SynchronousImageLoader();
            }
        });

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(DrawWidgetTest.DEFAULT_IMAGE_ANSWER)
                .build();

        SignatureWidget widget = createWidget();
        ImageView imageView = widget.getImageView();
        assertThat(imageView.getVisibility(), is(View.VISIBLE));
        Drawable drawable = imageView.getDrawable();
        assertThat(drawable, notNullValue());

        String loadedPath = shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromPath();
        assertThat(loadedPath, equalTo(imagePath));
    }

    @Test
    public void whenPromptHasCurrentAnswer_showsInImageView() throws Exception {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ImageLoader providesImageLoader() {
                return new SynchronousImageLoader();
            }
        });

        String imagePath = File.createTempFile("current", ".bmp").getAbsolutePath();
        currentFile = new File(imagePath);

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
                .build();

        SignatureWidget widget = createWidget();
        ImageView imageView = widget.getImageView();
        assertThat(imageView.getVisibility(), is(View.VISIBLE));
        Drawable drawable = imageView.getDrawable();
        assertThat(drawable, notNullValue());

        String loadedPath = shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromPath();
        assertThat(loadedPath, equalTo(imagePath));
    }

    @Test
    public void whenPromptHasDefaultAnswer_passUriToDrawActivity() throws Exception {
        File file = File.createTempFile("default", ".bmp");
        String imagePath = file.getAbsolutePath();

        ReferenceManager referenceManager = setupFakeReferenceManager(singletonList(
                new Pair<>(DrawWidgetTest.DEFAULT_IMAGE_ANSWER, imagePath)
        ));
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }

            @Override
            public ImageLoader providesImageLoader() {
                return new SynchronousImageLoader();
            }
        });

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(DrawWidgetTest.DEFAULT_IMAGE_ANSWER)
                .build();

        Intent intent = getIntentLaunchedByClick(R.id.sign_button);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent);
        assertExtraEquals(DrawActivity.REF_IMAGE, Uri.fromFile(file), intent);
    }

    @Test
    public void whenPromptHasDefaultAnswerThatDoesNotExist_doNotPassUriToDrawActivity() throws Exception {
        ReferenceManager referenceManager = setupFakeReferenceManager(singletonList(
                new Pair<>(DrawWidgetTest.DEFAULT_IMAGE_ANSWER, "/something")
        ));
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }
        });

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(DrawWidgetTest.DEFAULT_IMAGE_ANSWER)
                .build();

        Intent intent = getIntentLaunchedByClick(R.id.sign_button);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent);
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), is(false));
    }

    @Test
    public void whenThereIsNoAnswer_doNotPassUriToDrawActivity() {
        Intent intent = getIntentLaunchedByClick(R.id.sign_button);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent);
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), is(false));
    }
}
