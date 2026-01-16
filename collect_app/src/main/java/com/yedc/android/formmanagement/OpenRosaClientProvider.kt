package com.yedc.android.formmanagement

import com.yedc.openrosa.forms.OpenRosaClient
import com.yedc.openrosa.parse.Kxml2OpenRosaResponseParser
import com.yedc.projects.ProjectDependencyFactory
import com.yedc.settings.keys.ProjectKeys
import com.yedc.shared.settings.Settings

class OpenRosaClientProvider(
    private val settingsFactory: ProjectDependencyFactory<Settings>,
    private val openRosaHttpInterface: com.yedc.openrosa.http.OpenRosaHttpInterface
) {

    fun create(projectId: String): OpenRosaClient {
        val settings = settingsFactory.create(projectId)
        val serverURL = settings.getString(ProjectKeys.KEY_SERVER_URL)!!

        return OpenRosaClient(
            serverURL,
            openRosaHttpInterface,
            _root_ide_package_.com.yedc.android.utilities.WebCredentialsUtils(settings),
            Kxml2OpenRosaResponseParser
        )
    }
}
