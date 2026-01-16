package com.yedc.maps.markers

import com.yedc.maps.MapFragment
import com.yedc.maps.MapPoint

data class MarkerDescription(
    val point: MapPoint,
    val isDraggable: Boolean,
    @MapFragment.Companion.IconAnchor val iconAnchor: String,
    val iconDescription: MarkerIconDescription
)
