package com.yedc.android.support.pages

class ProjectManagementPage : Page<ProjectManagementPage>() {

    override fun assertOnPage(): ProjectManagementPage {
        assertText(com.yedc.strings.R.string.project_management_section_title)
        return this
    }

    fun clickOnResetProject(): ProjectManagementPage {
        clickOnString(com.yedc.strings.R.string.reset_project_settings_title)
        return this
    }

    fun clickConfigureQR(): com.yedc.android.support.pages.QRCodePage {
        clickOnString(com.yedc.strings.R.string.reconfigure_with_qr_code_settings_title)
        return com.yedc.android.support.pages.QRCodePage().assertOnPage()
    }

    fun clickOnDeleteProject(): ProjectManagementPage {
        scrollToRecyclerViewItemAndClickText(com.yedc.strings.R.string.delete_project)
        return this
    }

    fun deleteProject(): com.yedc.android.support.pages.MainMenuPage {
        scrollToRecyclerViewItemAndClickText(com.yedc.strings.R.string.delete_project)
        clickOnString(com.yedc.strings.R.string.delete_project_yes)
        return com.yedc.android.support.pages.MainMenuPage()
    }

    fun deleteLastProject(): FirstLaunchPage {
        scrollToRecyclerViewItemAndClickText(com.yedc.strings.R.string.delete_project)
        clickOnString(com.yedc.strings.R.string.delete_project_yes)
        return FirstLaunchPage()
    }
}
