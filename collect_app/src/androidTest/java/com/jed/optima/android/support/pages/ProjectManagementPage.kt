package com.jed.optima.android.support.pages

class ProjectManagementPage : Page<ProjectManagementPage>() {

    override fun assertOnPage(): ProjectManagementPage {
        assertText(com.jed.optima.strings.R.string.project_management_section_title)
        return this
    }

    fun clickOnResetProject(): ProjectManagementPage {
        clickOnString(com.jed.optima.strings.R.string.reset_project_settings_title)
        return this
    }

    fun clickConfigureQR(): com.jed.optima.android.support.pages.QRCodePage {
        clickOnString(com.jed.optima.strings.R.string.reconfigure_with_qr_code_settings_title)
        return com.jed.optima.android.support.pages.QRCodePage().assertOnPage()
    }

    fun clickOnDeleteProject(): ProjectManagementPage {
        scrollToRecyclerViewItemAndClickText(com.jed.optima.strings.R.string.delete_project)
        return this
    }

    fun deleteProject(): com.jed.optima.android.support.pages.MainMenuPage {
        scrollToRecyclerViewItemAndClickText(com.jed.optima.strings.R.string.delete_project)
        clickOnString(com.jed.optima.strings.R.string.delete_project_yes)
        return com.jed.optima.android.support.pages.MainMenuPage()
    }

    fun deleteLastProject(): FirstLaunchPage {
        scrollToRecyclerViewItemAndClickText(com.jed.optima.strings.R.string.delete_project)
        clickOnString(com.jed.optima.strings.R.string.delete_project_yes)
        return FirstLaunchPage()
    }
}
