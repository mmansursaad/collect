package com.jed.optima.android.feature.entitymanagement

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import com.jed.optima.android.support.StubOpenRosaServer
import com.jed.optima.android.support.TestDependencies
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain

class ViewEntitiesTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun canViewLocallyCreatedEntitiesInBrowser() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm(
            "one-question-entity-follow-up.xml",
            listOf(StubOpenRosaServer.EntityListItem("people.csv"))
        )

        rule.withMatchExactlyProject(testDependencies.server.url)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(com.jed.optima.android.support.pages.FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))
            .openEntityBrowser()
            .clickOnList("people")
            .assertEntity("Logan Roy", "full_name: Logan Roy")
    }

    @Test
    fun canViewListEntitiesInBrowser() {
        testDependencies.server.addForm(
            "one-question-entity-follow-up.xml",
            listOf(StubOpenRosaServer.EntityListItem("people.csv"))
        )

        rule.withMatchExactlyProject(testDependencies.server.url)
            .refreshForms()
            .openEntityBrowser()
            .clickOnList("people")
            .assertEntity("Roman Roy", "full_name: Roman Roy")
    }
}
