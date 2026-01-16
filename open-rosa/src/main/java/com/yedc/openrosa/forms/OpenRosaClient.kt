package com.yedc.openrosa.forms

import android.net.Uri
import com.yedc.entities.server.EntitySource
import com.yedc.forms.FormListItem
import com.yedc.forms.FormSourceException
import com.yedc.forms.FormSourceException.AuthRequired
import com.yedc.forms.FormSourceException.FetchError
import com.yedc.forms.FormSourceException.SecurityError
import com.yedc.forms.FormSourceException.ServerNotOpenRosaError
import com.yedc.forms.ManifestFile
import com.yedc.openrosa.forms.OpenRosaXmlFetcher.WebCredentialsProvider
import com.yedc.openrosa.http.OpenRosaConstants
import com.yedc.openrosa.parse.OpenRosaResponseParser
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.UnknownHostException
import java.util.concurrent.Callable
import javax.net.ssl.SSLException

class OpenRosaClient(
    serverURL: String,
    openRosaHttpInterface: _root_ide_package_.com.yedc.openrosa.http.OpenRosaHttpInterface?,
    private val webCredentialsProvider: WebCredentialsProvider,
    private val openRosaResponseParser: OpenRosaResponseParser
) : _root_ide_package_.com.yedc.forms.FormSource, EntitySource {
    private val openRosaXMLFetcher =
        _root_ide_package_.com.yedc.openrosa.forms.OpenRosaXmlFetcher(
            openRosaHttpInterface,
            this.webCredentialsProvider
        )

    private var serverUrl: String = serverURL

    @Throws(FormSourceException::class)
    override fun fetchFormList(): List<FormListItem> {
        val result = mapException { openRosaXMLFetcher.getXML(getFormListURL()) }

        if (result.errorMessage != null) {
            when (result.responseCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    throw AuthRequired()
                }

                HttpURLConnection.HTTP_NOT_FOUND -> {
                    throw FormSourceException.Unreachable(serverUrl)
                }

                else -> {
                    throw FormSourceException.ServerError(result.responseCode, serverUrl)
                }
            }
        }

        if (result.isOpenRosaResponse) {
            val formList = openRosaResponseParser.parseFormList(result.doc)

            if (formList != null) {
                return formList
            } else {
                throw FormSourceException.ParseError(serverUrl)
            }
        } else {
            throw ServerNotOpenRosaError()
        }
    }

    @Throws(FormSourceException::class)
    override fun fetchManifest(manifestURL: String?): ManifestFile? {
        if (manifestURL == null) {
            return null
        }

        val result = mapException { openRosaXMLFetcher.getXML(manifestURL) }

        if (result.errorMessage != null) {
            if (result.responseCode != HttpURLConnection.HTTP_OK) {
                throw FormSourceException.ServerError(result.responseCode, serverUrl)
            } else {
                throw FetchError()
            }
        }

        if (!result.isOpenRosaResponse) {
            throw FormSourceException.ParseError(serverUrl)
        }

        val mediaFiles = openRosaResponseParser.parseManifest(result.doc)
        if (mediaFiles != null) {
            return ManifestFile(result.hash, mediaFiles)
        } else {
            throw FormSourceException.ParseError(serverUrl)
        }
    }

    @Throws(FormSourceException::class)
    override fun fetchForm(formURL: String): InputStream {
        val result = mapException { openRosaXMLFetcher.fetch(formURL, null) }

        if (result.inputStream == null) {
            throw FormSourceException.ServerError(result.statusCode, serverUrl)
        } else {
            return result.inputStream
        }
    }

    @Throws(FormSourceException::class)
    override fun fetchMediaFile(mediaFileURL: String): InputStream {
        val result = mapException { openRosaXMLFetcher.fetch(mediaFileURL, null) }

        if (result.inputStream == null) {
            throw FormSourceException.ServerError(result.statusCode, serverUrl)
        } else {
            return result.inputStream
        }
    }

    fun updateUrl(url: String) {
        this.serverUrl = url
    }

    fun updateWebCredentialsUtils(webCredentialsProvider: WebCredentialsProvider?) {
        openRosaXMLFetcher.updateWebCredentialsProvider(webCredentialsProvider)
    }

    @Throws(FormSourceException::class)
    private fun <T> mapException(callable: Callable<T>): T {
        try {
            val result = callable.call()

            if (result != null) {
                return result
            } else {
                throw FetchError()
            }
        } catch (e: UnknownHostException) {
            throw FormSourceException.Unreachable(serverUrl)
        } catch (e: SSLException) {
            throw SecurityError(serverUrl)
        } catch (e: Exception) {
            throw FetchError()
        }
    }

    private fun getFormListURL(): String {
        var downloadListUrl = serverUrl

        while (downloadListUrl.endsWith("/")) {
            downloadListUrl = downloadListUrl.substring(0, downloadListUrl.length - 1)
        }

        downloadListUrl += OpenRosaConstants.FORM_LIST
        return downloadListUrl
    }

    override fun fetchDeletedStates(integrityUrl: String, ids: List<String>): List<Pair<String, Boolean>> {
        val uri = Uri.parse(integrityUrl)
            .buildUpon()
            .appendQueryParameter("id", ids.joinToString(","))
            .build()

        val result = openRosaXMLFetcher.getXML(uri.toString())
        if (!result.isOpenRosaResponse) {
            throw FormSourceException.ParseError(serverUrl)
        }

        val parsedResponse = openRosaResponseParser.parseIntegrityResponse(result.doc)

        if (parsedResponse != null) {
            return parsedResponse.map { Pair(it.id, it.deleted) }
        } else {
            throw FormSourceException.ParseError(serverUrl)
        }
    }
}
