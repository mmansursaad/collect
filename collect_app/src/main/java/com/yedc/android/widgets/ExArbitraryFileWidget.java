package com.yedc.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.form.api.FormEntryPrompt;
import com.yedc.android.databinding.ExArbitraryFileWidgetAnswerBinding;
import com.yedc.android.formentry.questions.QuestionDetails;
import com.yedc.android.utilities.ApplicationConstants;
import com.yedc.android.utilities.QuestionMediaManager;
import com.yedc.android.widgets.utilities.FileRequester;
import com.yedc.android.widgets.utilities.WaitingForDataRegistry;

@SuppressLint("ViewConstructor")
public class ExArbitraryFileWidget extends BaseArbitraryFileWidget {
    ExArbitraryFileWidgetAnswerBinding binding;

    private final FileRequester fileRequester;

    public ExArbitraryFileWidget(Context context, QuestionDetails questionDetails,
                                 QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry,
                                 FileRequester fileRequester, Dependencies dependencies) {
        super(context, questionDetails, questionMediaManager, waitingForDataRegistry, dependencies);
        this.fileRequester = fileRequester;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = ExArbitraryFileWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        setupAnswerFile(prompt.getAnswerText());

        binding.exArbitraryFileAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (questionDetails.isReadOnly()) {
            binding.exArbitraryFileButton.setVisibility(GONE);
        } else {
            binding.exArbitraryFileButton.setOnClickListener(v -> onButtonClick());
            binding.exArbitraryFileAnswerText.setOnClickListener(v -> mediaUtils.openFile(getContext(), answerFile, null));
        }

        if (answerFile != null) {
            binding.exArbitraryFileAnswerText.setText(answerFile.getName());
            binding.exArbitraryFileAnswerText.setVisibility(VISIBLE);
        }

        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        binding.exArbitraryFileAnswerText.setVisibility(GONE);
        deleteFile();
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        binding.exArbitraryFileButton.setOnLongClickListener(listener);
        binding.exArbitraryFileAnswerText.setOnLongClickListener(listener);
    }

    @Override
    protected void showAnswerText() {
        binding.exArbitraryFileAnswerText.setText(answerFile.getName());
        binding.exArbitraryFileAnswerText.setVisibility(VISIBLE);
    }

    @Override
    protected void hideAnswerText() {
        binding.exArbitraryFileAnswerText.setVisibility(GONE);
    }

    private void onButtonClick() {
        waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
        fileRequester.launch((Activity) getContext(), ApplicationConstants.RequestCodes.EX_ARBITRARY_FILE_CHOOSER, getFormEntryPrompt());
    }
}
