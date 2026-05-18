package com.servicemanager.app.util

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
            private val KEY_SERVER_SCHEME = stringPreferencesKey("server_scheme")
            private val KEY_SERVER_HOST = stringPreferencesKey("server_host")
            private val KEY_SERVER_PORT = intPreferencesKey("server_port")
            private val KEY_CONNECT_TIMEOUT_SECONDS = intPreferencesKey("connect_timeout_seconds")
            private val KEY_READ_TIMEOUT_SECONDS = intPreferencesKey("read_timeout_seconds")
            private const val KEYSET_NAME = "master_keyset"
            private const val PREF_FILE_NAME = "master_key_preference"
            private const val MASTER_KEY_URI = "android-keystore://master_key"
            private const val LEGACY_DEFAULT_URL = "http://192.168.23.83:3500"
            const val DEFAULT_SERVER_SCHEME = "http"
            const val DEFAULT_SERVER_HOST = "sensaimanager.drip"
            const val DEFAULT_SERVER_PORT = 3500
            const val DEFAULT_URL = "http://sensaimanager.drip:3500"
            const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 10
            const val DEFAULT_READ_TIMEOUT_SECONDS = 10
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

            init {
                migrateLegacyDefaultUrlIfNeeded()
            }

        val serverSchemeFlow: Flow<String> =
            context.dataStore.data.map { prefs ->
                prefs[KEY_SERVER_SCHEME] ?: DEFAULT_SERVER_SCHEME
            }

        val serverHostFlow: Flow<String> =
            context.dataStore.data.map { prefs ->
                prefs[KEY_SERVER_HOST] ?: DEFAULT_SERVER_HOST
            }

        val serverPortFlow: Flow<Int> =
            context.dataStore.data.map { prefs ->
                prefs[KEY_SERVER_PORT] ?: DEFAULT_SERVER_PORT
            }

        val serverUrlFlow: Flow<String> =
            context.dataStore.data.map { prefs ->
                val scheme = prefs[KEY_SERVER_SCHEME]
                val host = prefs[KEY_SERVER_HOST]
                val port = prefs[KEY_SERVER_PORT]
                if (!scheme.isNullOrBlank() && !host.isNullOrBlank() && port != null && port in 1..65535) {
                    composeUrl(scheme, host, port)
                } else {
                    val encrypted = prefs[KEY_SERVER_URL]
                    val decrypted = encrypted?.let(::decrypt)?.trim()?.trimEnd('/')
                    val parsed = decrypted?.let(::parseEndpoint)
                    if (parsed != null) {
                        composeUrl(parsed.scheme, parsed.host, parsed.port)
                    } else {
                        DEFAULT_URL
                    }
                }
            }

        val connectTimeoutSecondsFlow: Flow<Int> =
            context.dataStore.data.map { prefs ->
                prefs[KEY_CONNECT_TIMEOUT_SECONDS] ?: DEFAULT_CONNECT_TIMEOUT_SECONDS
            }

        val readTimeoutSecondsFlow: Flow<Int> =
            context.dataStore.data.map { prefs ->
                prefs[KEY_READ_TIMEOUT_SECONDS] ?: DEFAULT_READ_TIMEOUT_SECONDS
            }

        // Blocking getter for Retrofit Interceptor (caution: avoid calling on Main Thread)
        var serverScheme: String
            get() = runBlocking { serverSchemeFlow.first() }
            set(value) = runBlocking { setServerEndpoint(value, serverHost, serverPort) }

        var serverHost: String
            get() = runBlocking { serverHostFlow.first() }
            set(value) = runBlocking { setServerEndpoint(serverScheme, value, serverPort) }

        var serverPort: Int
            get() = runBlocking { serverPortFlow.first() }
            set(value) = runBlocking { setServerEndpoint(serverScheme, serverHost, value) }

        var serverUrl: String
            get() = runBlocking { serverUrlFlow.first() }
            set(value) = runBlocking { setServerUrl(value) }

        var connectTimeoutSeconds: Int
            get() = runBlocking { connectTimeoutSecondsFlow.first() }
            set(value) = runBlocking { setConnectTimeoutSeconds(value) }

        var readTimeoutSeconds: Int
            get() = runBlocking { readTimeoutSecondsFlow.first() }
            set(value) = runBlocking { setReadTimeoutSeconds(value) }

        suspend fun setServerUrl(url: String) {
            val normalized = url.trim().trimEnd('/')
            val parsed = parseEndpoint(normalized)
            context.dataStore.edit { prefs ->
                prefs[KEY_SERVER_URL] = encrypt(normalized)
                if (parsed != null) {
                    prefs[KEY_SERVER_SCHEME] = parsed.scheme
                    prefs[KEY_SERVER_HOST] = parsed.host
                    prefs[KEY_SERVER_PORT] = parsed.port
                }
            }
        }

        suspend fun setServerEndpoint(
            scheme: String,
            host: String,
            port: Int,
        ) {
            val normalizedScheme = scheme.trim().lowercase()
            val normalizedHost = host.trim()
            val normalizedPort = port
            val composedUrl = composeUrl(normalizedScheme, normalizedHost, normalizedPort)

            context.dataStore.edit { prefs ->
                prefs[KEY_SERVER_SCHEME] = normalizedScheme
                prefs[KEY_SERVER_HOST] = normalizedHost
                prefs[KEY_SERVER_PORT] = normalizedPort
                prefs[KEY_SERVER_URL] = encrypt(composedUrl)
            }
        }

        suspend fun setConnectTimeoutSeconds(seconds: Int) {
            context.dataStore.edit { prefs ->
                prefs[KEY_CONNECT_TIMEOUT_SECONDS] = seconds
            }
        }

        suspend fun setReadTimeoutSeconds(seconds: Int) {
            context.dataStore.edit { prefs ->
                prefs[KEY_READ_TIMEOUT_SECONDS] = seconds
            }
        }

        fun hasServerUrl(): Boolean =
            runBlocking {
                context.dataStore.data
                    .first()
                    .contains(KEY_SERVER_URL)
            }

        private fun migrateLegacyDefaultUrlIfNeeded() {
            runBlocking {
                context.dataStore.edit { prefs ->
                    val encrypted = prefs[KEY_SERVER_URL] ?: return@edit
                    val decrypted = decrypt(encrypted) ?: return@edit
                    val normalized = decrypted.trim().trimEnd('/')
                    if (normalized == LEGACY_DEFAULT_URL) {
                        prefs[KEY_SERVER_URL] = encrypt(DEFAULT_URL)
                    }
                    val parsed = parseEndpoint(normalized)
                    if (parsed != null) {
                        if (!prefs.contains(KEY_SERVER_SCHEME)) {
                            prefs[KEY_SERVER_SCHEME] = parsed.scheme
                        }
                        if (!prefs.contains(KEY_SERVER_HOST)) {
                            prefs[KEY_SERVER_HOST] = parsed.host
                        }
                        if (!prefs.contains(KEY_SERVER_PORT)) {
                            prefs[KEY_SERVER_PORT] = parsed.port
                        }
                    }
                }
            }
        }

        private data class Endpoint(
            val scheme: String,
            val host: String,
            val port: Int,
        )

        private fun parseEndpoint(url: String): Endpoint? {
            val parsed = "$url/".toHttpUrlOrNull() ?: return null
            if (parsed.host.isBlank()) return null
            return Endpoint(
                scheme = parsed.scheme,
                host = parsed.host,
                port = parsed.port,
            )
        }

        private fun composeUrl(
            scheme: String,
            host: String,
            port: Int,
        ): String = "${scheme.lowercase()}://${host.trim()}:${port}"

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
