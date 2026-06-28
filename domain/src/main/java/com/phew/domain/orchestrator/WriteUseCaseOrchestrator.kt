package com.phew.domain.orchestrator

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_ACCOUNT_SUSPENDED
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_FAIL_PACKAGE_IMAGE
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_NO_DATA
import com.phew.core_common.ERROR_UN_GOOD_IMAGE
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NOT_FOUND
import com.phew.core_common.HTTP_UN_GOOD_IMAGE
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.mapFailureMessage
import com.phew.core_common.resultFailure
import com.phew.domain.BuildConfig
import com.phew.domain.CROP_FILE
import com.phew.domain.dto.CardDefaultImagesResponse
import com.phew.domain.dto.CardReplyRequest
import com.phew.domain.dto.Location
import com.phew.domain.dto.TagInfo
import com.phew.domain.model.write.WriteCardParam
import com.phew.domain.model.write.WriteReplyParam
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.domain.repository.network.MembersRepository
import com.phew.domain.safeUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * 글쓰기 화면의 위치, 이미지, 태그 추천, 카드/댓글 작성, 작성 이벤트 기록 흐름을 조율합니다.
 */
class WriteUseCaseOrchestrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cardFeedRepository: CardFeedRepository,
    private val cardDetailRepository: CardDetailRepository,
    private val deviceRepository: DeviceRepository,
    private val eventRepository: EventRepository,
    private val membersRepository: MembersRepository,
) {
    suspend fun locationOrEmpty(): Location =
        try {
            deviceRepository.requestLocation()
        } catch (e: Exception) {
            Location.EMPTY
        }

    fun createImageFile(): Result<Uri> {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpeg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourApp")
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = context.contentResolver.insert(collection, values) ?: return resultFailure(ERROR_FAIL_JOB)
        return Result.success(uri)
    }

    fun finishTakePicture(uri: Uri): Result<Uri> {
        if (uri.scheme == CROP_FILE) {
            return Result.success(uri)
        }
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(uri, contentValues, null, null)
            Result.success(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            resultFailure(ERROR_FAIL_JOB)
        }
    }

    suspend fun relatedTags(tag: String, resultCount: Int): Result<List<TagInfo>> =
        cardFeedRepository.requestRelatedTag(resultCnt = resultCount, tag = tag)
            .mapFailureMessage { code, _ ->
                when (code) {
                    APP_ERROR_CODE -> ERROR_FAIL_JOB
                    HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                    else -> ERROR_NETWORK
                }
            }

    suspend fun cardDefaultImages(): Result<CardDefaultImagesResponse> =
        safeUseCase(
            apiCall = { cardFeedRepository.requestCardImageDefault() },
            mapper = { result -> result },
        )

    suspend fun checkCardDeleted(cardId: Long): Result<Boolean> =
        cardFeedRepository.requestCheckCardDelete(cardId = cardId)
            .mapFailureMessage { _, message -> message.ifBlank { ERROR_NETWORK } }

    suspend fun activityRestrictionDate(): Result<String?> =
        try {
            membersRepository.getActivityRestrictionDate().fold(
                onSuccess = { Result.success(it) },
                onFailure = { resultFailure(Unit) },
            )
        } catch (e: Exception) {
            resultFailure(Unit)
        }

    suspend fun refreshToken(): String {
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        return token.first.takeUnless { it == ERROR_NO_DATA } ?: ""
    }

    suspend fun postCard(param: WriteCardParam): Result<Long> {
        if (param.content.trim().isEmpty()) return resultFailure(ERROR_FAIL_JOB)
        val checkedPostBanned = cardFeedRepository.requestCheckUploadCard()
            .getOrElse { return resultFailure(ERROR_FAIL_JOB) }
        if (checkedPostBanned.isBaned) {
            return resultFailure(checkedPostBanned.viewTime)
        }
        val imageInfoResult = if (param.isFromDevice) {
            getFeedImageInfoFromDevice(param.imageUrl)
        } else {
            param.imgName?.let { imageName ->
                Result.success(UploadImageInfo(imageName, IMAGE_TYPE_DEFAULT))
            } ?: resultFailure(ERROR_FAIL_JOB)
        }
        val imageInfo = imageInfoResult.getOrElse { return Result.failure(it) }
        return uploadCardData(param = param, imageInfo = imageInfo)
    }

    suspend fun postCardReply(param: WriteReplyParam): Result<Long> {
        if (param.content.trim().isEmpty()) return resultFailure(ERROR_FAIL_JOB)
        val imageInfoResult = when (param.imgType) {
            IMAGE_TYPE_USER -> getReplyImageInfoFromDevice(param.imageUrl)
            else -> Result.success(UploadImageInfo(param.imgName, IMAGE_TYPE_DEFAULT))
        }
        val imageInfo = imageInfoResult.getOrElse { return Result.failure(it) }
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
            tags = param.tags,
        )
        return cardDetailRepository.postCardReply(param.cardId, request).fold(
            onSuccess = { result ->
                eventRepository.logWriteCardClickFinishButton()
                if (!locationPermissionCheck) eventRepository.logWriteDistanceSharedOff()
                Result.success(result.cardId)
            },
            onFailure = { throwable ->
                val exception = throwable.asSooumException()
                when (exception.code) {
                    APP_ERROR_CODE -> resultFailure(exception.message.ifBlank { ERROR_FAIL_JOB })
                    HTTP_BAD_REQUEST -> resultFailure(ERROR_ACCOUNT_SUSPENDED)
                    HTTP_CARD_ALREADY_DELETE -> resultFailure(ERROR_ALREADY_CARD_DELETE)
                    else -> resultFailure(ERROR_FAIL_JOB)
                }
            },
        )
    }

    suspend fun logBottomWriteClick() = eventRepository.logWriteBottomAddCard()

    suspend fun logWriteTagClickEnter() = eventRepository.logWriteTagWriteFinishWithEnter()

    suspend fun logFeedBackHandler() = eventRepository.logWriteBackToFeedCard()

    suspend fun logCommentBackHandler() = eventRepository.logWriteBackCommentCard()

    suspend fun logFeedBackgroundChange() = eventRepository.logWriteCountBackgroundChange()

    suspend fun logCommentBackgroundChange() = eventRepository.logWriteCommentCardBackgroundChange()

    suspend fun logWriteEventCard() = eventRepository.logWriteSelectEventTab()

    private suspend fun uploadCardData(param: WriteCardParam, imageInfo: UploadImageInfo): Result<Long> {
        val locationPermissionCheck = deviceRepository.getLocationPermission()
        val (latitude, longitude) = if (locationPermissionCheck && param.isDistanceShared) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }
        val uploadResult = if (!param.answerCard) {
            cardFeedRepository.requestUploadCard(
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
                pollContents = param.pollContents,
            )
        } else {
            if (param.cardId == null) return resultFailure(ERROR_FAIL_JOB)
            cardFeedRepository.requestUploadCardAnswer(
                cardId = param.cardId,
                content = param.content,
                font = param.font,
                imageName = imageInfo.name,
                imageType = imageInfo.type,
                isDistanceShared = locationPermissionCheck,
                latitude = latitude,
                longitude = longitude,
                tag = param.tags,
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

    private suspend fun getFeedImageInfoFromDevice(imageUrl: String?): Result<UploadImageInfo> {
        if (imageUrl == null) return resultFailure(ERROR_FAIL_JOB)
        val uploadInfo = cardFeedRepository.requestUploadCardImage()
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
        return cardFeedRepository.requestUploadImage(data = file, url = uploadInfo.url).fold(
            onSuccess = { Result.success(UploadImageInfo(uploadInfo.imageName, IMAGE_TYPE_USER)) },
            onFailure = { resultFailure(ERROR_NETWORK) },
        )
    }

    private suspend fun getReplyImageInfoFromDevice(imageUrl: String?): Result<UploadImageInfo> {
        if (imageUrl == null) return resultFailure(ERROR_FAIL_JOB)
        val uploadInfo = cardFeedRepository.requestUploadCardImage()
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
        return cardFeedRepository.requestUploadImage(data = file, url = uploadInfo.url).fold(
            onSuccess = { Result.success(UploadImageInfo(uploadInfo.imageName, IMAGE_TYPE_USER)) },
            onFailure = { resultFailure(ERROR_NETWORK) },
        )
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

    private companion object {
        private const val IMAGE_TYPE_DEFAULT = "DEFAULT"
        private const val IMAGE_TYPE_USER = "USER"
    }
}
