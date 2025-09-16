package com.phew.domain.usecase

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FinishTakePicture @Inject constructor(@ApplicationContext private val context: Context) {

    data class Param(
        val uri: Uri
    )

    suspend operator fun invoke(data: Param): Result<Uri, String> {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(data.uri, contentValues, null, null)
            return Result.Success(data.uri)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.Failure(ERROR_FAIL_JOB)
        }
    }
}