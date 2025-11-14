package com.phew.domain.repository

import androidx.paging.PagingData
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.dto.Notification
import com.phew.domain.dto.ProfileCard
import com.phew.domain.model.BlockMember
import kotlinx.coroutines.flow.Flow

interface PagerRepository {
    fun noticePageStream(source: NoticeSource = NoticeSource.SETTINGS): Flow<PagingData<Notice>>
    fun notificationUnRead(): Flow<PagingData<Notification>>
    fun notificationRead(): Flow<PagingData<Notification>>
    fun cardComments(
        cardId: Long,
        latitude: Double?,
        longitude: Double?,
    ): Flow<PagingData<CardComment>>

    fun profileFeedCard(userId: Long): Flow<PagingData<ProfileCard>>
    fun profileCommentCard(): Flow<PagingData<ProfileCard>>
    fun follower(profileId: Long): Flow<PagingData<FollowData>>
    fun following(profileId: Long): Flow<PagingData<FollowData>>
    fun getBlockUserPaging(): Flow<PagingData<BlockMember>>
}
