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
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 10L

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
                val parsed =
                    "$serverUrl/".toHttpUrlOrNull()
                        ?: return@addInterceptor chain.proceed(request)
                val newUrl =
                    request.url
                        .newBuilder()
                        .scheme(parsed.scheme)
                        .host(parsed.host)
                        .port(parsed.port)
                        .build()
                chain.proceed(request.newBuilder().url(newUrl).build())
            }.addInterceptor(logging)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
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
