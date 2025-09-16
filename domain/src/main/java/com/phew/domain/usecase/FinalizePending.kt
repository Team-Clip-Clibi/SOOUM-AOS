package com.phew.domain.usecase

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FinalizePending @Inject constructor(@ApplicationContext private val context: Context) {

    suspend operator fun invoke(uri: Uri): Boolean {
        val cv = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
        return runCatching {
            context.contentResolver.update(uri, cv, null, null) > 0
        }.getOrElse { false }
    }
}