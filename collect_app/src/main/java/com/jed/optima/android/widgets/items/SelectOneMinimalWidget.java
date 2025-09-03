package com.jed.optima.android.widgets.items;

import static com.jed.optima.android.formentry.media.FormMediaUtils.getPlayColor;

import android.annotation.SuppressLint;
import android.content.Context;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import com.jed.optima.android.activities.FormFillingActivity;
import com.jed.optima.android.formentry.questions.QuestionDetails;
import com.jed.optima.android.fragments.dialogs.SelectMinimalDialog;
import com.jed.optima.android.fragments.dialogs.SelectOneMinimalDialog;
import com.jed.optima.android.listeners.AdvanceToNextListener;
import com.jed.optima.android.utilities.Appearances;
import com.jed.optima.android.utilities.HtmlUtils;
import com.jed.optima.android.utilities.SelectOneWidgetUtils;
import com.jed.optima.android.widgets.interfaces.SelectChoiceLoader;
import com.jed.optima.android.widgets.utilities.WaitingForDataRegistry;
import com.jed.optima.androidshared.ui.DialogFragmentUtils;

import java.util.List;

@SuppressLint("ViewConstructor")
public class SelectOneMinimalWidget extends SelectMinimalWidget {
    private Selection selectedItem;
    private final boolean autoAdvance;
    private AdvanceToNextListener autoAdvanceListener;

    public SelectOneMinimalWidget(Context context, QuestionDetails prompt, boolean autoAdvance, WaitingForDataRegistry waitingForDataRegistry, SelectChoiceLoader selectChoiceLoader, Dependencies dependencies) {
        super(context, prompt, waitingForDataRegistry, selectChoiceLoader, dependencies);
        render();

        selectedItem = SelectOneWidgetUtils.getSelectedItem(prompt.getPrompt(), items);
        this.autoAdvance = autoAdvance;
        if (context instanceof AdvanceToNextListener) {
            autoAdvanceListener = (AdvanceToNextListener) context;
        }
        updateAnswer();
    }

    @Override
    protected void showDialog() {
        int numColumns = Appearances.getNumberOfColumns(getFormEntryPrompt(), screenUtils);
        boolean noButtonsMode = Appearances.isCompactAppearance(getFormEntryPrompt()) || Appearances.isNoButtonsAppearance(getFormEntryPrompt());

        SelectOneMinimalDialog dialog = new SelectOneMinimalDialog(getSavedSelectedValue(),
                Appearances.isFlexAppearance(getFormEntryPrompt()),
                Appearances.isAutocomplete(getFormEntryPrompt()), getContext(), items,
                getFormEntryPrompt(), getReferenceManager(),
                getPlayColor(getFormEntryPrompt(), themeUtils), numColumns, noButtonsMode, mediaUtils);

        DialogFragmentUtils.showIfNotShowing(dialog, SelectMinimalDialog.class, ((FormFillingActivity) getContext()).getSupportFragmentManager());
    }

    @Override
    public IAnswerData getAnswer() {
        return selectedItem == null
                ? null
                : new SelectOneData(selectedItem);
    }

    @Override
    public void clearAnswer() {
        selectedItem = null;
        super.clearAnswer();
    }

    @Override
    public void setData(Object answer) {
        List<Selection> answers = (List<Selection>) answer;
        selectedItem = answers.isEmpty() ? null : answers.get(0);
        updateAnswer();
        widgetValueChanged();

        if (autoAdvance && autoAdvanceListener != null) {
            autoAdvanceListener.advance();
        }
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        selectedItem = isSelected
                ? items.get(choiceIndex).selection()
                : null;
    }

    private void updateAnswer() {
        if (selectedItem == null) {
            binding.answer.setText(com.jed.optima.strings.R.string.select_answer);
        } else {
            binding.answer.setText(HtmlUtils.textToHtml(getFormEntryPrompt().getSelectItemText(selectedItem)));
        }
    }

    private String getSavedSelectedValue() {
        return selectedItem == null
                ? null
                : selectedItem.getValue();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }
}
