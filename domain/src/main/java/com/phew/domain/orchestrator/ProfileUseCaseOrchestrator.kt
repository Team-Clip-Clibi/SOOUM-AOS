package com.phew.domain.orchestrator

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.paging.PagingData
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_FAIL_PACKAGE_IMAGE
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_UN_GOOD_IMAGE
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NOT_FOUND
import com.phew.core_common.HTTP_UN_GOOD_IMAGE
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.mapFailureMessage
import com.phew.core_common.mapResult
import com.phew.core_common.resultFailure
import com.phew.domain.CROP_FILE
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.ProfileCard
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.model.profile.ProfileUpdateParam
import com.phew.domain.repository.PagerRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.domain.repository.network.ProfileRepository
import com.phew.domain.repository.network.SignUpRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * 프로필 화면에서 필요한 조회, 팔로우/차단, 이미지 처리, 프로필 수정 흐름을 한 곳에서 조율합니다.
 */
class ProfileUseCaseOrchestrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
    private val profileRepository: ProfileRepository,
    private val pagerRepository: PagerRepository,
    private val signUpRepository: SignUpRepository,
    private val cardFeedRepository: CardFeedRepository,
) {
    suspend fun myProfile(): Result<ProfileInfo> =
        profileRepository.requestMyProfile().mapFailureMessage { _, message ->
            message.ifBlank { ERROR_NETWORK }
        }

    suspend fun otherProfile(profileId: Long): Result<ProfileInfo> =
        profileRepository.requestOtherProfile(profileId = profileId)
            .mapFailureMessage { _, message -> message.ifBlank { ERROR_NETWORK } }

    fun profileFeedCards(userId: Long): Flow<PagingData<ProfileCard>> =
        pagerRepository.profileFeedCard(userId = userId)

    fun profileCommentCards(): Flow<PagingData<ProfileCard>> =
        pagerRepository.profileCommentCard()

    fun followers(profileId: Long): Flow<PagingData<FollowData>> =
        pagerRepository.follower(profileId = profileId)

    fun followings(profileId: Long): Flow<PagingData<FollowData>> =
        pagerRepository.following(profileId = profileId)

    suspend fun followUser(userId: Long): Result<Unit> =
        profileRepository.requestFollowUser(profileId = userId).mapProfileActionResult()

    suspend fun unFollowUser(userId: Long): Result<Unit> =
        profileRepository.requestUnFollowUser(profileId = userId).mapProfileActionResult()

    suspend fun blockUser(userId: Long): Result<Unit> =
        profileRepository.requestBlockUser(profileId = userId).mapProfileActionResult()

    suspend fun unBlockUser(userId: Long): Result<Unit> =
        profileRepository.requestUnBlockUser(profileId = userId).mapProfileActionResult()

    suspend fun checkNickName(nickName: String): Result<Boolean> =
        signUpRepository.requestCheckNickName(nickName)
            .mapFailureMessage { _, _ -> ERROR_NETWORK }

    fun createImageFile(): Result<Uri> {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpeg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourApp")
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = context.contentResolver.insert(collection, values)
            ?: return resultFailure(ERROR_FAIL_JOB)
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

    suspend fun updateProfile(param: ProfileUpdateParam): Result<Unit> {
        if (!param.isImageChange) {
            return handleUpdateProfileResult(
                profileRepository.requestUpdateProfile(
                    nickName = param.nickName,
                    profileImageName = param.imgName,
                    profileBio = param.profileBio,
                ),
            )
        }

        if (param.profileImage == null) {
            return handleUpdateProfileResult(
                profileRepository.requestUpdateProfile(
                    nickName = param.nickName,
                    profileImageName = null,
                    profileBio = param.profileBio,
                ),
            )
        }

        val imageUrl = profileRepository.requestUploadImageUrl()
            .getOrElse { return resultFailure(ERROR_NETWORK) }
        val file = try {
            contentResolver.readAsCompressedJpegRequestBody(uri = param.profileImage.toUri())
        } catch (e: Exception) {
            e.printStackTrace()
            return resultFailure(ERROR_FAIL_PACKAGE_IMAGE)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return resultFailure(ERROR_FAIL_JOB)
        }
        val requestUploadImage = profileRepository.requestUploadImage(
            uri = imageUrl.imgUrl,
            body = file,
        )
        requestUploadImage.exceptionOrNull()?.asSooumException()?.let { exception ->
            return when (exception.code) {
                HTTP_NOT_FOUND -> resultFailure(ERROR_NETWORK)
                else -> resultFailure(ERROR_FAIL_JOB)
            }
        }
        return handleUpdateProfileResult(
            profileRepository.requestUpdateProfile(
                nickName = param.nickName,
                profileImageName = imageUrl.imgName,
                profileBio = param.profileBio,
            ),
        )
    }

    suspend fun checkCardDeleted(cardId: Long): Result<Boolean> =
        cardFeedRepository.requestCheckCardDelete(cardId = cardId)
            .mapFailureMessage { _, message -> message.ifBlank { ERROR_NETWORK } }

    suspend fun checkIsMyProfile(userId: Long, nickName: String): Result<Pair<Boolean, Long>> =
        profileRepository.requestMyProfile().mapResult(
            success = { profile ->
                if (profile.nickname != nickName || profile.userId != userId) {
                    Pair(false, userId)
                } else {
                    Pair(true, profile.userId)
                }
            },
        ) { _, message -> message.ifBlank { ERROR_FAIL_JOB } }

    private fun Result<Boolean>.mapProfileActionResult(): Result<Unit> =
        mapResult(
            success = { Unit },
        ) { code, _ ->
            if (code == HTTP_INVALID_TOKEN) ERROR_LOGOUT else ERROR_NETWORK
        }

    private fun handleUpdateProfileResult(request: Result<Unit>): Result<Unit> =
        request.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { throwable ->
                when (throwable.asSooumException().code) {
                    HTTP_INVALID_TOKEN -> resultFailure(ERROR_LOGOUT)
                    HTTP_NOT_FOUND -> resultFailure(ERROR_NETWORK)
                    HTTP_UN_GOOD_IMAGE -> resultFailure(ERROR_UN_GOOD_IMAGE)
                    else -> resultFailure(ERROR_FAIL_JOB)
                }
            },
        )

    private fun ContentResolver.readAsCompressedJpegRequestBody(uri: Uri): RequestBody {
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
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
