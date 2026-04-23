package com.servicemanager.app.util

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun getString(
            @StringRes resId: Int,
        ): String = context.getString(resId)

        fun getString(
            @StringRes resId: Int,
            vararg formatArgs: Any,
        ): String = context.getString(resId, *formatArgs)
    }
