package mx.nexum.app.auth.data.network

import java.util.Locale
import java.util.TimeZone

object ClientInfoProvider {

    fun getLocale(): String {
        val defaultLocale = Locale.getDefault()
        val tag = defaultLocale.toLanguageTag()
        return if (tag.isNotBlank() && tag != "und") tag else "${defaultLocale.language}-${defaultLocale.country}".ifBlank { "es-MX" }
    }

    fun getTimeZone(): String {
        val tzId = TimeZone.getDefault().id
        return tzId.ifBlank { "America/Mexico_City" }
    }
}
