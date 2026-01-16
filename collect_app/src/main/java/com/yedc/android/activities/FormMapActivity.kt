/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.yedc.android.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yedc.android.R
import com.yedc.android.formmanagement.FormFillingIntentFactory
import com.yedc.android.formmanagement.formmap.FormMapViewModel
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.projects.ProjectsDataService
import com.yedc.android.utilities.FormsRepositoryProvider
import com.yedc.android.utilities.InstancesRepositoryProvider
import com.yedc.androidshared.ui.FragmentFactoryBuilder
import com.yedc.async.Scheduler
import com.yedc.geo.selection.SelectionMapFragment
import com.yedc.settings.SettingsProvider
import com.yedc.strings.localization.LocalizedActivity
import javax.inject.Inject

/**
 * Show a map with points representing saved instances of the selected form.
 */
class FormMapActivity : LocalizedActivity() {

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var instancesRepositoryProvider: InstancesRepositoryProvider

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var scheduler: Scheduler

    private val formId by lazy { intent.getLongExtra(EXTRA_FORM_ID, -1) }
    private val viewModel: FormMapViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FormMapViewModel(
                    resources,
                    formId,
                    formsRepositoryProvider.create(),
                    instancesRepositoryProvider.create(),
                    settingsProvider,
                    scheduler
                ) as T
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        DaggerUtils.getComponent(this).inject(this)
        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(SelectionMapFragment::class.java) {
                SelectionMapFragment(
                    selectionMapData = viewModel
                )
            }
            .build()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_map_activity)

        supportFragmentManager.setFragmentResultListener(
            SelectionMapFragment.REQUEST_SELECT_ITEM,
            this
        ) { _: String?, result: Bundle ->
            if (result.containsKey(SelectionMapFragment.RESULT_SELECTED_ITEM)) {
                val instanceId = result.getLong(SelectionMapFragment.RESULT_SELECTED_ITEM)
                startActivity(FormFillingIntentFactory.editDraftFormIntent(this, projectsDataService.requireCurrentProject().uuid, instanceId))
            } else if (result.containsKey(SelectionMapFragment.RESULT_CREATE_NEW_ITEM)) {
                startActivity(FormFillingIntentFactory.newFormIntent(this, _root_ide_package_.com.yedc.android.external.FormsContract.getUri(projectsDataService.requireCurrentProject().uuid, formId)))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    companion object {
        const val EXTRA_FORM_ID = "form_id"
    }
}
