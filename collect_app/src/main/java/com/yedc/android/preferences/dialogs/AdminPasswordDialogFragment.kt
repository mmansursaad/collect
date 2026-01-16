package com.yedc.android.preferences.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.CompoundButton
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yedc.android.databinding.AdminPasswordDialogLayoutBinding
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.preferences.ProjectPreferencesViewModel
import com.yedc.android.utilities.SoftKeyboardController
import com.yedc.androidshared.ui.ToastUtils
import javax.inject.Inject

class AdminPasswordDialogFragment : DialogFragment() {
    @Inject
    lateinit var factory: ProjectPreferencesViewModel.Factory

    @Inject
    lateinit var adminPasswordProvider: _root_ide_package_.com.yedc.android.utilities.AdminPasswordProvider

    @Inject
    lateinit var softKeyboardController: SoftKeyboardController

    lateinit var binding: AdminPasswordDialogLayoutBinding

    lateinit var projectPreferencesViewModel: ProjectPreferencesViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        projectPreferencesViewModel = ViewModelProvider(requireActivity(), factory)[ProjectPreferencesViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AdminPasswordDialogLayoutBinding.inflate(LayoutInflater.from(context))

        binding.editText.post {
            softKeyboardController.showSoftKeyboard(binding.editText)
        }

        binding.checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                binding.editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                binding.editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle(getString(com.yedc.strings.R.string.enter_admin_password))
            .setPositiveButton(getString(com.yedc.strings.R.string.ok)) { _: DialogInterface?, _: Int ->
                if (adminPasswordProvider.adminPassword == binding.editText.text.toString()) {
                    projectPreferencesViewModel.setStateUnlocked()
                } else {
                    ToastUtils.showShortToast(
                        com.yedc.strings.R.string.admin_password_incorrect
                    )
                }
                dismiss()
            }
            .setNegativeButton(getString(com.yedc.strings.R.string.cancel)) { _: DialogInterface?, _: Int -> dismiss() }
            .create()
    }
}
