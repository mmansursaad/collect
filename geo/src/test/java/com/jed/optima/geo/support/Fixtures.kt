package com.jed.optima.geo.support

import android.R
import com.jed.optima.geo.selection.IconifiedText
import com.jed.optima.geo.selection.MappableSelectItem
import com.jed.optima.maps.MapPoint

object Fixtures {
    fun actionMappableSelectPoint(): MappableSelectItem.MappableSelectPoint {
        return MappableSelectItem.MappableSelectPoint(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            point = MapPoint(0.0, 0.0),
            smallIcon = R.drawable.ic_lock_power_off,
            largeIcon = R.drawable.ic_lock_idle_charging,
            action = IconifiedText(R.drawable.ic_delete, "Action")
        )
    }

    fun infoMappableSelectPoint(): MappableSelectItem.MappableSelectPoint {
        return MappableSelectItem.MappableSelectPoint(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            point = MapPoint(0.0, 0.0),
            smallIcon = R.drawable.ic_lock_power_off,
            largeIcon = R.drawable.ic_lock_idle_charging,
            info = "Info"
        )
    }

    fun actionMappableSelectLine(): MappableSelectItem.MappableSelectLine {
        return MappableSelectItem.MappableSelectLine(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            points = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0)),
            action = IconifiedText(R.drawable.ic_delete, "Action")
        )
    }

    fun actionMappableSelectPolygon(): MappableSelectItem.MappableSelectPolygon {
        return MappableSelectItem.MappableSelectPolygon(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            points = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0)),
            action = IconifiedText(R.drawable.ic_delete, "Action")
        )
    }
}
