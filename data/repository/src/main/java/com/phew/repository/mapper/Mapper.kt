package com.phew.repository.mapper

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_ACCOUNT_SUSPENDED
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.TimeUtils
import com.phew.core_common.WITHDRAWAL_USER
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.dto.CardDetailTag
import com.phew.domain.dto.CardImageDefault
import com.phew.domain.dto.CheckSignUp
import com.phew.domain.dto.CheckedBaned
import com.phew.domain.dto.DistanceCard
import com.phew.domain.dto.FavoriteTag
import com.phew.domain.dto.FavoriteTagList
import com.phew.domain.dto.FeedLikeNotification
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.FollowNotification
import com.phew.domain.dto.Latest
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.dto.Popular
import com.phew.domain.dto.ProfileCard
import com.phew.domain.dto.TagInfo
import com.phew.domain.dto.Token
import com.phew.domain.dto.UploadImageUrl
import com.phew.domain.dto.UserBlockNotification
import com.phew.domain.dto.UserCommentLike
import com.phew.domain.dto.UserCommentWrite
import com.phew.domain.dto.UserDeleteNotification
import com.phew.domain.dto.TagCardContent
import com.phew.domain.model.AppVersionStatus
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.model.BlockMember
import com.phew.domain.model.RejoinableDate
import com.phew.domain.model.TransferCode
import com.phew.domain.model.TagInfo as DomainTagInfo
import com.phew.domain.model.TagInfoList
import com.phew.domain.model.TagCards
import com.phew.domain.model.CardContent
import com.phew.network.dto.AppVersionStatusDTO
import com.phew.network.dto.CheckSignUpDTO
import com.phew.network.dto.TransferCodeDTO
import com.phew.network.dto.NoticeData
import com.phew.network.dto.NotificationDTO
import com.phew.network.dto.TokenDTO
import com.phew.network.dto.UploadImageUrlDTO
import com.phew.network.dto.response.BlockMemberResponseDTO
import com.phew.network.dto.response.FavoriteTagItemDTO
import com.phew.network.dto.response.FavoriteTagsResponseDTO
import com.phew.network.dto.response.RejoinableDateResponseDTO
import com.phew.network.dto.response.NotifyToggleResponseDTO
import com.phew.network.dto.request.feed.CheckBanedDTO
import com.phew.network.dto.request.feed.ImageInfoDTO
import com.phew.network.dto.request.feed.TagInfoDTO
import com.phew.network.dto.request.feed.TagInfoListDTO
import com.phew.network.dto.response.TagCardsResponseDTO
import com.phew.network.dto.response.CardContentDTO
import com.phew.network.dto.response.DistanceDTO
import com.phew.network.dto.response.LatestDto
import com.phew.network.dto.response.PopularDto
import com.phew.network.dto.response.card.CardCommentResponseDTO
import com.phew.network.dto.response.card.CardContentDto
import com.phew.network.dto.response.card.CardDetailResponseDTO
import com.phew.network.dto.response.card.CardDetailTagDTO
import com.phew.network.dto.response.profile.FollowDataDTO
import com.phew.network.dto.response.profile.ProfileDTO
import com.phew.repository.TYPE_BLOCK
import com.phew.repository.TYPE_COMMENT_LIKE
import com.phew.repository.TYPE_COMMENT_WRITE
import com.phew.repository.TYPE_DELETE
import com.phew.repository.TYPE_FEED_LIKE
import com.phew.repository.TYPE_FOLLOW
import retrofit2.Response

internal fun NotificationDTO.toDomain(): Notification {
    return when (this.notificationType) {
        TYPE_FEED_LIKE -> {
            FeedLikeNotification(
                createTime = this.createTime,
                notificationId = this.notificationId,
                nickName = this.nickName ?: "error_nickName",
                targetCardId = this.targetCardId ?: -1,
                userId = this.userId ?: -1
            )
        }

        TYPE_FOLLOW -> {
            FollowNotification(
                notificationId = this.notificationId,
                createTime = this.createTime,
                nickName = this.nickName ?: "error_nickName",
                userId = this.userId ?: -1
            )
        }

        TYPE_COMMENT_LIKE -> {
            UserCommentLike(
                notificationId = this.notificationId,
                createTime = this.createTime,
                nickName = this.nickName ?: "error_nickname",
                targetCardId = this.targetCardId ?: -1,
                userId = this.userId ?: -1
            )
        }

        TYPE_COMMENT_WRITE -> {
            UserCommentWrite(
                notificationId = this.notificationId,
                createTime = this.createTime,
                targetCardId = this.targetCardId ?: -1,
                userId = this.userId ?: -1,
                nickName = nickName ?: "error_nickname"
            )
        }

        TYPE_BLOCK -> {
            UserBlockNotification(
                notificationId = this.notificationId,
                createTime = this.createTime,
                blockExpirationDateTime = this.blockExpirationDateTime ?: "error_block_time"
            )
        }

        TYPE_DELETE -> {
            UserDeleteNotification(
                notificationId = this.notificationId,
                createTime = this.createTime,
            )
        }

        else -> throw IllegalArgumentException("Unknown type")
    }
}

internal fun PopularDto.toDomain(): Popular {
    return Popular(
        cardId = this.cardId,
        likeCount = this.likeCount,
        commentCardCount = this.commentCardCount,
        cardImgUrl = this.cardImgUrl,
        cardImageName = this.cardImageName,
        cardContent = this.cardContent,
        font = this.font,
        distance = this.distance,
        createAt = TimeUtils.getRelativeTimeString(this.createAt),
        storyExpirationTime = this.storyExpirationTime,
        isAdminCard = this.isAdminCard
    )
}

internal fun NoticeData.toDomain(): Notice {
    return Notice(
        content = this.title,
        url = this.url ?: "",
        createdAt = this.createdAt,
        id = this.id,
        noticeType = this.noticeType
    )
}

internal fun LatestDto.toDomain(): Latest {
    return Latest(
        cardId = this.cardId,
        likeCount = this.likeCount,
        commentCardCount = this.commentCardCount,
        cardImgUrl = this.cardImgUrl,
        cardImageName = this.cardImageName,
        cardContent = this.cardContent,
        font = this.font,
        distance = this.distance,
        createAt = TimeUtils.getRelativeTimeString(this.createAt),
        storyExpirationTime = this.storyExpirationTime,
        isAdminCard = this.isAdminCard
    )
}

internal fun DistanceDTO.toDomain(): DistanceCard {
    return DistanceCard(
        cardId = this.cardId,
        likeCount = this.likeCount,
        commentCardCount = this.commentCardCount,
        cardImgUrl = this.cardImgUrl,
        cardImageName = this.cardImageName,
        cardContent = this.cardContent,
        font = this.font,
        distance = this.distance,
        createAt = TimeUtils.getRelativeTimeString(this.createAt),
        storyExpirationTime = this.storyExpirationTime,
        isAdminCard = this.isAdminCard
    )
}

internal fun CheckSignUpDTO.toDomain(): CheckSignUp {
    return CheckSignUp(
        time = this.rejoinAvailableAt ?: "",
        banned = this.banned,
        withdrawn = this.withdrawn,
        registered = this.registered
    )
}

internal fun TokenDTO.toDomain(): Token {
    return Token(
        refreshToken = this.refreshToken,
        accessToken = this.accessToken
    )
}

internal fun UploadImageUrlDTO.toDomain(): UploadImageUrl {
    return UploadImageUrl(
        imgUrl = this.imgUrl,
        imgName = this.imgName
    )
}

internal fun TagInfoDTO.toDomain(): TagInfo {
    return TagInfo(
        id = this.id,
        name = this.name,
        usageCnt = this.usageCnt
    )
}

internal fun ImageInfoDTO.toDomain(): CardImageDefault {
    return CardImageDefault(
        imageName = this.imgName,
        url = this.url
    )
}

internal fun CheckBanedDTO.toDomain(): CheckedBaned {
    return CheckedBaned(
        isBaned = this.isBaned,
        expiredAt = this.expiredAt
    )
}

internal fun AppVersionStatusDTO.toDomain(): AppVersionStatus {
    return AppVersionStatus(
        status = when (this.status) {
            com.phew.network.dto.AppVersionStatus.UPDATE -> AppVersionStatusType.UPDATE
            com.phew.network.dto.AppVersionStatus.PENDING -> AppVersionStatusType.PENDING
            com.phew.network.dto.AppVersionStatus.OK -> AppVersionStatusType.OK
        },
        latestVersion = this.latestVersion
    )
}

internal fun TransferCodeDTO.toDomain(): TransferCode {
    return TransferCode(
        transferCode = this.transferCode,
        expiredAt = this.expiredAt
    )
}

internal fun RejoinableDateResponseDTO.toDomain(): RejoinableDate {
    return RejoinableDate(
        rejoinableDate = this.rejoinableDate,
        isActivityRestricted = this.isActivityRestricted
    )
}

internal fun BlockMemberResponseDTO.toDomain(): BlockMember {
    return BlockMember(
        blockId = this.blockId,
        blockMemberId = this.blockMemberId,
        blockMemberNickname = this.blockMemberNickname,
        blockMemberProfileImageUrl = this.blockMemberProfileImageUrl
    )
}

internal fun CardDetailResponseDTO.toDomain(): CardDetail {
    return CardDetail(
        cardId = cardId,
        likeCount = likeCnt,
        commentCardCount = commentCardCnt,
        cardImgUrl = cardImgUrl,
        cardImgName = cardImgName,
        cardContent = cardContent,
        font = font,
        distance = distance,
        createdAt = createdAt,
        isAdminCard = isAdminCard,
        memberId = memberId,
        nickname = nickname,
        profileImgUrl = profileImgUrl,
        isLike = isLike,
        isCommentWritten = isCommentWritten,
        tags = tags.map { it.toDomain() },
        isOwnCard = isOwnCard,
        previousCardId = previousCardId,
        previousCardImgUrl = previousCardImgUrl,
        visitedCnt = visitedCnt,
        isFeedCard = isFeedCard,
        storyExpirationTime = storyExpirationTime
    )
}

internal fun CardDetailTagDTO.toDomain(): CardDetailTag {
    return CardDetailTag(
        tagId = tagId,
        name = name
    )
}

internal fun CardCommentResponseDTO.toDomain(): CardComment {
    return CardComment(
        cardId = cardId,
        likeCount = likeCnt,
        commentCardCount = commentCardCnt,
        cardImgUrl = cardImgUrl,
        cardImgName = cardImgName,
        cardContent = cardContent,
        font = font,
        distance = distance,
        createdAt = createdAt,
        isAdminCard = isAdminCard
    )
}

internal fun ProfileDTO.toDomain() : ProfileInfo{
    return ProfileInfo(
        cardCnt = this.cardCnt,
        followingCnt = this.followingCnt,
        followerCnt = this.followerCnt,
        nickname = this.nickname,
        profileImageUrl = this.profileImageUrl ?: "",
        profileImgName = this.profileImgName ?: "",
        todayVisitCnt = this.todayVisitCnt,
        totalVisitCnt = this.totalVisitCnt,
        userId = this.userId,
        isBlocked = this.isBlocked,
        isAlreadyFollowing = this.isAlreadyFollowing
    )
}

internal fun CardContentDto.toDomain(): ProfileCard {
    return ProfileCard(
        cardId = this.cardId,
        cardImgUrl = this.cardImgUrl ?: "",
        cardContent = this.cardContent ?: "",
        cardImgName = this.cardImgName ?: "",
        font = this.font
    )
}

internal fun FollowDataDTO.toDomain(): FollowData {
    return FollowData(
        followId = this.followId,
        memberId = this.memberId,
        nickname = this.nickname,
        profileImageUrl = this.profileImageUrl,
        isFollowing = this.isFollowing,
        isRequester = this.isRequester
    )
}

suspend fun <T, R> apiCall(
    apiCall: suspend () -> Response<T>,
    mapper: (T) -> R,
): DataResult<R> {
    try {
        val response = apiCall()
        if (!response.isSuccessful) {
            return when (response.code()) {
                WITHDRAWAL_USER -> DataResult.Fail(
                    code = response.code(),
                    message = ERROR_ACCOUNT_SUSPENDED,
                    throwable = Exception("Account suspended - Error code 418")
                )
                else -> DataResult.Fail(
                    code = response.code(),
                    message = response.message()
                )
            }
        }

        val body = response.body()
            ?: return DataResult.Fail(
                code = response.code(),
                message = "Response body is null or empty"
            )

        return DataResult.Success(mapper(body))
    } catch (e: Exception) {
        e.printStackTrace()
        return DataResult.Fail(code = APP_ERROR_CODE, message = e.message, throwable = e)
    }
}

/**
 * T: Retrofit이 반환하는 DTO 타입 (e.g., List<NotificationDto> 또는 CardContentsResponse)
 * R: 최종적으로 사용할 Domain 모델의 *아이템* 타입 (e.g., Notification 또는 ProfileCard)
 */
suspend fun <T, R> pagingCall(
    apiCall: suspend () -> Response<T>,
    mapper: (T) -> List<R>,
): DataResult<Pair<Int, List<R>>> {
    try {
        val response = apiCall()
        if (!response.isSuccessful) {
            return DataResult.Fail(code = response.code(), message = response.message())
        }
        if (response.body() == null && response.code() == HTTP_NO_MORE_CONTENT) {
            return DataResult.Success(Pair(response.code(), emptyList()))
        }
        val body = response.body()
            ?: return DataResult.Fail(
                code = response.code(),
                message = ERROR_NETWORK
            )
        val domainList = mapper(body)
        if (domainList.isEmpty()) {
            return DataResult.Success(Pair(response.code(), emptyList()))
        }
        return DataResult.Success(Pair(response.code(), domainList))
    } catch (e: Exception) {
        e.printStackTrace()
        return DataResult.Fail(
            code = APP_ERROR_CODE,
            message = e.message,
            throwable = e
        )
    }
}

internal fun TagInfoListDTO.toDomainModel(): TagInfoList {
    return TagInfoList(
        tagInfos = this.tagInfo.map { it.toDomainModel() }
    )
}

internal fun TagInfoDTO.toDomainModel(): DomainTagInfo {
    return DomainTagInfo(
        id = this.id,
        name = this.name,
        usageCnt = this.usageCnt
    )
}

internal fun TagCardsResponseDTO.toDomainModel(): TagCards {
    return TagCards(
        cardContents = this.cardContents.map { it.toDomainModel() },
        isFavorite = this.isFavorite
    )
}

internal fun CardContentDTO.toDomainModel(): CardContent {
    return CardContent(
        cardId = this.cardId,
        cardImgName = this.cardImgName,
        cardImgUrl = this.cardImgUrl,
        cardContent = this.cardContent,
        font = this.font
    )
}

internal fun CardContentDTO.toDomain(): TagCardContent {
    return TagCardContent(
        cardId = this.cardId,
        cardImgName = this.cardImgName,
        cardImgUrl = this.cardImgUrl,
        cardContent = this.cardContent,
        font = this.font
    )
}

internal fun FavoriteTagsResponseDTO.toDomain(): FavoriteTagList {
    return FavoriteTagList(
        favoriteTags = this.favoriteTags.map { it.toDomain() }
    )
}

internal fun FavoriteTagItemDTO.toDomain(): FavoriteTag {
    return FavoriteTag(
        id = this.id,
        name = this.name
    )
}

