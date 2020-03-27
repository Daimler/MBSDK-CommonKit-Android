package com.daimler.mbcommonkit.security

import android.content.Context
import android.content.SharedPreferences
import com.daimler.mbcommonkit.extensions.getMultiAppSharedPreferences
import com.daimler.mbcommonkit.utils.preferencesForSharedUserId

private const val RSA_ENCR_SETTINGS_NAME = "com.daimler.encryption.rsaalgorithm.settings"

/**
 * Preferences utilities for RSA encryption. Multi-app preferences are enforced if there is an
 * sharedUserId given in the AndroidManifest.
 */
internal class RsaSettings(private val context: Context) {

    private val sharedUserId: String? = getSharedUserIdFromManifest()
    private val preferences: SharedPreferences by lazy { createPreferences(sharedUserId) }

    private fun getSharedUserIdFromManifest(): String? =
        context.packageManager.getPackageInfo(context.packageName, 0).sharedUserId

    private fun createPreferences(sharedUserId: String?): SharedPreferences =
        sharedUserId?.let {
            context.getMultiAppSharedPreferences(RSA_ENCR_SETTINGS_NAME, it)
        } ?: context.getSharedPreferences(RSA_ENCR_SETTINGS_NAME, Context.MODE_PRIVATE)

    fun getForAlias(alias: String): String =
        sharedUserId?.let {
            getForAliasWithSharedUserId(alias, it)
        } ?: run {
            preferences.getString(alias, null).orEmpty()
        }

    private fun getForAliasWithSharedUserId(alias: String, sharedUserId: String): String =
        preferences.getString(alias, null)?.takeIf {
            !it.isBlank()
        } ?: copyKeyAndReturn(alias, sharedUserId).orEmpty()

    fun putForAlias(alias: String, key: String) {
        preferences.edit().putString(alias, key).apply()
    }

    fun removeForAlias(alias: String) {
        preferences.edit().remove(alias).apply()
    }

    private fun copyKeyAndReturn(alias: String, sharedUserId: String): String? =
        findKeyIfAvailable(alias, sharedUserId)?.let {
            preferences.edit().putString(alias, it).apply()
            it
        }

    private fun findKeyIfAvailable(alias: String, sharedUserId: String): String? =
        preferencesForSharedUserId(context, RSA_ENCR_SETTINGS_NAME, sharedUserId).find {
            !it.getString(alias, null).isNullOrBlank()
        }?.getString(alias, null)
}