package com.phew.domain.usecase

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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
        val isImageChange: Boolean
    )

    suspend operator fun invoke(param: Param): DomainResult<Unit, String> {
        when (param.isImageChange) {
            //이미지 변경이 없을 시
            false -> {
                val request = repository.requestUpdateProfile(
                    nickName = param.nickName,
                    profileImageName = param.imgName
                )
                return handleResult(request = request)
            }
            // 이미지 변경이 있을 시
            true -> {
                //이미지 변경은 있지만 Default Image 사용 시
                if (param.profileImage == null) {
                    val request = repository.requestUpdateProfile(
                        nickName = param.nickName,
                        profileImageName = null
                    )
                    return handleResult(request = request)
                }
                val requestImageUrl = repository.requestUploadImageUrl()
                if (requestImageUrl is DataResult.Fail) return DomainResult.Failure(ERROR_NETWORK)
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
                        HTTP_NOT_FOUND -> DomainResult.Failure(ERROR_NETWORK)
                        else -> DomainResult.Failure(ERROR_FAIL_JOB)
                    }
                }
                val requestUpdateProfile = repository.requestUpdateProfile(
                    nickName = null,
                    profileImageName = requestImageUrl.data.imgName
                )
                return handleResult(request = requestUpdateProfile)
            }
        }
    }

    private fun handleResult(request: DataResult<Unit>): DomainResult<Unit, String> {
        return when (request) {
            is DataResult.Fail -> {
                when (request.code) {
                    HTTP_INVALID_TOKEN -> DomainResult.Failure(ERROR_LOGOUT)
                    HTTP_NOT_FOUND -> DomainResult.Failure(ERROR_NETWORK)
                    HTTP_UN_GOOD_IMAGE -> DomainResult.Failure(ERROR_UN_GOOD_IMAGE)
                    else -> DomainResult.Failure(ERROR_FAIL_JOB)
                }
            }

            is DataResult.Success -> DomainResult.Success(Unit)
        }
    }

    private fun ContentResolver.readAsCompressedJpegRequestBody(
        uri: Uri,
    ): RequestBody {
        // 1. EXIF 정보를 읽기 위해 스트림을 엽니다.
        val inputForExif = openInputStream(uri)
        val orientation = inputForExif?.use {
            val exif = ExifInterface(it)
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
        val inputStream = openInputStream(uri)
            ?: throw IOException("Failed to open InputStream for URI : $uri")
        inputStream.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
                ?: throw IOException("Fail to decode bitmap from URI :$uri")
            val rotatedBitmap = rotateBitmap(bitmap, orientation)
            val byteArrayOutputStream = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap // 회전이 필요 없으면 원본 반환
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}