package com.yedc.android.widgets.utilities

import com.yedc.android.injection.DaggerUtils
import com.yedc.settings.keys.ProjectKeys
import com.yedc.shared.settings.Settings

object QuestionFontSizeUtils {
    const val DEFAULT_FONT_SIZE = 21
    private const val HEADLINE_6_DIFF = -1
    private const val SUBTITLE_1_DIFF = -5
    private const val BODY_MEDIUM_DIFF = -7
    private const val BODY_LARGE_DIFF = -5
    private const val TITLE_LARGE_DIFF = -1

    @JvmStatic
    fun getFontSize(settings: Settings, fontSize: FontSize?): Int {
        val settingsValue = settings.getString(ProjectKeys.KEY_FONT_SIZE)!!.toInt()

        return when (fontSize) {
            FontSize.HEADLINE_6 -> settingsValue + HEADLINE_6_DIFF
            FontSize.SUBTITLE_1 -> settingsValue + SUBTITLE_1_DIFF
            FontSize.BODY_MEDIUM -> settingsValue + BODY_MEDIUM_DIFF
            FontSize.BODY_LARGE -> settingsValue + BODY_LARGE_DIFF
            FontSize.TITLE_LARGE -> settingsValue + TITLE_LARGE_DIFF
            else -> throw IllegalArgumentException()
        }
    }

    @JvmStatic
    @Deprecated("Use {@link QuestionFontSizeUtils#getFontSize(Settings, FontSize)} instead")
    fun getQuestionFontSize(): Int {
        return try {
            val fontSize = DaggerUtils.getComponent(_root_ide_package_.com.yedc.android.application.Collect.getInstance()).settingsProvider().getUnprotectedSettings().getString(ProjectKeys.KEY_FONT_SIZE)!!.toInt()
            fontSize + HEADLINE_6_DIFF
        } catch (e: Exception) {
            DEFAULT_FONT_SIZE
        } catch (e: Error) {
            DEFAULT_FONT_SIZE
        }
    }

    enum class FontSize {
        HEADLINE_6, SUBTITLE_1, BODY_MEDIUM, BODY_LARGE, TITLE_LARGE
    }
}
