package com.yedc.osmdroid

import org.osmdroid.config.Configuration

object OsmDroidInitializer {

    fun initialize(userAgent: String) {
        Configuration.getInstance().userAgentValue = userAgent
    }
}
