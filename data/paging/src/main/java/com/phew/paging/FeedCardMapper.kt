package com.phew.paging

import com.phew.domain.dto.DistanceCard
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Popular

internal fun Latest.toFeedCardType(): FeedCardType = toFeedCardType(
    cardId = cardId,
    storyExpirationTime = storyExpirationTime,
    content = cardContent,
    imageUrl = cardImgUrl,
    imageName = cardImageName,
    font = font,
    location = distance,
    writeTime = createAt,
    commentValue = commentCardCount.toString(),
    likeValue = likeCount.toString(),
    isLike = isLike,
    isAdminCard = isAdminCard
)

internal fun Popular.toFeedCardType(): FeedCardType = toFeedCardType(
    cardId = cardId,
    storyExpirationTime = storyExpirationTime,
    content = cardContent,
    imageUrl = cardImgUrl,
    imageName = cardImageName,
    font = font,
    location = distance,
    writeTime = createAt,
    commentValue = commentCardCount.toString(),
    likeValue = likeCount.toString(),
    isLike = isLike,
    isAdminCard = isAdminCard
)

internal fun DistanceCard.toFeedCardType(): FeedCardType = toFeedCardType(
    cardId = cardId,
    storyExpirationTime = storyExpirationTime,
    content = cardContent,
    imageUrl = cardImgUrl,
    imageName = cardImageName,
    font = font,
    location = distance,
    writeTime = createAt,
    commentValue = commentCardCount.toString(),
    likeValue = likeCount.toString(),
    isLike = isLike,
    isAdminCard = isAdminCard
)

private fun toFeedCardType(
    cardId: String,
    storyExpirationTime: String?,
    content: String,
    imageUrl: String,
    imageName: String,
    font: String,
    location: String?,
    writeTime: String,
    commentValue: String,
    likeValue: String,
    isLike: Boolean,
    isAdminCard: Boolean,
): FeedCardType {
    return when {
        !storyExpirationTime.isNullOrEmpty() -> FeedCardType.BoombType(
            cardId = cardId,
            storyExpirationTime = storyExpirationTime,
            content = content,
            imageUrl = imageUrl,
            imageName = imageName,
            font = font,
            location = location,
            writeTime = writeTime,
            commentValue = commentValue,
            likeValue = likeValue,
            isLike = isLike,
        )

        isAdminCard -> FeedCardType.AdminType(
            cardId = cardId,
            content = content,
            imageUrl = imageUrl,
            imageName = imageName,
            font = font,
            location = location,
            writeTime = writeTime,
            commentValue = commentValue,
            likeValue = likeValue,
            isLike = isLike,
        )

        else -> FeedCardType.NormalType(
            cardId = cardId,
            content = content,
            imageUrl = imageUrl,
            imageName = imageName,
            font = font,
            location = location,
            writeTime = writeTime,
            commentValue = commentValue,
            likeValue = likeValue,
            isLike = isLike,
        )
    }
}
