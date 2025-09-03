/*
 * Copyright (C) 2017 Shobhit
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
package com.jed.optima.android.preferences.screens

import android.content.Context
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.jed.optima.android.R
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.androidshared.ui.DialogFragmentUtils
import com.jed.optima.androidshared.ui.FragmentFactoryBuilder
import com.jed.optima.androidshared.ui.PrefUtils
import com.jed.optima.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick
import com.jed.optima.async.Scheduler
import com.jed.optima.maps.MapConfigurator
import com.jed.optima.maps.layers.OfflineMapLayersPickerBottomSheetDialogFragment
import com.jed.optima.maps.layers.ReferenceLayerRepository
import com.jed.optima.settings.keys.ProjectKeys
import com.jed.optima.settings.keys.ProjectKeys.CATEGORY_BASEMAP
import com.jed.optima.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE
import com.jed.optima.strings.localization.getLocalizedString
import javax.inject.Inject

class MapsPreferencesFragment : com.jed.optima.android.preferences.screens.BaseProjectPreferencesFragment(), Preference.OnPreferenceClickListener {

    private lateinit var basemapSourcePref: ListPreference

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var externalWebPageHelper: com.jed.optima.webpage.ExternalWebPageHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(OfflineMapLayersPickerBottomSheetDialogFragment::class) {
                OfflineMapLayersPickerBottomSheetDialogFragment(requireActivity().activityResultRegistry, referenceLayerRepository, scheduler, settingsProvider, externalWebPageHelper)
            }
            .build()

        super.onCreate(savedInstanceState)
    }

    override fun onSettingChanged(key: String) {
        super.onSettingChanged(key)
        if (key == ProjectKeys.KEY_REFERENCE_LAYER) {
            findPreference<Preference>(ProjectKeys.KEY_REFERENCE_LAYER)!!.summary = getLayerName()
        } else if (key == KEY_BASEMAP_SOURCE) {
            val cftor = com.jed.optima.android.geo.MapConfiguratorProvider.getConfigurator(settingsProvider.getUnprotectedSettings().getString(key))
            if (!cftor.isAvailable(requireContext())) {
                cftor.showUnavailableMessage(requireContext())
            } else {
                onBasemapSourceChanged(cftor)
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.maps_preferences, rootKey)
        initBasemapSourcePref()
        initLayersPref()
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (allowClick(javaClass.name)) {
            when (preference.key) {
                ProjectKeys.KEY_REFERENCE_LAYER -> {
                    DialogFragmentUtils.showIfNotShowing(
                        OfflineMapLayersPickerBottomSheetDialogFragment::class.java,
                        childFragmentManager
                    )
                }
            }
            return true
        }
        return false
    }

    /**
     * Creates the Basemap Source preference widget (but doesn't add it to
     * the screen; onBasemapSourceChanged will do that part).
     */
    private fun initBasemapSourcePref() {
        basemapSourcePref = PrefUtils.createListPref(
            requireContext(),
            KEY_BASEMAP_SOURCE,
            getString(com.jed.optima.strings.R.string.basemap_source),
            com.jed.optima.android.geo.MapConfiguratorProvider.getLabelIds(),
            com.jed.optima.android.geo.MapConfiguratorProvider.getIds(),
            settingsProvider.getUnprotectedSettings()
        )

        basemapSourcePref.setIconSpaceReserved(false)
        onBasemapSourceChanged(com.jed.optima.android.geo.MapConfiguratorProvider.getConfigurator())
    }

    private fun initLayersPref() {
        findPreference<Preference>(ProjectKeys.KEY_REFERENCE_LAYER)?.apply {
            onPreferenceClickListener = this@MapsPreferencesFragment
            summary = getLayerName()
        }
    }

    private fun getLayerName(): String {
        val layerId = settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER)
        return if (layerId == null) {
            requireContext().getLocalizedString(com.jed.optima.strings.R.string.none)
        } else {
            referenceLayerRepository.get(layerId)!!.name
        }
    }

    /** Updates the rest of the preference UI when the Basemap Source is changed.  */
    private fun onBasemapSourceChanged(cftor: MapConfigurator) {
        // Set up the preferences in the "Basemap" section.
        val baseCategory = findPreference<PreferenceCategory>(CATEGORY_BASEMAP)
        baseCategory!!.removeAll()
        baseCategory.addPreference(basemapSourcePref)
        for (pref in cftor.createPrefs(requireContext(), settingsProvider.getUnprotectedSettings())) {
            pref.isIconSpaceReserved = false
            baseCategory.addPreference(pref)
        }

        // Clear the reference layer if it does not exist or it isn't supported by the new basemap.
        val layerId = settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER)
        if (layerId != null) {
            val layer = referenceLayerRepository.get(layerId)
            if (layer == null) {
                settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, null)
            }
        }
    }
}
