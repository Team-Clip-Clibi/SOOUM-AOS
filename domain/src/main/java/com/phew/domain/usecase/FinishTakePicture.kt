package com.phew.domain.usecase

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.domain.CROP_FILE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FinishTakePicture @Inject constructor(@ApplicationContext private val context: Context) {

    data class Param(
        val uri: Uri
    )

    operator fun invoke(data: Param): DomainResult<Uri, String> {
        if (data.uri.scheme == CROP_FILE) {
            return DomainResult.Success(data.uri)
        }
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(data.uri, contentValues, null, null)
            return DomainResult.Success(data.uri)
        } catch (e: Exception) {
            e.printStackTrace()
            return DomainResult.Failure(ERROR_FAIL_JOB)
        }
    }
}