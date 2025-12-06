package com.phew.domain.usecase

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_ACCOUNT_SUSPENDED
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_FAIL_PACKAGE_IMAGE
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_UN_GOOD_IMAGE
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.core_common.HTTP_NOT_FOUND
import com.phew.core_common.HTTP_SUCCESS
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.CardFeedRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class PostCard @Inject constructor(
    private val networkRepository: CardFeedRepository,
    @ApplicationContext private val context: Context,
    private val deviceRepository: DeviceRepository,
) {
    data class Param(
        val isFromDevice: Boolean, //핸드폰 디바이스에서 사진 선택했는지
        val answerCard: Boolean, // 답 카드 인지
        val cardId: Long?,
        val imageUrl: String?,
        val content: String,
        val font: String,
        var imgName: String?,
        val isStory: Boolean?,
        val tags: List<String>,
    )

    suspend operator fun invoke(data: Param): DomainResult<Long, String> {
        when (val checkedPostBanned = networkRepository.requestCheckUploadCard()) {
            is DataResult.Fail -> return DomainResult.Failure(ERROR_FAIL_JOB)
            is DataResult.Success -> {
                if (checkedPostBanned.data.isBaned) return DomainResult.Failure(checkedPostBanned.data.viewTime)
            }
        }
        val imageInfoResult = if (data.isFromDevice) {
            getImageInfoFromDevice(data.imageUrl)
        } else {
            data.imgName?.let { imageName ->
                DomainResult.Success(
                    UploadImageInfo(
                        imageName,
                        "DEFAULT"
                    )
                )
            } ?: DomainResult.Failure(ERROR_FAIL_JOB)
        }
        if (imageInfoResult is DomainResult.Failure) return imageInfoResult
        val imageInfo = (imageInfoResult as DomainResult.Success).data
        return uploadCardData(param = data, imageInfo = imageInfo)
    }

    private suspend fun uploadCardData(
        param: Param,
        imageInfo: UploadImageInfo,
    ): DomainResult<Long, String> {
        val locationPermissionCheck = deviceRepository.getLocationPermission()
        val (latitude, longitude) = if (locationPermissionCheck) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }
        val uploadResult = if (!param.answerCard) {
            networkRepository.requestUploadCard(
                isDistanceShared = locationPermissionCheck,
                content = param.content,
                imageName = imageInfo.name,
                isStory = param.isStory!!,
                font = param.font,
                latitude = latitude,
                longitude = longitude,
                tag = param.tags,
                imageType = imageInfo.type
            )
        } else {
            if (param.cardId == null) return DomainResult.Failure(ERROR_FAIL_JOB) // Ensure early exit for null cardId
            networkRepository.requestUploadCardAnswer(
                cardId = param.cardId ?: 0L,
                content = param.content,
                font = param.font,
                imageName = imageInfo.name,
                imageType = imageInfo.type,
                isDistanceShared = locationPermissionCheck,
                latitude = latitude,
                longitude = longitude,
                tag = param.tags
            )
        }
        return when (uploadResult) {
            HTTP_SUCCESS -> DomainResult.Success(uploadResult.data.cardId)
            HTTP_BAD_REQUEST -> DomainResult.Failure(ERROR_ACCOUNT_SUSPENDED)
            HTTP_CARD_ALREADY_DELETE -> DomainResult.Failure(ERROR_ALREADY_CARD_DELETE)
            HTTP_NOT_FOUND -> DomainResult.Failure(ERROR_FAIL_JOB)
            else -> DomainResult.Failure(ERROR_FAIL_JOB)
        }
    }

    private suspend fun getImageInfoFromDevice(imageUrl: String?): DomainResult<UploadImageInfo, String> {
        if (imageUrl == null) return DomainResult.Failure(ERROR_FAIL_JOB)
        return when (val requestUrl = networkRepository.requestUploadCardImage()) {
            is DataResult.Fail -> {
                DomainResult.Failure(requestUrl.message ?: ERROR_FAIL_JOB)
            }

            is DataResult.Success -> {
                val uploadInfo = requestUrl.data
                val file =
                    try {
                        context.contentResolver.readAsCompressedJpegRequestBody(uri = imageUrl.toUri())
                    } catch (e: IOException) {
                        return DomainResult.Failure(ERROR_FAIL_PACKAGE_IMAGE)
                    } catch (e: OutOfMemoryError) {
                        return DomainResult.Failure(ERROR_FAIL_JOB)
                    }
                when (networkRepository.requestUploadImage(data = file, url = uploadInfo.url)) {
                    is DataResult.Fail -> {
                        return DomainResult.Failure(ERROR_NETWORK)
                    }

                    is DataResult.Success -> {
                        val checkBackgroundImage =
                            networkRepository.requestCheckImage(uploadInfo.imageName)
                        if (checkBackgroundImage is DataResult.Fail) {
                            return DomainResult.Failure(ERROR_NETWORK)
                        }
                        if (checkBackgroundImage is DataResult.Success && !checkBackgroundImage.data) {
                            return DomainResult.Failure(ERROR_UN_GOOD_IMAGE)
                        }
                        DomainResult.Success(
                            UploadImageInfo(
                                uploadInfo.imageName,
                                "USER"
                            )
                        )
                    }
                }
            }
        }
    }

    private data class UploadImageInfo(val name: String, val type: String)

    private fun ContentResolver.readAsCompressedJpegRequestBody(
        uri: Uri,
    ): RequestBody {
        val inputStream = openInputStream(uri)
            ?: throw IOException("Failed to open InputStream for URI: $uri")

        inputStream.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
                ?: throw IOException("Failed to decode bitmap from URI: $uri")

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        }
    }
}