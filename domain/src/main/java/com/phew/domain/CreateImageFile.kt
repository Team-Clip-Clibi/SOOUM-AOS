package com.phew.domain

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.phew.core_common.ERROR_FAIL_JOB
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.phew.core_common.Result

class CreateImageFile @Inject constructor(@ApplicationContext private val context: Context) {

    suspend operator fun invoke(): Result<Uri, String> {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourApp")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = context.contentResolver.insert(collection, values) ?: return Result.Failure(
            ERROR_FAIL_JOB
        )
        return Result.Success(uri)
    }
}