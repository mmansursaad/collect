package com.yedc.android.formlists.savedformlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yedc.analytics.Analytics
import com.yedc.android.R
import com.yedc.android.analytics.AnalyticsEvents
import com.yedc.android.instancemanagement.isDeletable
import com.yedc.android.instancemanagement.isEdit
import com.yedc.androidshared.ui.FragmentFactoryBuilder
import com.yedc.androidshared.ui.SnackbarUtils
import com.yedc.androidshared.ui.SnackbarUtils.SnackbarPresenterObserver
import com.yedc.lists.RecyclerViewUtils
import com.yedc.lists.selects.MultiSelectControlsFragment
import com.yedc.lists.selects.MultiSelectListFragment
import com.yedc.lists.selects.MultiSelectViewModel
import com.yedc.lists.selects.SelectItem
import com.yedc.strings.R.string

class DeleteSavedFormFragment(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val menuHost: MenuHost? = null
) : Fragment() {

    private val savedFormListViewModel: SavedFormListViewModel by viewModels { viewModelFactory }
    private val multiSelectViewModel: MultiSelectViewModel<com.yedc.forms.instances.Instance> by viewModels {
        MultiSelectViewModel.Factory(
            savedFormListViewModel.formsToDisplay.map { instanceList ->
                val unsentEditsById = instanceList
                    .filter { it.isEdit() }
                    .groupBy { it.editOf }

                instanceList
                    .filter { it.isDeletable() }
                    .filter { instance ->
                        if (instance.status == com.yedc.forms.instances.Instance.STATUS_SUBMITTED) {
                            true
                        } else {
                            val originalId = instance.editOf ?: instance.dbId

                            unsentEditsById[originalId].orEmpty().none { edit ->
                                edit.editNumber!! > (instance.editNumber ?: 0)
                            }
                        }
                    }
                    .map { instance ->
                        SelectItem(
                            instance.dbId.toString(),
                            instance
                        )
                    }
            }
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(MultiSelectListFragment::class) {
                MultiSelectListFragment(
                    getString(string.delete_file),
                    multiSelectViewModel,
                    ::SelectableSavedFormListItemViewHolder
                ) {
                    it.empty.setIcon(R.drawable.ic_baseline_delete_72)
                    it.empty.setTitle(getString(string.empty_list_of_forms_to_delete_title))
                    it.empty.setSubtitle(getString(string.empty_list_of_saved_forms_to_delete_subtitle))

                    it.list.addItemDecoration(RecyclerViewUtils.verticalLineDivider(context))
                }
            }
            .build()

        childFragmentManager.setFragmentResultListener(
            MultiSelectControlsFragment.REQUEST_ACTION,
            this
        ) { _, result ->
            val selected = result.getStringArray(MultiSelectControlsFragment.RESULT_SELECTED)!!
            onDeleteSelected(selected.map { it.toLong() }.toLongArray())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.delete_form_layout,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        menuHost?.addMenuProvider(
            SavedFormListListMenuProvider(requireContext(), savedFormListViewModel),
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        com.yedc.material.MaterialProgressDialogFragment.showOn(
            viewLifecycleOwner,
            savedFormListViewModel.isDeleting,
            childFragmentManager
        ) {
            com.yedc.material.MaterialProgressDialogFragment().also {
                it.message = getString(string.form_delete_message)
            }
        }
    }

    private fun onDeleteSelected(selected: LongArray) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(string.delete_file)
            .setMessage(
                getString(
                    string.delete_confirm,
                    selected.size.toString()
                )
            )
            .setPositiveButton(getString(string.delete_yes)) { _, _ ->
                logDelete(selected.size)

                multiSelectViewModel.unselectAll()
                savedFormListViewModel.deleteForms(selected).observe(
                    viewLifecycleOwner,
                    object : SnackbarPresenterObserver<Int>(requireView()) {
                        override fun getSnackbarDetails(value: Int): SnackbarUtils.SnackbarDetails {
                            return SnackbarUtils.SnackbarDetails(
                                getString(
                                    string.file_deleted_ok,
                                    value.toString()
                                )
                            )
                        }
                    }
                )
            }
            .setNegativeButton(getString(string.delete_no), null)
            .show()
    }

    private fun logDelete(size: Int) {
        val event = when {
            size >= 100 -> AnalyticsEvents.DELETE_SAVED_FORM_HUNDREDS
            size >= 10 -> AnalyticsEvents.DELETE_SAVED_FORM_TENS
            else -> AnalyticsEvents.DELETE_SAVED_FORM_FEW
        }

        Analytics.log(event)
    }
}
