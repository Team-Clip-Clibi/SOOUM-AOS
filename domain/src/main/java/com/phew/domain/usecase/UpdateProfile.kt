package com.phew.domain.usecase

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_FAIL_PACKAGE_IMAGE
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_UN_GOOD_IMAGE
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NOT_FOUND
import com.phew.core_common.HTTP_UN_GOOD_IMAGE
import com.phew.domain.repository.network.ProfileRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class UpdateProfile @Inject constructor(
    private val repository: ProfileRepository,
    private val contextResolver: ContentResolver,
) {
    data class Param(
        val nickName: String?,
        val imgName: String?,
        val profileImage: String?,
        val isImageChange: Boolean,
    )

    suspend operator fun invoke(param: Param): DomainResult<Unit, String> {
        when (param.isImageChange) {
            true -> {
                val requestImageUrl = repository.requestUploadImageUrl()
                if (requestImageUrl is DataResult.Fail) return DomainResult.Failure(ERROR_NETWORK)
                if(param.profileImage == null) return DomainResult.Failure(ERROR_FAIL_JOB)
                val file = try {
                    contextResolver.readAsCompressedJpegRequestBody(uri = param.profileImage.toUri())
                } catch (e: Exception) {
                    e.printStackTrace()
                    return DomainResult.Failure(ERROR_FAIL_PACKAGE_IMAGE)
                } catch (e: OutOfMemoryError) {
                    e.printStackTrace()
                    return DomainResult.Failure(ERROR_FAIL_JOB)
                }
                val requestUploadImage = repository.requestUploadImage(
                    uri = (requestImageUrl as DataResult.Success).data.imgUrl,
                    body = file
                )
                if (requestUploadImage is DataResult.Fail) {
                    return when (requestUploadImage.code) {
                        HTTP_INVALID_TOKEN -> DomainResult.Failure(ERROR_LOGOUT)
                        HTTP_NOT_FOUND -> DomainResult.Failure(ERROR_NETWORK)
                        HTTP_UN_GOOD_IMAGE -> DomainResult.Failure(ERROR_UN_GOOD_IMAGE)
                        else -> DomainResult.Failure(ERROR_FAIL_JOB)
                    }
                }
                val requestUpdateProfile = repository.requestUpdateProfile(
                    nickName = param.nickName,
                    profileImageName = requestImageUrl.data.imgName
                )
                return when (requestUpdateProfile) {
                    is DataResult.Fail -> {
                        when (requestUpdateProfile.code) {
                            HTTP_INVALID_TOKEN -> DomainResult.Failure(ERROR_LOGOUT)
                            HTTP_NOT_FOUND -> DomainResult.Failure(ERROR_NETWORK)
                            else -> DomainResult.Failure(ERROR_FAIL_JOB)
                        }
                    }

                    is DataResult.Success -> {
                        DomainResult.Success(Unit)
                    }
                }
            }

            false -> {
                if (param.imgName == null) return DomainResult.Failure(ERROR_FAIL_JOB)
                val requestUpdateProfile = repository.requestUpdateProfile(
                    nickName = param.nickName,
                    profileImageName = param.imgName
                )
                return when (requestUpdateProfile) {
                    is DataResult.Fail -> {
                        when (requestUpdateProfile.code) {
                            HTTP_INVALID_TOKEN -> DomainResult.Failure(ERROR_LOGOUT)
                            HTTP_NOT_FOUND -> DomainResult.Failure(ERROR_NETWORK)
                            else -> DomainResult.Failure(ERROR_FAIL_JOB)
                        }
                    }

                    is DataResult.Success -> {
                        DomainResult.Success(Unit)
                    }
                }
            }
        }
    }


    private fun ContentResolver.readAsCompressedJpegRequestBody(
        uri: Uri,
    ): RequestBody {
        val inputStream =
            openInputStream(uri) ?: throw IOException("Failed to open InputStream for URI : $uri")
        inputStream.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
                ?: throw IOException("Fail to decode bitmap from URI :$uri")
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        }
    }
}