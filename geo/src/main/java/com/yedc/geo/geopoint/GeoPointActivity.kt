package com.yedc.geo.geopoint

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.yedc.analytics.Analytics
import com.yedc.androidshared.ui.DialogFragmentUtils
import com.yedc.externalapp.ExternalAppUtils
import com.yedc.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY
import com.yedc.geo.GeoDependencyComponentProvider
import com.yedc.geo.analytics.AnalyticsEvents
import com.yedc.strings.localization.LocalizedActivity
import javax.inject.Inject

class GeoPointActivity : LocalizedActivity(), GeoPointDialogFragment.Listener {

    @Inject
    internal lateinit var geoPointViewModelFactory: GeoPointViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as GeoDependencyComponentProvider).geoDependencyComponent.inject(this)

        val viewModel =
            ViewModelProvider(this, geoPointViewModelFactory).get(GeoPointViewModel::class.java)

        viewModel.acceptedLocation.observe(this) {
            if (it != null) {
                Analytics.log(AnalyticsEvents.SAVE_POINT_AUTO)
                ExternalAppUtils.returnSingleValue(this, _root_ide_package_.com.yedc.geo.GeoUtils.formatLocationResultString(it))
            }
        }

        DialogFragmentUtils.showIfNotShowing(
            GeoPointDialogFragment::class.java,
            supportFragmentManager
        )

        viewModel.start(
            retainMockAccuracy = this.intent.getBooleanExtra(
                EXTRA_RETAIN_MOCK_ACCURACY,
                false
            ),
            accuracyThreshold = (this.intent.extras?.get(EXTRA_ACCURACY_THRESHOLD) as? Float),
            unacceptableAccuracyThreshold = (
                this.intent.extras?.get(
                    EXTRA_UNACCEPTABLE_ACCURACY_THRESHOLD
                ) as? Float
            )
        )
    }

    override fun onCancel() {
        finish()
    }

    companion object {
        const val EXTRA_ACCURACY_THRESHOLD = "extra_accuracy_threshold"
        const val EXTRA_UNACCEPTABLE_ACCURACY_THRESHOLD = "extra_unacceptable_accuracy_threshold"
    }
}
