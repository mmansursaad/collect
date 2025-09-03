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
package com.jed.optima.android.external

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jed.optima.android.R
import com.jed.optima.android.formlists.blankformlist.BlankFormListItem
import com.jed.optima.android.formlists.blankformlist.BlankFormListViewModel
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.settings.SettingsProvider
import javax.inject.Inject

/**
 * Allows the user to create desktop shortcuts to any form currently available to Collect
 */
class AndroidShortcutsActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: BlankFormListViewModel.Factory

    @Inject
    lateinit var settingsProvider: SettingsProvider

    private val viewModel: BlankFormListViewModel by viewModels { viewModelFactory }

    private var dialog: AlertDialog? = null

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        DaggerUtils.getComponent(this).inject(this)

        viewModel.formsToDisplay.observe(this) { forms ->
            showFormListDialog(forms)
        }
    }

    private fun showFormListDialog(blankFormListItems: List<BlankFormListItem>) {
        dialog?.dismiss()
        dialog = MaterialAlertDialogBuilder(this)
            .setTitle(com.jed.optima.strings.R.string.select_odk_shortcut)
            .setItems(
                blankFormListItems
                    .map { it.formName }
                    .toTypedArray()
            ) { _: DialogInterface?, item: Int ->
                val intent = getShortcutIntent(blankFormListItems, item)
                setResult(RESULT_OK, intent)
                finish()
            }
            .setOnCancelListener {
                setResult(RESULT_CANCELED)
                finish()
            }
            .create()
            .also {
                it.show()
            }
    }

    private fun getShortcutIntent(forms: List<BlankFormListItem>, item: Int): Intent {
        val shortcutIntent = Intent(Intent.ACTION_EDIT).apply {
            data = forms[item].contentUri
        }

        return Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, forms[item].formName)
            val iconResource: Parcelable =
                Intent.ShortcutIconResource.fromContext(this@AndroidShortcutsActivity, R.drawable.notes)
            putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
        }
    }
}
