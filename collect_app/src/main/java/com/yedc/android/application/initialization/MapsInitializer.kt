package com.yedc.android.application.initialization

import android.content.Context
import android.os.Handler
import com.google.android.gms.maps.MapView
import com.yedc.osmdroid.OsmDroidInitializer
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.ProjectKeys
import timber.log.Timber
import javax.inject.Inject

class MapsInitializer @Inject constructor(
    private val context: Context,
    private val settingsProvider: SettingsProvider,
    private val userAgentProvider: _root_ide_package_.com.yedc.utilities.UserAgentProvider
) {

    fun initialize() {
        resetToAvailableFramework()

        if (!FRAMEWORKS_INITIALIZED) {
            initializeFrameworks()
        }
    }

    private fun resetToAvailableFramework() {
        _root_ide_package_.com.yedc.android.geo.MapConfiguratorProvider.initOptions(context)
        val availableBaseMaps = _root_ide_package_.com.yedc.android.geo.MapConfiguratorProvider.getIds()
        val baseMapSetting =
            settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_BASEMAP_SOURCE)
        if (!availableBaseMaps.contains(baseMapSetting)) {
            settingsProvider.getUnprotectedSettings().save(
                ProjectKeys.KEY_BASEMAP_SOURCE,
                availableBaseMaps[0]
            )
        }
    }

    private fun initializeFrameworks() {
        try {
            com.google.android.gms.maps.MapsInitializer.initialize(
                context,
                com.google.android.gms.maps.MapsInitializer.Renderer.LATEST
            ) { renderer: com.google.android.gms.maps.MapsInitializer.Renderer ->
                when (renderer) {
                    com.google.android.gms.maps.MapsInitializer.Renderer.LATEST -> Timber.d("The latest version of Google Maps renderer is used.")
                    com.google.android.gms.maps.MapsInitializer.Renderer.LEGACY -> Timber.d("The legacy version of Google Maps renderer is used.")
                }
            }
            val handler = Handler(context.mainLooper)
            handler.post {
                // This has to happen on the main thread but we might call `initialize` from tests
                MapView(context).onCreate(null)
            }
            OsmDroidInitializer.initialize(userAgentProvider.userAgent)
        } catch (ignore: Exception) {
            // ignored
        } catch (ignore: Error) {
            // ignored
        }
    }

    companion object {
        private var FRAMEWORKS_INITIALIZED = false
    }
}
