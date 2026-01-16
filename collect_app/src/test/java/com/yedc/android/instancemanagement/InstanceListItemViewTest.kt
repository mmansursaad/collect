package com.yedc.android.instancemanagement

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.yedc.android.R
import com.yedc.android.databinding.FormChooserListItemBinding
import com.yedc.formstest.InstanceFixtures

@RunWith(AndroidJUnit4::class)
class InstanceListItemViewTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val layoutInflater = LayoutInflater.from(context)

    @Before
    fun setup() {
        context.setTheme(R.style.Theme_Collect)
    }

    @Test
    fun `show an error chip if the status is STATUS_INVALID`() {
        val binding = FormChooserListItemBinding.inflate(layoutInflater)
        val instance = InstanceFixtures.instance(status = com.yedc.forms.instances.Instance.STATUS_INVALID)

        InstanceListItemView.setInstance(binding.root, instance, false)

        assertThat(binding.chip.visibility, equalTo(View.VISIBLE))
        assertThat(binding.chip.errors, equalTo(true))
    }

    @Test
    fun `show a no-error chip if the status is STATUS_VALID`() {
        val binding = FormChooserListItemBinding.inflate(layoutInflater)
        val instance = InstanceFixtures.instance(status = com.yedc.forms.instances.Instance.STATUS_VALID)

        InstanceListItemView.setInstance(binding.root, instance, false)

        assertThat(binding.chip.visibility, equalTo(View.VISIBLE))
        assertThat(binding.chip.errors, equalTo(false))
    }

    @Test
    fun `show a no-error chip if the status is STATUS_NEW_EDIT`() {
        val binding = FormChooserListItemBinding.inflate(layoutInflater)
        val instance = InstanceFixtures.instance(status = com.yedc.forms.instances.Instance.STATUS_NEW_EDIT)

        InstanceListItemView.setInstance(binding.root, instance, false)

        assertThat(binding.chip.visibility, equalTo(View.VISIBLE))
        assertThat(binding.chip.errors, equalTo(false))
    }

    @Test
    fun `show an error chip if the status is STATUS_INCOMPLETE`() {
        val binding = FormChooserListItemBinding.inflate(layoutInflater)
        val instance = InstanceFixtures.instance(status = com.yedc.forms.instances.Instance.STATUS_INCOMPLETE)

        InstanceListItemView.setInstance(binding.root, instance, false)

        assertThat(binding.chip.visibility, equalTo(View.VISIBLE))
        assertThat(binding.chip.errors, equalTo(true))
    }

    @Test
    fun `do not show a chip if the status is STATUS_COMPLETE`() {
        val binding = FormChooserListItemBinding.inflate(layoutInflater)
        val instance = InstanceFixtures.instance(status = com.yedc.forms.instances.Instance.STATUS_COMPLETE)

        InstanceListItemView.setInstance(binding.root, instance, false)

        assertThat(binding.chip.visibility, equalTo(View.GONE))
    }
}
