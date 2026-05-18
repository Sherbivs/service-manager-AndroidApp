package com.servicemanager.app.di

import com.servicemanager.app.data.api.ApiService
import com.servicemanager.app.util.SecurePrefsHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val MIN_TIMEOUT_SECONDS = 1
    private const val MAX_TIMEOUT_SECONDS = 120

    @Provides
    @Singleton
    fun provideOkHttpClient(prefsHelper: SecurePrefsHelper): OkHttpClient {
        val logging =
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            }
        return OkHttpClient
            .Builder()
            // Dynamically rewrite the base URL on every request so the Retrofit
            // singleton picks up URL changes from EncryptedPrefsHelper immediately.
            .addInterceptor { chain ->
                val request = chain.request()
                val serverUrl = prefsHelper.serverUrl.trimEnd('/')
                val connectTimeoutSeconds =
                    prefsHelper.connectTimeoutSeconds.coerceIn(MIN_TIMEOUT_SECONDS, MAX_TIMEOUT_SECONDS)
                val readTimeoutSeconds =
                    prefsHelper.readTimeoutSeconds.coerceIn(MIN_TIMEOUT_SECONDS, MAX_TIMEOUT_SECONDS)
                val timeoutChain =
                    chain
                        .withConnectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                        .withReadTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                val parsed =
                    "$serverUrl/".toHttpUrlOrNull()
                        ?: return@addInterceptor timeoutChain.proceed(request)
                val newUrl =
                    request.url
                        .newBuilder()
                        .scheme(parsed.scheme)
                        .host(parsed.host)
                        .port(parsed.port)
                        .build()
                timeoutChain.proceed(request.newBuilder().url(newUrl).build())
            }.addInterceptor(logging)
            .connectTimeout(SecurePrefsHelper.DEFAULT_CONNECT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .readTimeout(SecurePrefsHelper.DEFAULT_READ_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit
            .Builder()
            .baseUrl("http://localhost:3500/") // placeholder; overridden by interceptor
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}
