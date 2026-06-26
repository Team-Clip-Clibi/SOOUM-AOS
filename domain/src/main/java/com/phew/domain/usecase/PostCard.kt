package com.phew.domain.usecase

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import com.phew.core_common.ERROR_ACCOUNT_SUSPENDED
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_FAIL_PACKAGE_IMAGE
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_UN_GOOD_IMAGE
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.core_common.HTTP_NOT_FOUND
import com.phew.core_common.HTTP_UN_GOOD_IMAGE
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.resultFailure
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.event.EventRepository
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
    private val eventRepository: EventRepository
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
        val isDistanceShared: Boolean,
        val pollContents: List<String>,
    )

    suspend operator fun invoke(data: Param): Result<Long> {
        if(data.content.trim().isEmpty()) return resultFailure(ERROR_FAIL_JOB)
        val checkedPostBanned = networkRepository.requestCheckUploadCard()
            .getOrElse { return resultFailure(ERROR_FAIL_JOB) }
        if (checkedPostBanned.isBaned) {
            return resultFailure(checkedPostBanned.viewTime)
        }
        val imageInfoResult = if (data.isFromDevice) {
            getImageInfoFromDevice(data.imageUrl)
        } else {
            data.imgName?.let { imageName ->
                Result.success(
                    UploadImageInfo(
                        imageName,
                        "DEFAULT"
                    )
                )
            } ?: resultFailure(ERROR_FAIL_JOB)
        }
        val imageInfo = imageInfoResult.getOrElse { return Result.failure(it) }
        return uploadCardData(param = data, imageInfo = imageInfo)
    }

    private suspend fun uploadCardData(
        param: Param,
        imageInfo: UploadImageInfo,
    ): Result<Long> {
        val locationPermissionCheck = deviceRepository.getLocationPermission()
        val (latitude, longitude) = if (locationPermissionCheck && param.isDistanceShared) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }
        val uploadResult = if (!param.answerCard) {
            networkRepository.requestUploadCard(
                isDistanceShared = param.isDistanceShared,
                content = param.content,
                imageName = imageInfo.name,
                isStory = param.isStory!!,
                font = param.font,
                latitude = latitude,
                longitude = longitude,
                tag = param.tags,
                imageType = imageInfo.type,
                hasPoll = param.pollContents.isNotEmpty(),
                pollContents = param.pollContents
            )
        } else {
            if (param.cardId == null) return resultFailure(ERROR_FAIL_JOB)
            networkRepository.requestUploadCardAnswer(
                cardId = param.cardId,
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
        return uploadResult.fold(
            onSuccess = { response ->
                eventRepository.logWriteCardClickFinishButton()
                if (!param.isDistanceShared) eventRepository.logWriteDistanceSharedOff()
                Result.success(response.cardId)
            },
            onFailure = { throwable ->
                when (throwable.asSooumException().code) {
                    HTTP_BAD_REQUEST -> resultFailure(ERROR_ACCOUNT_SUSPENDED)
                    HTTP_CARD_ALREADY_DELETE -> resultFailure(ERROR_ALREADY_CARD_DELETE)
                    HTTP_NOT_FOUND -> resultFailure(ERROR_FAIL_JOB)
                    HTTP_UN_GOOD_IMAGE -> resultFailure(ERROR_UN_GOOD_IMAGE)
                    else -> resultFailure(ERROR_FAIL_JOB)
                }
            },
        )
    }

    private suspend fun getImageInfoFromDevice(imageUrl: String?): Result<UploadImageInfo> {
        if (imageUrl == null) return resultFailure(ERROR_FAIL_JOB)
        val uploadInfo = networkRepository.requestUploadCardImage()
            .getOrElse { return resultFailure(it.asSooumException().message.ifBlank { ERROR_FAIL_JOB }) }
        val file = try {
            context.contentResolver.readAsCompressedJpegRequestBody(uri = imageUrl.toUri())
        } catch (e: IOException) {
            e.printStackTrace()
            return resultFailure(ERROR_FAIL_PACKAGE_IMAGE)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return resultFailure(ERROR_FAIL_JOB)
        }
        return networkRepository.requestUploadImage(data = file, url = uploadInfo.url)
            .fold(
                onSuccess = { Result.success(UploadImageInfo(uploadInfo.imageName, "USER")) },
                onFailure = { resultFailure(ERROR_NETWORK) },
            )
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
