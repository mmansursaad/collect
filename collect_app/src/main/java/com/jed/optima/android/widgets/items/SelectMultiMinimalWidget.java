package com.jed.optima.android.widgets.items;

import static com.jed.optima.android.formentry.media.FormMediaUtils.getPlayColor;

import android.annotation.SuppressLint;
import android.content.Context;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import com.jed.optima.android.activities.FormFillingActivity;
import com.jed.optima.android.formentry.questions.QuestionDetails;
import com.jed.optima.android.fragments.dialogs.SelectMinimalDialog;
import com.jed.optima.android.fragments.dialogs.SelectMultiMinimalDialog;
import com.jed.optima.android.utilities.Appearances;
import com.jed.optima.android.utilities.HtmlUtils;
import com.jed.optima.android.widgets.interfaces.SelectChoiceLoader;
import com.jed.optima.android.widgets.utilities.WaitingForDataRegistry;
import com.jed.optima.android.widgets.warnings.SpacesInUnderlyingValuesWarning;
import com.jed.optima.androidshared.ui.DialogFragmentUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
public class SelectMultiMinimalWidget extends SelectMinimalWidget {
    private List<Selection> selectedItems;

    public SelectMultiMinimalWidget(Context context, QuestionDetails prompt, WaitingForDataRegistry waitingForDataRegistry, SelectChoiceLoader selectChoiceLoader, Dependencies dependencies) {
        super(context, prompt, waitingForDataRegistry, selectChoiceLoader, dependencies);
        render();

        selectedItems = getFormEntryPrompt().getAnswerValue() == null
                ? new ArrayList<>() :
                (List<Selection>) getFormEntryPrompt().getAnswerValue().getValue();
        updateAnswerLabel();
        SpacesInUnderlyingValuesWarning
                .forQuestionWidget(this)
                .renderWarningIfNecessary(items);
    }

    @Override
    protected void showDialog() {
        int numColumns = Appearances.getNumberOfColumns(getFormEntryPrompt(), screenUtils);
        boolean noButtonsMode = Appearances.isCompactAppearance(getFormEntryPrompt()) || Appearances.isNoButtonsAppearance(getFormEntryPrompt());

        SelectMultiMinimalDialog dialog = new SelectMultiMinimalDialog(new ArrayList<>(selectedItems),
                Appearances.isFlexAppearance(getFormEntryPrompt()),
                Appearances.isAutocomplete(getFormEntryPrompt()), getContext(), items,
                getFormEntryPrompt(), getReferenceManager(),
                getPlayColor(getFormEntryPrompt(), themeUtils), numColumns, noButtonsMode, mediaUtils);

        DialogFragmentUtils.showIfNotShowing(dialog, SelectMinimalDialog.class, ((FormFillingActivity) getContext()).getSupportFragmentManager());
    }

    @Override
    public IAnswerData getAnswer() {
        return selectedItems.isEmpty()
                ? null
                : new SelectMultiData(selectedItems);
    }

    @Override
    public void clearAnswer() {
        selectedItems = new ArrayList<>();
        super.clearAnswer();
    }

    @Override
    public void setData(Object answer) {
        selectedItems = (List<Selection>) answer;
        updateAnswerLabel();
        widgetValueChanged();
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        if (isSelected) {
            selectedItems.add(items.get(choiceIndex).selection());
        } else {
            selectedItems.remove(items.get(choiceIndex).selection());
        }
    }

    private void updateAnswerLabel() {
        if (selectedItems.isEmpty()) {
            binding.answer.setText(com.jed.optima.strings.R.string.select_answer);
        } else {
            StringBuilder builder = new StringBuilder();
            for (Selection selectedItem : selectedItems) {
                builder.append(getFormEntryPrompt().getSelectItemText(selectedItem));
                if (selectedItems.size() - 1 > selectedItems.indexOf(selectedItem)) {
                    builder.append(", ");
                }
            }
            binding.answer.setText(HtmlUtils.textToHtml(builder.toString()));
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }
}
