package com.jed.optima.android.formmanagement

import com.jed.optima.openrosa.forms.OpenRosaClient
import com.jed.optima.openrosa.parse.Kxml2OpenRosaResponseParser
import com.jed.optima.projects.ProjectDependencyFactory
import com.jed.optima.settings.keys.ProjectKeys
import com.jed.optima.shared.settings.Settings

class OpenRosaClientProvider(
    private val settingsFactory: ProjectDependencyFactory<Settings>,
    private val openRosaHttpInterface: com.jed.optima.openrosa.http.OpenRosaHttpInterface
) {

    fun create(projectId: String): OpenRosaClient {
        val settings = settingsFactory.create(projectId)
        val serverURL = settings.getString(ProjectKeys.KEY_SERVER_URL)!!

        return OpenRosaClient(
            serverURL,
            openRosaHttpInterface,
            com.jed.optima.android.utilities.WebCredentialsUtils(settings),
            Kxml2OpenRosaResponseParser
        )
    }
}
