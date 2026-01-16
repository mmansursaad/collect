package com.yedc.android.fragments.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yedc.android.formmanagement.ServerFormDetails
import com.yedc.android.formmanagement.download.FormDownloadException
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.utilities.FormsDownloadResultInterpreter
import com.yedc.errors.ErrorActivity
import java.io.Serializable

class FormsDownloadResultDialog : DialogFragment() {
    private lateinit var result: Map<ServerFormDetails, FormDownloadException?>

    var listener: FormDownloadResultDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        if (context is FormDownloadResultDialogListener) {
            listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        result = arguments?.getSerializable(ARG_RESULT) as Map<ServerFormDetails, FormDownloadException?>

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setMessage(getMessage())
            .setPositiveButton(getString(com.yedc.strings.R.string.ok)) { _, _ ->
                listener?.onCloseDownloadingResult()
            }

        if (!FormsDownloadResultInterpreter.allFormsDownloadedSuccessfully(result)) {
            builder.setNegativeButton(getString(com.yedc.strings.R.string.show_details)) { _, _ ->
                val intent = Intent(context, ErrorActivity::class.java).apply {
                    putExtra(ErrorActivity.EXTRA_ERRORS, FormsDownloadResultInterpreter.getFailures(result, requireContext()) as Serializable)
                }
                startActivity(intent)
                listener?.onCloseDownloadingResult()
            }
        }

        return builder.create()
    }

    private fun getMessage(): String {
        return if (FormsDownloadResultInterpreter.allFormsDownloadedSuccessfully(result)) {
            getString(com.yedc.strings.R.string.all_downloads_succeeded)
        } else {
            getString(com.yedc.strings.R.string.some_downloads_failed, FormsDownloadResultInterpreter.getNumberOfFailures(result).toString(), result.size.toString())
        }
    }

    interface FormDownloadResultDialogListener {
        fun onCloseDownloadingResult()
    }

    companion object {
        const val ARG_RESULT = "RESULT"
    }
}
