package com.servicemanager.app.util

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_prefs")

@Singleton
class SecurePrefsHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            private val KEY_SERVER_URL = stringPreferencesKey("server_url")
            private const val KEYSET_NAME = "master_keyset"
            private const val PREF_FILE_NAME = "master_key_preference"
            private const val MASTER_KEY_URI = "android-keystore://master_key"
            const val DEFAULT_URL = "http://192.168.23.83:3500"
        }

        private val aead: Aead by lazy {
            AeadConfig.register()
            AndroidKeysetManager
                .Builder()
                .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .keysetHandle
                .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        }

        val serverUrlFlow: Flow<String> =
            context.dataStore.data.map { prefs ->
                val encrypted = prefs[KEY_SERVER_URL]
                if (encrypted != null) {
                    decrypt(encrypted) ?: DEFAULT_URL
                } else {
                    DEFAULT_URL
                }
            }

        // Blocking getter for Retrofit Interceptor (caution: avoid calling on Main Thread)
        var serverUrl: String
            get() = runBlocking { serverUrlFlow.first() }
            set(value) = runBlocking { setServerUrl(value) }

        suspend fun setServerUrl(url: String) {
            context.dataStore.edit { prefs ->
                prefs[KEY_SERVER_URL] = encrypt(url)
            }
        }

        fun hasServerUrl(): Boolean =
            runBlocking {
                context.dataStore.data
                    .first()
                    .contains(KEY_SERVER_URL)
            }

        private fun encrypt(plaintext: String): String {
            val ciphertext = aead.encrypt(plaintext.toByteArray(Charsets.UTF_8), null)
            return Base64.encodeToString(ciphertext, Base64.DEFAULT)
        }

        @Suppress("SwallowedException")
        private fun decrypt(ciphertextBase64: String): String? =
            try {
                val ciphertext = Base64.decode(ciphertextBase64, Base64.DEFAULT)
                val decrypted = aead.decrypt(ciphertext, null)
                String(decrypted, Charsets.UTF_8)
            } catch (e: java.security.GeneralSecurityException) {
                // Decryption failed. This is expected if keys are lost.
                null
            } catch (e: IllegalArgumentException) {
                // Base64 decode failed.
                null
            }
    }
