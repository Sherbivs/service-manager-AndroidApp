package com.servicemanager.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// EncryptedPrefsHelper is @Singleton with @Inject constructor — Hilt resolves it automatically.
// Add @Provides here when non-injectable app-wide dependencies are needed.
@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused") // Intentionally empty Hilt module — placeholder for future @Provides methods
object AppModule
