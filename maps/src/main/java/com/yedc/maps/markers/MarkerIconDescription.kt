package com.yedc.maps.markers

import com.yedc.androidshared.utils.toColorInt
import com.yedc.shared.strings.StringUtils
import java.util.Locale

class MarkerIconDescription @JvmOverloads constructor(
    val icon: Int,
    private val color: String? = null,
    private val symbol: String? = null
) {
    fun getColor(): Int? = color?.toColorInt()

    fun getSymbol(): String? = symbol?.let {
        if (it.isBlank()) {
            null
        } else {
            StringUtils.firstCharacterOrEmoji(it).uppercase(Locale.US)
        }
    }
}
