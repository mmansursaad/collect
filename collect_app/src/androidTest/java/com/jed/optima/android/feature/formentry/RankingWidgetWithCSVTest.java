package com.jed.optima.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import com.jed.optima.android.support.rules.BlankFormTestRule;
import com.jed.optima.android.support.rules.TestRuleChain;
import com.jed.optima.android.support.pages.FormEntryPage;

import java.util.Collections;

public class RankingWidgetWithCSVTest {

    private static final String TEST_FORM = "ranking_widget.xml";

    public BlankFormTestRule activityTestRule = new BlankFormTestRule(TEST_FORM, "ranking_widget", Collections.singletonList("fruits.csv"));

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(activityTestRule);


    @Test
    public void rankingWidget_shouldDisplayItemsFromSearchFunc() {
        new FormEntryPage("ranking_widget")
                .clickRankingButton()
                .assertTexts("Mango", "Oranges", "Strawberries");
    }
}
