package com.phew.domain.usecase

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CreateImageFile @Inject constructor(@ApplicationContext private val context: Context) {

    operator fun invoke(): DomainResult<Uri, String> {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpeg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourApp")
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = context.contentResolver.insert(collection, values) ?: return DomainResult.Failure(
            ERROR_FAIL_JOB
        )
        return DomainResult.Success(uri)
    }
}