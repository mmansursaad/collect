package com.yedc.settings

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.yedc.projects.InMemProjectsRepository
import com.yedc.projects.Project
import com.yedc.projects.ProjectConfigurationResult
import com.yedc.settings.importing.SettingsChangeHandler
import com.yedc.settings.support.SettingsUtils.assertSettingsEmpty
import java.lang.RuntimeException

class ODKAppSettingsImporterTest {
    private val projectsRepository = InMemProjectsRepository()
    private val settingsProvider = InMemSettingsProvider()
    private val settingsChangeHandler = mock<SettingsChangeHandler>()

    private val settingsImporter = ODKAppSettingsImporter(
        projectsRepository,
        settingsProvider,
        mapOf("server_url" to "https://demo.getodk.org"),
        emptyMap(),
        listOf("#00000"),
        settingsChangeHandler,
        JSONObject()
    )

    @Test
    fun `accepts valid JSON`() {
        val result = settingsImporter.fromJSON(
            "{\n" +
                "  \"general\": {\n" +
                "  },\n" +
                "  \"admin\": {\n" +
                "  }\n" +
                "}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(ProjectConfigurationResult.SUCCESS))
    }

    @Test
    fun `rejects JSON without general object`() {
        val result = settingsImporter.fromJSON(
            "{ \"admin\": {}}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(ProjectConfigurationResult.INVALID_SETTINGS))
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
        assertSettingsEmpty(settingsProvider.getProtectedSettings())
    }

    @Test
    fun `rejects JSON without admin object`() {
        val result = settingsImporter.fromJSON(
            "{ \"general\": {}}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(ProjectConfigurationResult.INVALID_SETTINGS))
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
        assertSettingsEmpty(settingsProvider.getProtectedSettings())
    }

    @Test
    fun `rejects invalid JSON`() {
        val result = settingsImporter.fromJSON(
            "{\"general\":{*},\"admin\":{}}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(ProjectConfigurationResult.INVALID_SETTINGS))
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
        assertSettingsEmpty(settingsProvider.getProtectedSettings())
    }

    @Test
    fun `rejects JSON when exception is thrown during importing`() {
        whenever(settingsChangeHandler.onSettingsChanged(any(), any(), any())).thenThrow(RuntimeException::class.java)

        val result = settingsImporter.fromJSON(
            "{\n" +
                "  \"general\": {\n" +
                "  },\n" +
                "  \"admin\": {\n" +
                "  }\n" +
                "}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(ProjectConfigurationResult.INVALID_SETTINGS))
    }

    @Test
    fun `rejects JSON with google_sheets protocol`() {
        val result = settingsImporter.fromJSON(
            "{\n" +
                "  \"general\": {\n" +
                "       \"protocol\" : \"google_sheets\"" +
                "  },\n" +
                "  \"admin\": {\n" +
                "  }\n" +
                "}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(ProjectConfigurationResult.GD_PROJECT))
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
        assertSettingsEmpty(settingsProvider.getProtectedSettings())
    }
}
