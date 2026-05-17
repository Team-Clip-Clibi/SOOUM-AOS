package com.phew.domain.usecase

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_ACCOUNT_SUSPENDED
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_FAIL_PACKAGE_IMAGE
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.domain.dto.CardReplyRequest
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.domain.repository.network.CardFeedRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class PostCardReply @Inject constructor(
    private val repository: CardDetailRepository,
    private val cardFeedRepository: CardFeedRepository,
    @ApplicationContext private val context: Context,
    private val deviceRepository: DeviceRepository,
    private val eventRepository: EventRepository
) {
    data class Param(
        val cardId: Long,
        val content: String,
        val font: String,
        val imgType: String,
        val imgName: String,
        val imageUrl: String?,
        val tags: List<String>,
        val isDistanceShared: Boolean
    )

    suspend operator fun invoke(param: Param): DomainResult<Long, String> {
        val imageInfoResult = when (param.imgType) {
            IMAGE_TYPE_USER -> getImageInfoFromDevice(param.imageUrl)
            else -> DomainResult.Success(UploadImageInfo(param.imgName, IMAGE_TYPE_DEFAULT))
        }
        if (imageInfoResult is DomainResult.Failure) return imageInfoResult
        val imageInfo = (imageInfoResult as DomainResult.Success).data

        val locationPermissionCheck = deviceRepository.getLocationPermission()
        val (latitude, longitude) = if (locationPermissionCheck && param.isDistanceShared) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }

        val request = CardReplyRequest(
            isDistanceShared = param.isDistanceShared,
            latitude = latitude,
            longitude = longitude,
            content = param.content,
            font = param.font,
            imgType = imageInfo.type,
            imgName = imageInfo.name,
            tags = param.tags
        )

        return when (val result = repository.postCardReply(param.cardId, request)) {
            is DataResult.Success -> {
                eventRepository.logWriteCardClickFinishButton()
                if(!locationPermissionCheck) eventRepository.logWriteDistanceSharedOff()
                DomainResult.Success(result.data.cardId)
            }
            is DataResult.Fail -> mapFailure(result)
        }
    }

    private fun mapFailure(result: DataResult.Fail): DomainResult.Failure<String> {
        return when (result.code) {
            APP_ERROR_CODE -> DomainResult.Failure(result.message ?: ERROR_FAIL_JOB)
            HTTP_BAD_REQUEST -> DomainResult.Failure(ERROR_ACCOUNT_SUSPENDED)
            HTTP_CARD_ALREADY_DELETE -> DomainResult.Failure(ERROR_ALREADY_CARD_DELETE)
            else -> DomainResult.Failure(ERROR_FAIL_JOB)
        }
    }

    private suspend fun getImageInfoFromDevice(imageUrl: String?): DomainResult<UploadImageInfo, String> {
        if (imageUrl == null) return DomainResult.Failure(ERROR_FAIL_JOB)
        return when (val requestUrl = cardFeedRepository.requestUploadCardImage()) {
            is DataResult.Fail -> DomainResult.Failure(requestUrl.message ?: ERROR_FAIL_JOB)
            is DataResult.Success -> {
                val uploadInfo = requestUrl.data
                val file = try {
                    context.contentResolver.readAsCompressedJpegRequestBody(uri = imageUrl.toUri())
                } catch (e: IOException) {
                    e.printStackTrace()
                    return DomainResult.Failure(ERROR_FAIL_PACKAGE_IMAGE)
                } catch (e: OutOfMemoryError) {
                    e.printStackTrace()
                    return DomainResult.Failure(ERROR_FAIL_JOB)
                }

                when (val uploadResult =
                    cardFeedRepository.requestUploadImage(data = file, url = uploadInfo.url)) {
                    is DataResult.Fail -> DomainResult.Failure(ERROR_NETWORK)
                    is DataResult.Success -> DomainResult.Success(
                        UploadImageInfo(
                            uploadInfo.imageName,
                            IMAGE_TYPE_USER
                        )
                    )
                }
            }
        }
    }

    private data class UploadImageInfo(val name: String, val type: String)

    private fun ContentResolver.readAsCompressedJpegRequestBody(uri: Uri): RequestBody {
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

private const val IMAGE_TYPE_DEFAULT = "DEFAULT"
private const val IMAGE_TYPE_USER = "USER"
