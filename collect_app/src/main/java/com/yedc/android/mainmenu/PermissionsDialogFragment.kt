package com.yedc.android.mainmenu

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yedc.android.R
import com.yedc.permissions.PermissionListener
import com.yedc.permissions.PermissionsProvider

class PermissionsDialogFragment(
    private val permissionsProvider: PermissionsProvider,
    private val requestPermissionsViewModel: RequestPermissionsViewModel
) : DialogFragment() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(com.yedc.strings.R.string.permission_dialog_title)
            .setView(R.layout.permissions_dialog_layout)
            .setPositiveButton(com.yedc.strings.R.string.ok) { _, _ ->
                requestPermissionsViewModel.permissionsRequested()
                permissionsProvider.requestPermissions(
                    requireActivity(),
                    object : PermissionListener {
                        override fun granted() {}
                    },
                    *requestPermissionsViewModel.permissions
                )
            }
            .create()
    }
}
