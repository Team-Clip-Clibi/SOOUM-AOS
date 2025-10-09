package com.phew.domain.usecase

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_UN_GOOD_IMAGE
import com.phew.core_common.HTTP_SUCCESS
import com.phew.core_common.HTTP_UN_GOOD_IMAGE
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.CardFeedRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import javax.inject.Inject

class PostCard @Inject constructor(
    private val networkRepository: CardFeedRepository,
    @ApplicationContext private val context: Context,
    private val deviceRepository: DeviceRepository
) {
    data class Param(
        val isFromDevice: Boolean, //핸드폰 디바이스에서 사진 선택했는지
        val answerCard: Boolean, // 답 카드 인지
        val cardId: Int?,
        val imageUrl: String?,
        val content: String,
        val font: String,
        var imgName: String?,
        val isStory: Boolean?,
        val tags: List<String>
    )

    suspend operator fun invoke(data: Param): DomainResult<Unit, String> {
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
        imageInfo: UploadImageInfo
    ): DomainResult<Unit, String> {
        val locationPermissionCheck = deviceRepository.getLocationPermission()
        val (latitude, longitude) = if (locationPermissionCheck) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }
        val requestUploadCard = if (!param.answerCard) {
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
            if (param.cardId == null) DomainResult.Failure(ERROR_FAIL_JOB)
            networkRepository.requestUploadCardAnswer(
                cardId = param.cardId!!,
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
        return if (requestUploadCard == HTTP_SUCCESS) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(ERROR_FAIL_JOB)
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
                val file = context.contentResolver.readAsRequestBody(uri = imageUrl.toUri())
                when (val uploadResult =
                    networkRepository.requestUploadImage(data = file, url = uploadInfo.url)) {
                    is DataResult.Fail -> {
                        if (uploadResult.code == HTTP_UN_GOOD_IMAGE) {
                            return DomainResult.Failure(ERROR_UN_GOOD_IMAGE)
                        }
                        return DomainResult.Failure(ERROR_FAIL_JOB)
                    }

                    is DataResult.Success -> DomainResult.Success(
                        UploadImageInfo(
                            uploadInfo.imageName,
                            "USER"
                        )
                    )
                }
            }
        }
    }

    private data class UploadImageInfo(val name: String, val type: String)

    private fun ContentResolver.readAsRequestBody(uri: Uri): RequestBody =
        object : RequestBody() {
            override fun contentType(): MediaType? =
                this@readAsRequestBody.getType(uri)?.toMediaTypeOrNull()


            override fun writeTo(sink: BufferedSink) {
                this@readAsRequestBody.openInputStream(uri)?.source()?.use(sink::writeAll)
            }

            override fun contentLength(): Long =
                this@readAsRequestBody.query(uri, null, null, null, null)?.use { cursor ->
                    val sizeColumnIndex: Int = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    cursor.getLong(sizeColumnIndex)
                } ?: super.contentLength()
        }
}