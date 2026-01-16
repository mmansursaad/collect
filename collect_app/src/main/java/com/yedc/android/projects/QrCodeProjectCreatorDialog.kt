package com.yedc.android.projects

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.google.zxing.client.android.BeepManager
import com.yedc.analytics.Analytics
import com.yedc.android.R
import com.yedc.android.analytics.AnalyticsEvents
import com.yedc.android.databinding.QrCodeProjectCreatorDialogLayoutBinding
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.mainmenu.MainMenuActivity
import com.yedc.androidshared.system.IntentLauncher
import com.yedc.androidshared.ui.DialogFragmentUtils
import com.yedc.androidshared.ui.ToastUtils
import com.yedc.androidshared.ui.ToastUtils.showShortToast
import com.yedc.androidshared.ui.enableIconsVisibility
import com.yedc.androidshared.utils.CompressionUtils
import com.yedc.async.Scheduler
import com.yedc.permissions.PermissionListener
import com.yedc.permissions.PermissionsProvider
import com.yedc.projects.ProjectConfigurationResult
import com.yedc.projects.ProjectCreator
import com.yedc.projects.ProjectsRepository
import com.yedc.projects.SettingsConnectionMatcher
import com.yedc.qrcode.BarcodeScannerViewContainer
import com.yedc.qrcode.zxing.QRCodeDecoder
import com.yedc.settings.ODKAppSettingsImporter
import com.yedc.settings.SettingsProvider
import timber.log.Timber
import javax.inject.Inject

class QrCodeProjectCreatorDialog :
    com.yedc.material.MaterialFullScreenDialogFragment(),
    DuplicateProjectConfirmationDialog.DuplicateProjectConfirmationListener {

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var projectCreator: ProjectCreator

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var settingsProvider: SettingsProvider

    lateinit var settingsConnectionMatcher: SettingsConnectionMatcher

    private lateinit var beepManager: BeepManager
    lateinit var binding: QrCodeProjectCreatorDialogLayoutBinding

    @Inject
    lateinit var qrCodeDecoder: QRCodeDecoder

    @Inject
    lateinit var settingsImporter: ODKAppSettingsImporter

    @Inject
    lateinit var intentLauncher: IntentLauncher

    @Inject
    lateinit var barcodeScannerViewFactory: BarcodeScannerViewContainer.Factory

    @Inject
    lateinit var scheduler: Scheduler

    private var savedInstanceState: Bundle? = null

    private val imageQrCodeImportResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                permissionsProvider.requestReadUriPermission(
                    requireActivity(),
                    imageUri,
                    requireActivity().contentResolver,
                    object : PermissionListener {
                        override fun granted() {
                            // Do not call from a fragment that does not exist anymore https://github.com/getodk/collect/issues/4741
                            if (isAdded) {
                                requireActivity().contentResolver.openInputStream(imageUri).use {
                                    val settingsJson = try {
                                        qrCodeDecoder.decode(it)
                                    } catch (e: QRCodeDecoder.QRCodeInvalidException) {
                                        showShortToast(
                                            com.yedc.strings.R.string.invalid_qrcode
                                        )
                                        ""
                                    } catch (e: QRCodeDecoder.QRCodeNotFoundException) {
                                        showShortToast(
                                            com.yedc.strings.R.string.qr_code_not_found
                                        )
                                        ""
                                    }
                                    createProjectOrError(settingsJson)
                                }
                            }
                        }
                    }
                )
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        settingsConnectionMatcher =
            SettingsConnectionMatcherImpl(projectsRepository, settingsProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.savedInstanceState = savedInstanceState

        binding = QrCodeProjectCreatorDialogLayoutBinding.inflate(inflater)
        binding.toolbarLayout.toolbar.setTitle(com.yedc.strings.R.string.add_project)

        configureMenu()

        binding.configureManuallyButton.setOnClickListener {
            DialogFragmentUtils.showIfNotShowing(
                ManualProjectCreatorDialog::class.java,
                requireActivity().supportFragmentManager
            )
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        beepManager = BeepManager(requireActivity())

        binding.barcodeView.setup(
            barcodeScannerViewFactory,
            requireActivity(),
            viewLifecycleOwner,
            true
        )

        binding.barcodeView.barcodeScannerView.latestBarcode.observe(
            viewLifecycleOwner
        ) { result: String ->
            try {
                beepManager.playBeepSoundAndVibrate()
            } catch (e: Exception) {
                // ignore because beeping isn't essential and this can crash the whole app
            }

            val settingsJson = try {
                CompressionUtils.decompress(result)
            } catch (e: Exception) {
                showShortToast(
                    getString(com.yedc.strings.R.string.invalid_qrcode)
                )
                ""
            }
            createProjectOrError(settingsJson)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        permissionsProvider.requestCameraPermission(
            requireActivity(),
            object : PermissionListener {
                override fun granted() {
                    // Do not call from a fragment that does not exist anymore https://github.com/getodk/collect/issues/4741
                    if (isAdded) {
                        binding.barcodeView.barcodeScannerView.start()
                    }
                }
            }
        )
    }

    private fun configureMenu() {
        val toolbar = binding.toolbarLayout.toolbar
        toolbar.inflateMenu(R.menu.qr_code_scan_menu)

        val menu = toolbar.menu
        menu.enableIconsVisibility()

        menu.removeItem(R.id.menu_item_share)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_item_scan_sd_card -> {
                    val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                    photoPickerIntent.type = "image/*"
                    intentLauncher.launchForResult(
                        imageQrCodeImportResultLauncher,
                        photoPickerIntent
                    ) {
                        showShortToast(
                            getString(
                                com.yedc.strings.R.string.activity_not_found,
                                getString(com.yedc.strings.R.string.choose_image)
                            )
                        )
                        Timber.w(
                            getString(
                                com.yedc.strings.R.string.activity_not_found,
                                getString(com.yedc.strings.R.string.choose_image)
                            )
                        )
                    }
                }
            }
            false
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar? {
        return binding.toolbarLayout.toolbar
    }

    private fun createProjectOrError(settingsJson: String) {
        settingsConnectionMatcher.getProjectWithMatchingConnection(settingsJson)?.let { uuid ->
            val confirmationArgs = Bundle()
            confirmationArgs.putString(
                DuplicateProjectConfirmationKeys.SETTINGS_JSON,
                settingsJson
            )
            confirmationArgs.putString(DuplicateProjectConfirmationKeys.MATCHING_PROJECT, uuid)
            DialogFragmentUtils.showIfNotShowing(
                DuplicateProjectConfirmationDialog::class.java,
                confirmationArgs,
                childFragmentManager
            )
        } ?: run {
            createProject(settingsJson)
        }
    }

    override fun createProject(settingsJson: String) {
        when (projectCreator.createNewProject(settingsJson, true)) {
            ProjectConfigurationResult.SUCCESS -> {
                Analytics.log(AnalyticsEvents.QR_CREATE_PROJECT)

                _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
                ToastUtils.showLongToast(
                    getString(
                        com.yedc.strings.R.string.switched_project,
                        projectsDataService.requireCurrentProject().name
                    )
                )
            }

            ProjectConfigurationResult.INVALID_SETTINGS -> {
                ToastUtils.showLongToast(
                    getString(
                        com.yedc.strings.R.string.invalid_qrcode
                    )
                )

                restartScanning()
            }

            ProjectConfigurationResult.GD_PROJECT -> {
                ToastUtils.showLongToast(
                    getString(
                        com.yedc.strings.R.string.settings_with_gd_protocol
                    )
                )

                restartScanning()
            }
        }
    }

    private fun restartScanning() {
        scheduler.immediate(foreground = true, delay = 2000L) {
            binding.barcodeView.barcodeScannerView.start()
        }
    }

    override fun switchToProject(uuid: String) {
        projectsDataService.setCurrentProject(uuid)
        _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(
            getString(
                com.yedc.strings.R.string.switched_project,
                projectsDataService.requireCurrentProject().name
            )
        )
    }
}
