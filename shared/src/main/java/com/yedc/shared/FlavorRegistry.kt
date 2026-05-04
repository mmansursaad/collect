package com.yedc.shared

/**
 * A central hub for flavor-specific data.
 * The App module will fill these values on startup.
 */
object FlavorRegistry {
    var deviceIdPrefix: String = "default"
    var offlineLayersUrl: String = "https://default.url"
    var qrFolderUrl: String = "https://default.url"
    var idPrefix: String = "default"
    var smallIcon: Int = 0
    var contentProviderAuthority: String = "default.provider"
}