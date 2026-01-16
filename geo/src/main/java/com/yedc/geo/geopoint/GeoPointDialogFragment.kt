package com.yedc.geo.geopoint

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yedc.geo.GeoDependencyComponentProvider
import com.yedc.geo.GeoUtils.formatAccuracy
import com.yedc.geo.databinding.GeopointDialogBinding
import javax.inject.Inject

class GeoPointDialogFragment : DialogFragment() {

    @Inject
    internal lateinit var geoPointViewModelFactory: GeoPointViewModelFactory

    var listener: Listener? = null

    lateinit var binding: GeopointDialogBinding
    private lateinit var viewModel: GeoPointViewModel

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                listener?.onCancel()
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val component =
            (context.applicationContext as GeoDependencyComponentProvider).geoDependencyComponent
        component.inject(this)

        listener = context as? Listener

        viewModel =
            ViewModelProvider(
                requireActivity(),
                geoPointViewModelFactory
            ).get(GeoPointViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = GeopointDialogBinding.inflate(LayoutInflater.from(context))

        val accuracyThreshold = viewModel.accuracyThreshold

        viewModel.currentAccuracy.observe(this) {
            binding.accuracyStatus.accuracy = it
        }

        binding.threshold.text =
            getString(com.yedc.strings.R.string.point_will_be_saved, formatAccuracy(context, accuracyThreshold))

        viewModel.timeElapsed.observe(this) {
            binding.time.text =
                getString(com.yedc.strings.R.string.time_elapsed, DateUtils.formatElapsedTime(it / 1000))
        }

        viewModel.satellites.observe(this) {
            binding.satellites.text = getString(com.yedc.strings.R.string.satellites, it.toString())
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton(com.yedc.strings.R.string.save) { _, _ -> viewModel.forceLocation() }
            .setNegativeButton(com.yedc.strings.R.string.cancel) { _, _ -> listener?.onCancel() }
            .create()

        dialog.setOnShowListener {
            viewModel.currentAccuracy.observe(this) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = it != null
            }
        }

        isCancelable = false

        (dialog as ComponentDialog).onBackPressedDispatcher.addCallback(onBackPressedCallback)
        return dialog
    }

    interface Listener {
        fun onCancel()
    }
}
