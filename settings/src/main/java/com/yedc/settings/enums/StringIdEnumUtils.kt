package com.yedc.settings.enums

import android.content.Context
import com.yedc.settings.keys.ProjectKeys
import com.yedc.shared.settings.Settings

object StringIdEnumUtils {

    @JvmStatic
    fun Settings.getFormUpdateMode(context: Context): _root_ide_package_.com.yedc.settings.enums.FormUpdateMode {
        val setting = this.getString(ProjectKeys.KEY_FORM_UPDATE_MODE)
        return parse(context, setting)
    }

    @JvmStatic
    fun Settings.getAutoSend(context: Context): AutoSend {
        val setting = this.getString(ProjectKeys.KEY_AUTOSEND)
        return parse(context, setting)
    }

    private inline fun <reified T> parse(
        context: Context,
        value: String?
    ): T where T : Enum<T>, T : StringIdEnum {
        return enumValues<T>().find {
            context.getString(it.stringId) == value
        } ?: throw IllegalArgumentException()
    }
}
