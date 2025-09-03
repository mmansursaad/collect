package com.jed.optima.android.configure.qr

import com.google.zxing.WriterException
import org.json.JSONException
import com.jed.optima.android.storage.StoragePathProvider
import com.jed.optima.androidshared.bitmap.ImageFileUtils
import com.jed.optima.qrcode.zxing.QRCodeCreator
import com.jed.optima.qrcode.zxing.QRCodeDecoder
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays

class CachingQRCodeGenerator(private val qrCodeCreator: QRCodeCreator) : QRCodeGenerator {

    @Throws(
        QRCodeDecoder.QRCodeInvalidException::class,
        NoSuchAlgorithmException::class,
        IOException::class,
        WriterException::class
    )
    override fun generateQRCode(
        selectedPasswordKeys: Collection<String>,
        appConfigurationGenerator: AppConfigurationGenerator
    ): String {
        val preferencesString =
            appConfigurationGenerator.getAppConfigurationAsJson(selectedPasswordKeys)
        val md = MessageDigest.getInstance("MD5")
        md.update(preferencesString.toByteArray())
        val messageDigest = md.digest()
        var shouldWriteToDisk = true

        // check if settings directory exists, if not then create one
        val writeDir = File(StoragePathProvider().getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.SETTINGS))
        if (!writeDir.exists()) {
            if (!writeDir.mkdirs()) {
                Timber.e(Error("Error creating directory " + writeDir.absolutePath))
            }
        }
        val mdCacheFile = File(md5CachePath)
        if (mdCacheFile.exists()) {
            val cachedMessageDigest = com.jed.optima.android.utilities.FileUtils.read(mdCacheFile)

            /*
             * If the messageDigest generated from the preferences is equal to cachedMessageDigest
             * then don't generate QRCode and read the one saved in disk
             */
            if (Arrays.equals(messageDigest, cachedMessageDigest)) {
                Timber.i("Loading QRCode from the disk...")
                shouldWriteToDisk = false
            }
        }

        // If the file is not found in the disk or md5Hash not matched
        if (shouldWriteToDisk) {
            Timber.i("Generating QRCode...")
            val time = System.currentTimeMillis()
            val bmp = qrCodeCreator.createEncoded(preferencesString)
            Timber.i("QR Code generation took : %d ms", System.currentTimeMillis() - time)
            Timber.i("Saving QR Code to disk... : %s", qRCodeFilepath)
            ImageFileUtils.saveBitmapToFile(bmp, qRCodeFilepath)
            com.jed.optima.android.utilities.FileUtils.write(mdCacheFile, messageDigest)
            Timber.i("Updated %s file contents", SETTINGS_MD5_FILE)
        }
        return qRCodeFilepath
    }

    private val qRCodeFilepath: String
        get() = StoragePathProvider().getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.SETTINGS) + File.separator + "com.jed.optima-settings.png"
    private val md5CachePath: String
        get() = StoragePathProvider().getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.SETTINGS) + File.separator + SETTINGS_MD5_FILE

    companion object {
        private const val SETTINGS_MD5_FILE = ".com.jed.optima-settings-hash"
    }
}

interface QRCodeGenerator {
    @Throws(
        JSONException::class,
        NoSuchAlgorithmException::class,
        IOException::class,
        WriterException::class
    )
    fun generateQRCode(
        selectedPasswordKeys: Collection<String>,
        appConfigurationGenerator: AppConfigurationGenerator
    ): String
}
