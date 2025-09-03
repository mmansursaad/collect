package com.jed.optima.maps.layers

import android.os.Bundle
import com.jed.optima.maps.MapFragment
import java.io.File

object MapFragmentReferenceLayerUtils {

    @JvmStatic
    fun getReferenceLayerFile(
        config: Bundle,
        layerRepository: ReferenceLayerRepository
    ): File? {
        val filePath = config.getString(MapFragment.KEY_REFERENCE_LAYER)
        return if (filePath != null) {
            val referenceLayer = layerRepository.get(filePath)
            referenceLayer?.file
        } else {
            null
        }
    }
}
