package com.phew.domain.usecase.orchestrator

import android.net.Uri
import androidx.paging.PagingData
import com.phew.core_common.DomainResult
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.ProfileCard
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.usecase.CheckCardAlreadyDelete
import com.phew.domain.usecase.CheckIsMyProfile
import com.phew.domain.usecase.CheckNickName
import com.phew.domain.usecase.CreateImageFile
import com.phew.domain.usecase.FinishTakePicture
import com.phew.domain.usecase.GetFollower
import com.phew.domain.usecase.GetFollowing
import com.phew.domain.usecase.GetMyProfileInfo
import com.phew.domain.usecase.GetOtherProfile
import com.phew.domain.usecase.GetProfileCommentCard
import com.phew.domain.usecase.GetProfileFeedCard
import com.phew.domain.usecase.SendBlockUser
import com.phew.domain.usecase.SendFollowUser
import com.phew.domain.usecase.SendUnBlockUser
import com.phew.domain.usecase.SendUnFollowUser
import com.phew.domain.usecase.UpdateProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileUseCaseOrchestrator @Inject constructor(
    private val getMyProfileInfo: GetMyProfileInfo,
    private val getProfileFeedCard: GetProfileFeedCard,
    private val getProfileCommentCard: GetProfileCommentCard,
    private val sendUnFollowUser: SendUnFollowUser,
    private val getFollower: GetFollower,
    private val getFollowing: GetFollowing,
    private val getOtherProfile: GetOtherProfile,
    private val sendFollowUser: SendFollowUser,
    private val sendBlockUser: SendBlockUser,
    private val sendUnBlockUser: SendUnBlockUser,
    private val checkNickName: CheckNickName,
    private val createImageFile: CreateImageFile,
    private val finishTakePicture: FinishTakePicture,
    private val updateProfile: UpdateProfile,
    private val checkCardAlreadyDelete: CheckCardAlreadyDelete,
    private val checkIsMyProfile: CheckIsMyProfile,
) {
    suspend fun myProfile(): DomainResult<ProfileInfo, String> = getMyProfileInfo()

    suspend fun otherProfile(profileId: Long): DomainResult<ProfileInfo, String> =
        getOtherProfile(GetOtherProfile.Param(profileId = profileId))

    fun profileFeedCards(userId: Long): Flow<PagingData<ProfileCard>> =
        getProfileFeedCard(userId = userId)

    fun profileCommentCards(): Flow<PagingData<ProfileCard>> = getProfileCommentCard()

    fun followers(profileId: Long): Flow<PagingData<FollowData>> =
        getFollower(profileId = profileId)

    fun followings(profileId: Long): Flow<PagingData<FollowData>> =
        getFollowing(profileId = profileId)

    suspend fun followUser(userId: Long): DomainResult<Unit, String> =
        sendFollowUser(SendFollowUser.Param(userId = userId))

    suspend fun unFollowUser(userId: Long): DomainResult<Unit, String> =
        sendUnFollowUser(SendUnFollowUser.Param(userId = userId))

    suspend fun blockUser(userId: Long): DomainResult<Unit, String> =
        sendBlockUser(SendBlockUser.Param(userId = userId))

    suspend fun unBlockUser(userId: Long): DomainResult<Unit, String> =
        sendUnBlockUser(SendUnBlockUser.Param(userId = userId))

    suspend fun checkNickName(nickName: String): DomainResult<Boolean, String> =
        checkNickName(CheckNickName.Param(nickName = nickName))

    fun createImageFile(): DomainResult<Uri, String> = createImageFile()

    fun finishTakePicture(uri: Uri): DomainResult<Uri, String> =
        finishTakePicture(FinishTakePicture.Param(uri))

    suspend fun updateProfile(param: UpdateProfile.Param): DomainResult<Unit, String> =
        updateProfile(param)

    suspend fun checkCardDeleted(cardId: Long): DomainResult<Boolean, String> =
        checkCardAlreadyDelete(CheckCardAlreadyDelete.Param(cardId = cardId))

    suspend fun checkIsMyProfile(userId: Long, nickName: String): DomainResult<Pair<Boolean, Long>, String> =
        checkIsMyProfile(CheckIsMyProfile.Param(userId = userId, nickName = nickName))
}
