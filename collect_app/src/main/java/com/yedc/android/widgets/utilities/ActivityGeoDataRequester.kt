package com.yedc.android.widgets.utilities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.javarosa.form.api.FormEntryPrompt
import com.yedc.android.utilities.Appearances
import com.yedc.android.widgets.interfaces.GeoDataRequester
import com.yedc.geo.Constants.EXTRA_DRAGGABLE_ONLY
import com.yedc.geo.Constants.EXTRA_READ_ONLY
import com.yedc.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY
import com.yedc.geo.geopoint.GeoPointActivity
import com.yedc.permissions.PermissionListener
import com.yedc.permissions.PermissionsProvider
import java.lang.Boolean.parseBoolean

class ActivityGeoDataRequester(
    private val permissionsProvider: PermissionsProvider,
    private val activity: Activity
) : GeoDataRequester {

    override fun requestGeoPoint(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: _root_ide_package_.com.yedc.android.widgets.utilities.WaitingForDataRegistry
    ) {
        permissionsProvider.requestEnabledLocationPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    waitingForDataRegistry.waitForData(prompt.index)

                    val bundle = Bundle().also {
                        val parsedGeometry = GeoWidgetUtils.parseGeometry(answerText)
                        if (parsedGeometry.isNotEmpty()) {
                            it.putParcelable(
                                com.yedc.geo.geopoint.GeoPointMapActivity.EXTRA_LOCATION,
                                parsedGeometry[0]
                            )
                        }

                        val accuracyThreshold =
                            _root_ide_package_.com.yedc.android.utilities.FormEntryPromptUtils.getAdditionalAttribute(prompt, "accuracyThreshold")
                        val unacceptableAccuracyThreshold =
                            _root_ide_package_.com.yedc.android.utilities.FormEntryPromptUtils.getAdditionalAttribute(
                                prompt,
                                "unacceptableAccuracyThreshold"
                            )

                        it.putFloat(
                            GeoPointActivity.EXTRA_ACCURACY_THRESHOLD,
                            accuracyThreshold?.toFloatOrNull() ?: DEFAULT_ACCURACY_THRESHOLD
                        )

                        it.putFloat(
                            GeoPointActivity.EXTRA_UNACCEPTABLE_ACCURACY_THRESHOLD,
                            unacceptableAccuracyThreshold?.toFloatOrNull()
                                ?: DEFAULT_UNACCEPTABLE_ACCURACY_THRESHOLD
                        )

                        it.putBoolean(EXTRA_RETAIN_MOCK_ACCURACY, getAllowMockAccuracy(prompt))
                        it.putBoolean(EXTRA_READ_ONLY, prompt.isReadOnly)
                        it.putBoolean(EXTRA_DRAGGABLE_ONLY, hasPlacementMapAppearance(prompt))
                    }

                    val intent = Intent(
                        activity,
                        if (isMapsAppearance(prompt)) com.yedc.geo.geopoint.GeoPointMapActivity::class.java else GeoPointActivity::class.java
                    ).also {
                        it.putExtras(bundle)
                    }

                    activity.startActivityForResult(
                        intent,
                        _root_ide_package_.com.yedc.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE
                    )
                }
            }
        )
    }

    override fun requestGeoShape(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: _root_ide_package_.com.yedc.android.widgets.utilities.WaitingForDataRegistry
    ) {
        permissionsProvider.requestEnabledLocationPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    waitingForDataRegistry.waitForData(prompt.index)

                    val intent = Intent(activity, com.yedc.geo.geopoly.GeoPolyActivity::class.java).also {
                        it.putExtra(
                            com.yedc.geo.geopoly.GeoPolyActivity.EXTRA_POLYGON,
                            GeoWidgetUtils.parseGeometry(answerText)
                        )
                        it.putExtra(
                            com.yedc.geo.geopoly.GeoPolyActivity.OUTPUT_MODE_KEY,
                            com.yedc.geo.geopoly.GeoPolyActivity.OutputMode.GEOSHAPE
                        )
                        it.putExtra(EXTRA_READ_ONLY, prompt.isReadOnly)
                        it.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, getAllowMockAccuracy(prompt))
                    }

                    activity.startActivityForResult(
                        intent,
                        _root_ide_package_.com.yedc.android.utilities.ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE
                    )
                }
            }
        )
    }

    override fun requestGeoTrace(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: _root_ide_package_.com.yedc.android.widgets.utilities.WaitingForDataRegistry
    ) {
        permissionsProvider.requestEnabledLocationPermissions(
            activity,
            object : PermissionListener {
                override fun granted() {
                    waitingForDataRegistry.waitForData(prompt.index)

                    val intent = Intent(activity, com.yedc.geo.geopoly.GeoPolyActivity::class.java).also {
                        it.putExtra(
                            com.yedc.geo.geopoly.GeoPolyActivity.EXTRA_POLYGON,
                            GeoWidgetUtils.parseGeometry(answerText)
                        )
                        it.putExtra(
                            com.yedc.geo.geopoly.GeoPolyActivity.OUTPUT_MODE_KEY,
                            com.yedc.geo.geopoly.GeoPolyActivity.OutputMode.GEOTRACE
                        )
                        it.putExtra(EXTRA_READ_ONLY, prompt.isReadOnly)
                        it.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, getAllowMockAccuracy(prompt))
                    }

                    activity.startActivityForResult(
                        intent,
                        _root_ide_package_.com.yedc.android.utilities.ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE
                    )
                }
            }
        )
    }

    private fun getAllowMockAccuracy(prompt: FormEntryPrompt): Boolean {
        return parseBoolean(
            _root_ide_package_.com.yedc.android.utilities.FormEntryPromptUtils.getBindAttribute(
                prompt,
                "allow-mock-accuracy"
            )
        )
    }

    private fun isMapsAppearance(prompt: FormEntryPrompt): Boolean {
        return hasMapsAppearance(prompt) || hasPlacementMapAppearance(prompt)
    }

    private fun hasMapsAppearance(prompt: FormEntryPrompt): Boolean {
        return Appearances.hasAppearance(prompt, Appearances.MAPS)
    }

    private fun hasPlacementMapAppearance(prompt: FormEntryPrompt): Boolean {
        return Appearances.hasAppearance(prompt, Appearances.PLACEMENT_MAP)
    }

    companion object {
        const val DEFAULT_ACCURACY_THRESHOLD = 5f
        const val DEFAULT_UNACCEPTABLE_ACCURACY_THRESHOLD = 100f
    }
}
