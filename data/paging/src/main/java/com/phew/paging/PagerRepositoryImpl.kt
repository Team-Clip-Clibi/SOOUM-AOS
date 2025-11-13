package com.phew.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.dto.ProfileCard
import com.phew.domain.model.BlockMember
import com.phew.domain.repository.PagerRepository
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.domain.repository.network.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider

class PagerRepositoryImpl @Inject constructor(
    private val pagingNotifyProvider: Provider<PagingNotify>,
    private val pagingNotificationProvider: Provider<PagingNotificationUnRead>,
    private val pagingUnReadNotificationProvider: Provider<PagingNotificationRead>,
    private val blockListPagingSourceProvider: Provider<BlockListPagingSource>,
    private val cardDetailRepository: CardDetailRepository,
    private val profileRepository: ProfileRepository,
) : PagerRepository {
    override fun noticePageStream(): Flow<PagingData<Notice>> =
        Pager(PagingConfig(pageSize = 30)) { pagingNotifyProvider.get() }.flow

    override fun notificationUnRead(): Flow<PagingData<Notification>> = Pager(
        PagingConfig(pageSize = 30)
    ) { pagingNotificationProvider.get() }.flow

    override fun notificationRead(): Flow<PagingData<Notification>> = Pager(
        PagingConfig(pageSize = 30)
    ) { pagingUnReadNotificationProvider.get() }.flow

    override fun cardComments(
        cardId: Long,
        latitude: Double?,
        longitude: Double?,
    ): Flow<PagingData<CardComment>> =
        Pager(
            PagingConfig(pageSize = 30)
        ) { PagingCardComments(cardDetailRepository, cardId, latitude, longitude) }.flow

    override fun profileFeedCard(userId: Long): Flow<PagingData<ProfileCard>> = Pager(
        config = PagingConfig(pageSize = 50),
        pagingSourceFactory = {
            PagingProfileFeedCard(
                repository = profileRepository,
                userId = userId
            )
        }
    ).flow

    override fun profileCommentCard(): Flow<PagingData<ProfileCard>> = Pager(
        config = PagingConfig(pageSize = 50),
        pagingSourceFactory = {
            PagingProfileCommentCard(repository = profileRepository)
        }
    ).flow

    override fun getBlockUserPaging(): Flow<PagingData<BlockMember>> = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { blockListPagingSourceProvider.get() }
    ).flow

    override fun follower(profileId: Long): Flow<PagingData<FollowData>> = Pager(
        config = PagingConfig(pageSize = 50),
        pagingSourceFactory = {
            PagingFollower(repository = profileRepository, profileId = profileId)
        }
    ).flow

    override fun following(profileId: Long): Flow<PagingData<FollowData>> = Pager(
        config = PagingConfig(pageSize = 50),
        pagingSourceFactory = {
            PagingFollowing(repository = profileRepository, profileId = profileId)
        }
    ).flow
}
