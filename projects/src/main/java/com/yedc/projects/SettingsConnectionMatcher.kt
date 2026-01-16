package com.yedc.projects

interface SettingsConnectionMatcher {
    fun getProjectWithMatchingConnection(settingsJson: String): String?
}
