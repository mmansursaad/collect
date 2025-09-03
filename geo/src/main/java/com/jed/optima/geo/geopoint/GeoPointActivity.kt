package com.jed.optima.geo.geopoint

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.jed.optima.analytics.Analytics
import com.jed.optima.androidshared.ui.DialogFragmentUtils
import com.jed.optima.externalapp.ExternalAppUtils
import com.jed.optima.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY
import com.jed.optima.geo.GeoDependencyComponentProvider
import com.jed.optima.geo.analytics.AnalyticsEvents
import com.jed.optima.strings.localization.LocalizedActivity
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
                ExternalAppUtils.returnSingleValue(this, _root_ide_package_.com.jed.optima.geo.GeoUtils.formatLocationResultString(it))
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
