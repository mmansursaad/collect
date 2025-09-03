package com.jed.optima.android.formlists.savedformlist

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import com.jed.optima.android.R
import com.jed.optima.androidshared.ui.FragmentFactoryBuilder
import com.jed.optima.formstest.InstanceFixtures
import com.jed.optima.fragmentstest.FragmentScenarioLauncherRule
import com.jed.optima.strings.R.string
import com.jed.optima.testshared.Assertions
import com.jed.optima.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import com.jed.optima.testshared.ViewActions.clickOnItemWith
import com.jed.optima.testshared.ViewMatchers.recyclerView

@RunWith(AndroidJUnit4::class)
class DeleteSavedFormFragmentTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val formsToDisplay = MutableLiveData<List<com.jed.optima.forms.instances.Instance>>(emptyList())
    private val isDeleting = MutableLiveData(false)
    private val savedFormListViewModel = mock<SavedFormListViewModel> {
        on { formsToDisplay } doReturn formsToDisplay
        on { isDeleting } doReturn isDeleting
        on { deleteForms(any()) } doReturn MutableLiveData(null)
    }

    private val viewModelFactory = viewModelFactory {
        addInitializer(SavedFormListViewModel::class) { savedFormListViewModel }
    }

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(DeleteSavedFormFragment::class) {
                DeleteSavedFormFragment(viewModelFactory)
            }.build()
    )

    @Test
    fun `clicking delete selected and then cancelling does nothing`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteSavedFormFragment::class.java)
        formsToDisplay.value = listOf(
            InstanceFixtures.instance(dbId = 1, displayName = "Form 1"),
            InstanceFixtures.instance(dbId = 2, displayName = "Form 2")
        )

        onView(recyclerView()).perform(clickOnItemWith(withText("Form 1")))
        onView(recyclerView()).perform(clickOnItemWith(withText("Form 2")))

        onView(withText(string.delete_file)).perform(click())
        onView(withText(context.getString(string.delete_confirm, 2)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(string.delete_no))
            .inRoot(isDialog())
            .perform(click())

        verify(savedFormListViewModel, never()).deleteForms(any())
    }

    @Test
    fun `clicking delete selected unselects forms`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteSavedFormFragment::class.java)
        formsToDisplay.value = listOf(
            InstanceFixtures.instance(dbId = 1, displayName = "Form 1"),
            InstanceFixtures.instance(dbId = 2, displayName = "Form 2")
        )

        onView(recyclerView()).perform(clickOnItemWith(withText("Form 1")))

        onView(withText(string.delete_file)).perform(click())
        onView(withText(context.getString(string.delete_confirm, 1)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(string.delete_yes))
            .inRoot(isDialog())
            .perform(click())

        onView(withRecyclerView(R.id.list).atPositionOnView(0, R.id.form_title))
            .check(matches(withText("Form 1")))
        onView(withRecyclerView(R.id.list).atPositionOnView(0, R.id.checkbox))
            .check(matches(not(isChecked())))
    }

    @Test
    fun `shows progress while deleting forms`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteSavedFormFragment::class.java)
        formsToDisplay.value = listOf(InstanceFixtures.instance(dbId = 1))

        isDeleting.value = true
        onView(withText(string.form_delete_message)).inRoot(isDialog()).check(matches(isDisplayed()))

        isDeleting.value = false
        onView(withText(string.delete_file)).check(matches(isDisplayed()))
    }

    @Test
    fun `displays only the latest edit when all edits are unsent`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteSavedFormFragment::class.java)
        formsToDisplay.value = listOf(
            InstanceFixtures.instance(dbId = 1, displayName = "Form"),
            InstanceFixtures.instance(dbId = 2, displayName = "Form", editOf = 1, editNumber = 1),
            InstanceFixtures.instance(dbId = 3, displayName = "Form", editOf = 1, editNumber = 2),
            InstanceFixtures.instance(dbId = 4, displayName = "Form", editOf = 1, editNumber = 3)
        )

        Assertions.assertNotVisible(withText("Form"))
        Assertions.assertNotVisible(withText("Form (Edit 1)"))
        Assertions.assertNotVisible(withText("Form (Edit 2)"))
        Assertions.assertVisible(withText("Form (Edit 3)"))
    }

    @Test
    fun `displays latest edit and submitted edits when not all are unsent`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteSavedFormFragment::class.java)
        formsToDisplay.value = listOf(
            InstanceFixtures.instance(dbId = 1, displayName = "Form"),
            InstanceFixtures.instance(dbId = 2, displayName = "Form", editOf = 1, editNumber = 1),
            InstanceFixtures.instance(dbId = 3, displayName = "Form", editOf = 1, editNumber = 2, status = com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED),
            InstanceFixtures.instance(dbId = 4, displayName = "Form", editOf = 1, editNumber = 3)
        )

        Assertions.assertNotVisible(withText("Form"))
        Assertions.assertNotVisible(withText("Form (Edit 1)"))
        Assertions.assertVisible(withText("Form (Edit 2)"))
        Assertions.assertVisible(withText("Form (Edit 3)"))
    }
}
