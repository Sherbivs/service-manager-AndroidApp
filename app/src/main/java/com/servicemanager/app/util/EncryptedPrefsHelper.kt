package com.servicemanager.app.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPrefsHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            private const val PREFS_FILE = "service_manager_secure_prefs"
            private const val KEY_SERVER_URL = "server_url"
            const val DEFAULT_URL = "http://192.168.23.83:3500"
        }

        private val prefs by lazy {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREFS_FILE,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }

        var serverUrl: String
            get() = prefs.getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL
            set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

        fun hasServerUrl(): Boolean = prefs.contains(KEY_SERVER_URL)
    }
