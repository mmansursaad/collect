package com.jed.optima.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import com.jed.optima.android.support.pages.MainMenuPage;
import com.jed.optima.android.support.pages.SaveOrDiscardFormDialog;
import com.jed.optima.android.support.rules.CollectTestRule;
import com.jed.optima.android.support.rules.TestRuleChain;

@RunWith(AndroidJUnit4.class)
public class FormLanguageTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain().around(rule);

    @Test
    public void canSwitchLanguagesInForm() {
        rule.startAtMainMenu()
                .copyForm("one-question-translation.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "64")
                .clickOptionsIcon()
                .clickOnString(com.jed.optima.strings.R.string.change_language)
                .clickOnText("French (fr)")
                .assertQuestion("quel âge as-tu")
                .assertText("64"); // Check answer hasn't been cleared/changed
    }

    @Test
    public void languageChoiceIsPersisted() {
        rule.startAtMainMenu()
                .copyForm("one-question-translation.xml")
                .startBlankForm("One Question")
                .clickOptionsIcon()
                .clickOnString(com.jed.optima.strings.R.string.change_language)
                .clickOnText("French (fr)")
                .closeSoftKeyboard()
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickDiscardForm()

                .startBlankForm("One Question")
                .assertQuestion("quel âge as-tu");
    }
}
