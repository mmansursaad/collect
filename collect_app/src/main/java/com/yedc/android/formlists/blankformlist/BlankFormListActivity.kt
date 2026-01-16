package com.yedc.android.formlists.blankformlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yedc.android.R
import com.yedc.android.activities.FormMapActivity
import com.yedc.android.formmanagement.FormFillingIntentFactory
import com.yedc.android.injection.DaggerUtils
import com.yedc.androidshared.ui.DialogFragmentUtils
import com.yedc.androidshared.ui.ObviousProgressBar
import com.yedc.androidshared.ui.SnackbarUtils
import com.yedc.async.network.NetworkStateProvider
import com.yedc.lists.EmptyListView
import com.yedc.lists.RecyclerViewUtils
import com.yedc.permissions.PermissionListener
import com.yedc.permissions.PermissionsProvider
import com.yedc.strings.localization.LocalizedActivity
import javax.inject.Inject

class BlankFormListActivity : LocalizedActivity(), OnFormItemClickListener {

    @Inject
    lateinit var viewModelFactory: BlankFormListViewModel.Factory

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    private val viewModel: BlankFormListViewModel by viewModels { viewModelFactory }

    private val adapter: BlankFormListAdapter = BlankFormListAdapter(this)

    private val formLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            setResult(RESULT_OK, it.data)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_blank_form_list)
        title = getString(com.yedc.strings.R.string.enter_data)
        setSupportActionBar(findViewById(com.yedc.androidshared.R.id.toolbar))

        val menuProvider = BlankFormListMenuProvider(this, viewModel, networkStateProvider)
        addMenuProvider(menuProvider, this)

        val list = findViewById<RecyclerView>(R.id.form_list)
        list.layoutManager = LinearLayoutManager(this)
        list.addItemDecoration(RecyclerViewUtils.verticalLineDivider(this))
        list.adapter = adapter

        initObservers()
    }

    override fun onFormClick(formUri: Uri) {
        if (Intent.ACTION_PICK == intent.action) {
            // caller is waiting on a picked form
            setResult(RESULT_OK, Intent().setData(formUri))
            finish()
        } else {
            // caller wants to view/edit a form, so launch FormFillingActivity
            formLauncher.launch(FormFillingIntentFactory.newFormIntent(this, formUri))
        }
    }

    override fun onMapButtonClick(id: Long) {
        permissionsProvider.requestEnabledLocationPermissions(
            this,
            object : PermissionListener {
                override fun granted() {
                    startActivity(
                        Intent(this@BlankFormListActivity, FormMapActivity::class.java).also {
                            it.putExtra(FormMapActivity.EXTRA_FORM_ID, id)
                        }
                    )
                }
            }
        )
    }

    private fun initObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                findViewById<ObviousProgressBar>(com.yedc.androidshared.R.id.progressBar).show()
            } else {
                findViewById<ObviousProgressBar>(com.yedc.androidshared.R.id.progressBar).hide()
            }
        }

        viewModel.syncResult.observe(this) { result ->
            if (result != null) {
                SnackbarUtils.showShortSnackbar(findViewById(R.id.form_list), result)
            }
        }

        viewModel.formsToDisplay.observe(this) { forms ->
            findViewById<RecyclerView>(R.id.form_list).visibility =
                if (forms.isEmpty()) View.GONE else View.VISIBLE

            findViewById<EmptyListView>(R.id.empty_list_message).visibility =
                if (forms.isEmpty()) View.VISIBLE else View.GONE

            adapter.setData(forms)
        }

        viewModel.isAuthenticationRequired().observe(this) { authenticationRequired ->
            if (authenticationRequired) {
                DialogFragmentUtils.showIfNotShowing(
                    _root_ide_package_.com.yedc.android.preferences.dialogs.ServerAuthDialogFragment::class.java,
                    supportFragmentManager
                )
            } else {
                DialogFragmentUtils.dismissDialog(
                    _root_ide_package_.com.yedc.android.preferences.dialogs.ServerAuthDialogFragment::class.java,
                    supportFragmentManager
                )
            }
        }
    }
}
