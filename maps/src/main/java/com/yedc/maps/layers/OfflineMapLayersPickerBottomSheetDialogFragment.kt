package com.yedc.maps.layers

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yedc.androidshared.ui.DialogFragmentUtils
import com.yedc.androidshared.ui.FragmentFactoryBuilder
import com.yedc.androidshared.ui.addOnClickListener
import com.yedc.async.Scheduler
import com.yedc.lists.selects.MultiSelectViewModel
import com.yedc.lists.selects.SelectItem
import com.yedc.lists.selects.SingleSelectViewModel
import com.yedc.maps.databinding.OfflineMapLayersPickerBinding
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.ProjectKeys
import com.yedc.strings.localization.getLocalizedString

class OfflineMapLayersPickerBottomSheetDialogFragment(
    registry: ActivityResultRegistry,
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val scheduler: Scheduler,
    private val settingsProvider: SettingsProvider,
    private val externalWebPageHelper: _root_ide_package_.com.yedc.webpage.ExternalWebPageHelper
) : BottomSheetDialogFragment(),
    OfflineMapLayersPickerAdapter.OfflineMapLayersPickerAdapterInterface {

    private val sharedViewModel: OfflineMapLayersViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineMapLayersViewModel(
                    referenceLayerRepository,
                    scheduler,
                    settingsProvider
                ) as T
            }
        }
    }
    private val expandedStateViewModel: MultiSelectViewModel<*> by viewModels {
        MultiSelectViewModel.Factory<Any>()
    }

    private val checkedStateViewModel: SingleSelectViewModel by viewModels {
        viewModelFactory {
            addInitializer(SingleSelectViewModel::class) {
                SingleSelectViewModel(
                    settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER),
                    sharedViewModel.existingLayers.map {
                        it.map { layer ->
                            SelectItem(layer.id, layer)
                        }
                    }
                )
            }
        }
    }

    private val getLayers = registerForActivityResult(ActivityResultContracts.GetMultipleContents(), registry) { uris ->
        if (uris.isNotEmpty()) {
            sharedViewModel.loadLayersToImport(uris, requireContext())
            DialogFragmentUtils.showIfNotShowing(
                OfflineMapLayersImporterDialogFragment::class.java,
                childFragmentManager
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedViewModel.loadExistingLayers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(OfflineMapLayersImporterDialogFragment::class) {
                OfflineMapLayersImporterDialogFragment(referenceLayerRepository, scheduler, settingsProvider)
            }
            .build()

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = OfflineMapLayersPickerBinding.inflate(inflater)

        binding.mbtilesInfoGroup.addOnClickListener {
            externalWebPageHelper.openWebPageInCustomTab(
                requireActivity(),
                Uri.parse("https://drive.google.com/drive/u/0/folders/1JWG_0lqtIjmSWau2_EAoJmHy6nwLRldm"
                )
            )
        }

        binding.addLayer.setOnClickListener {
            getLayers.launch("*/*")
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }

        binding.save.setOnClickListener {
            sharedViewModel.saveCheckedLayer(checkedStateViewModel.getSelected().value)
            dismiss()
        }

        if (sharedViewModel.layersToImport.value?.value == null) {
            DialogFragmentUtils.dismissDialog(
                OfflineMapLayersImporterDialogFragment::class.java,
                childFragmentManager
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = OfflineMapLayersPickerBinding.bind(view)

        sharedViewModel.trackableWorker.isWorking.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressIndicator.visibility = View.VISIBLE
                binding.layers.visibility = View.GONE
                binding.save.isEnabled = false
            } else {
                binding.progressIndicator.visibility = View.GONE
                binding.layers.visibility = View.VISIBLE
                binding.save.isEnabled = true
            }
        }

        val adapter = OfflineMapLayersPickerAdapter(this)
        binding.layers.setAdapter(adapter)
        _root_ide_package_.com.yedc.androidshared.livedata.LiveDataUtils.zip3(
            sharedViewModel.existingLayers,
            checkedStateViewModel.getSelected(),
            expandedStateViewModel.getSelected()
        ).observe(this) { (layers, checkedLayerId, expandedLayerIds) ->
            updateAdapter(layers, checkedLayerId, expandedLayerIds.toList(), adapter)
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            BottomSheetBehavior.from(requireView().parent as View).apply {
                maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onLayerChecked(layerId: String?) {
        if (layerId != null) {
            checkedStateViewModel.select(layerId)
        } else {
            checkedStateViewModel.clear()
        }
    }

    override fun onLayerToggled(layerId: String) {
        expandedStateViewModel.toggle(layerId)
    }

    override fun onDeleteLayer(layerItem: CheckableReferenceLayer) {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(
                requireActivity().getLocalizedString(
                    com.yedc.strings.R.string.delete_layer_confirmation_message,
                    layerItem.name
                )
            )
            .setPositiveButton(com.yedc.strings.R.string.delete_layer) { _, _ ->
                sharedViewModel.deleteLayer(layerItem.id!!)
            }
            .setNegativeButton(com.yedc.strings.R.string.cancel, null)
            .create()
            .show()
    }

    private fun updateAdapter(
        layers: List<ReferenceLayer>?,
        checkedLayerId: String?,
        expandedLayerIds: List<String?>,
        adapter: OfflineMapLayersPickerAdapter
    ) {
        if (layers == null) {
            return
        }

        val newData = mutableListOf(
            CheckableReferenceLayer(
                null,
                null,
                requireContext().getLocalizedString(com.yedc.strings.R.string.none),
                checkedLayerId == null,
                false
            )
        )

        newData.addAll(
            layers.map {
                CheckableReferenceLayer(
                    it.id,
                    it.file,
                    it.name,
                    checkedLayerId == it.id,
                    expandedLayerIds.contains(it.id)
                )
            }
        )
        adapter.setData(newData)
    }
}
