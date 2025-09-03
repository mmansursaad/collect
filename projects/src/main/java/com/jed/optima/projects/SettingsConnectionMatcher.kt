package com.jed.optima.projects

interface SettingsConnectionMatcher {
    fun getProjectWithMatchingConnection(settingsJson: String): String?
}
