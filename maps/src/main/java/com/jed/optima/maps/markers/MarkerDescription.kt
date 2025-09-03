package com.jed.optima.maps.markers

import com.jed.optima.maps.MapFragment
import com.jed.optima.maps.MapPoint

data class MarkerDescription(
    val point: MapPoint,
    val isDraggable: Boolean,
    @MapFragment.Companion.IconAnchor val iconAnchor: String,
    val iconDescription: MarkerIconDescription
)
