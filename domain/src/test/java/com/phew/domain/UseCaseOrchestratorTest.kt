package com.phew.domain

import android.content.ContentResolver
import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NO_DATA
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.HTTP_SUCCESS
import com.phew.core_common.errorMessage
import com.phew.core_common.resultFailure
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.dto.TagInfo as FeedTagInfo
import com.phew.domain.interceptor.InterceptorManger
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.model.TagInfo
import com.phew.domain.model.TagInfoList
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.FeedPagingFactory
import com.phew.domain.repository.PagerRepository
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.AppVersionRepository
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.domain.repository.network.MembersRepository
import com.phew.domain.repository.network.NotifyRepository
import com.phew.domain.repository.network.ProfileRepository
import com.phew.domain.repository.network.ReportsRepository
import com.phew.domain.repository.network.SignUpRepository
import com.phew.domain.repository.network.SplashRepository
import com.phew.domain.repository.network.TagRepository
import com.phew.domain.orchestrator.CardDetailUseCaseOrchestrator
import com.phew.domain.orchestrator.FeedUseCaseOrchestrator
import com.phew.domain.orchestrator.ProfileUseCaseOrchestrator
import com.phew.domain.orchestrator.ReportUseCaseOrchestrator
import com.phew.domain.orchestrator.SettingsUseCaseOrchestrator
import com.phew.domain.orchestrator.SignUpUseCaseOrchestrator
import com.phew.domain.orchestrator.SplashUseCaseOrchestrator
import com.phew.domain.orchestrator.TagUseCaseOrchestrator
import com.phew.domain.orchestrator.WriteUseCaseOrchestrator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UseCaseOrchestratorTest {
    private val context = mockk<Context>(relaxed = true)
    private val contentResolver = mockk<ContentResolver>(relaxed = true)
    private val pagerRepository = mockk<PagerRepository>(relaxed = true)
    private val feedPagingFactory = mockk<FeedPagingFactory>(relaxed = true)
    private val deviceRepository = mockk<DeviceRepository>(relaxed = true)
    private val notifyRepository = mockk<NotifyRepository>(relaxed = true)
    private val cardFeedRepository = mockk<CardFeedRepository>(relaxed = true)
    private val cardDetailRepository = mockk<CardDetailRepository>(relaxed = true)
    private val eventRepository = mockk<EventRepository>(relaxed = true)
    private val profileRepository = mockk<ProfileRepository>(relaxed = true)
    private val signUpRepository = mockk<SignUpRepository>(relaxed = true)
    private val tagRepository = mockk<TagRepository>(relaxed = true)
    private val membersRepository = mockk<MembersRepository>(relaxed = true)
    private val appVersionRepository = mockk<AppVersionRepository>(relaxed = true)
    private val reportsRepository = mockk<ReportsRepository>(relaxed = true)
    private val splashRepository = mockk<SplashRepository>(relaxed = true)
    private val interceptorManger = mockk<InterceptorManger>(relaxed = true)

    @Test
    fun `feed notification is sorted descending and limited to latest three`() = runTest {
        val orchestrator = feedOrchestrator()
        coEvery {
            notifyRepository.requestNotice(pageSize = 3, source = NoticeSource.NOTIFICATION)
        } returns Result.success(
            4 to listOf(
                notice(id = 1),
                notice(id = 4),
                notice(id = 2),
                notice(id = 3),
            ),
        )

        val result = orchestrator.getFeedNotification(NoticeSource.NOTIFICATION)

        assertThat(result.getOrThrow().map { it.id }).containsExactly(4, 3, 2).inOrder()
    }

    @Test
    fun `feed mark notifications read succeeds when any notification is read`() = runTest {
        val orchestrator = feedOrchestrator()
        coEvery { notifyRepository.requestReadNotify(1L) } returns 500
        coEvery { notifyRepository.requestReadNotify(2L) } returns HTTP_SUCCESS

        val result = orchestrator.markNotificationsRead(listOf(1L, 2L))

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { notifyRepository.requestReadNotify(1L) }
        coVerify(exactly = 1) { notifyRepository.requestReadNotify(2L) }
    }

    @Test
    fun `profile check is my profile compares id and nickname from repository`() = runTest {
        val orchestrator = profileOrchestrator()
        coEvery { profileRepository.requestMyProfile() } returns Result.success(
            profile(userId = 10L, nickname = "sonny"),
        )

        val mine = orchestrator.checkIsMyProfile(userId = 10L, nickName = "sonny")
        val other = orchestrator.checkIsMyProfile(userId = 11L, nickName = "sonny")

        assertThat(mine.getOrThrow()).isEqualTo(true to 10L)
        assertThat(other.getOrThrow()).isEqualTo(false to 11L)
    }

    @Test
    fun `profile follow maps invalid token to logout message`() = runTest {
        val orchestrator = profileOrchestrator()
        coEvery { profileRepository.requestFollowUser(profileId = 7L) } returns resultFailure(
            code = HTTP_INVALID_TOKEN,
        )

        val result = orchestrator.followUser(7L)

        assertThat(result.errorMessage()).isEqualTo(ERROR_LOGOUT)
    }

    @Test
    fun `tag related tags returns no content failure when repository list is empty`() = runTest {
        val orchestrator = tagOrchestrator()
        coEvery { tagRepository.getRelatedTags(20L, "empty") } returns Result.success(TagInfoList(emptyList()))

        val result = orchestrator.relatedTags("empty")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `tag favorite add logs event and maps network failure`() = runTest {
        val orchestrator = tagOrchestrator()
        coEvery { tagRepository.addFavoriteTag(3L) } returns resultFailure(code = 500)

        val result = orchestrator.addFavoriteTag(3L)

        assertThat(result.errorMessage()).isEqualTo(ERROR_NETWORK)
        coVerify(exactly = 1) { eventRepository.logTagRegisterTag() }
    }

    @Test
    fun `tag rank filters zero usage and sorts descending`() = runTest {
        val orchestrator = tagOrchestrator()
        coEvery { tagRepository.getTagRank() } returns Result.success(
            TagInfoList(
                listOf(
                    TagInfo(id = 1L, name = "zero", usageCnt = 0),
                    TagInfo(id = 2L, name = "middle", usageCnt = 3),
                    TagInfo(id = 3L, name = "top", usageCnt = 9),
                ),
            ),
        )

        val result = orchestrator.tagRank()

        assertThat(result.getOrThrow().map { it.name }).containsExactly("top", "middle").inOrder()
    }

    @Test
    fun `splash debug version check returns ok without repository request`() = runTest {
        val result = splashOrchestrator().checkAppVersion(appVersion = "1.0.0", isDebugMode = true)

        assertThat(result.getOrThrow()).isEqualTo(AppVersionStatusType.OK)
    }

    @Test
    fun `report empty card id maps to fail job`() = runTest {
        val result = reportOrchestrator().reportCard(cardId = "", reason = com.phew.domain.dto.ReportReason.NONE)

        assertThat(result.errorMessage()).isEqualTo(ERROR_FAIL_JOB)
    }

    @Test
    fun `settings refresh token returns blank when token is absent`() = runTest {
        coEvery { deviceRepository.requestToken(BuildConfig.TOKEN_KEY) } returns (ERROR_NO_DATA to ERROR_NO_DATA)

        val result = settingsOrchestrator().refreshToken()

        assertThat(result).isEmpty()
    }

    @Test
    fun `detail unlike delegates to detail repository`() = runTest {
        coEvery { cardDetailRepository.unlikeCard(22L) } returns Result.success(Unit)

        val result = detailOrchestrator().setCardLike(cardId = 22L, shouldLike = false)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { cardDetailRepository.unlikeCard(22L) }
    }

    @Test
    fun `sign up generated nickname delegates to sign up repository`() = runTest {
        coEvery { signUpRepository.requestNickName() } returns Result.success("sooum")

        val result = signUpOrchestrator().generatedNickName()

        assertThat(result.getOrThrow()).isEqualTo("sooum")
    }

    @Test
    fun `write related tags maps repository success`() = runTest {
        coEvery { cardFeedRepository.requestRelatedTag(resultCnt = 8, tag = "hello") } returns Result.success(
            listOf(FeedTagInfo(id = 1L, name = "hello", usageCnt = 10)),
        )

        val result = writeOrchestrator().relatedTags(tag = "hello", resultCount = 8)

        assertThat(result.getOrThrow().map { it.name }).containsExactly("hello")
    }

    private fun feedOrchestrator() = FeedUseCaseOrchestrator(
        context = context,
        pagerRepository = pagerRepository,
        feedPagingFactory = feedPagingFactory,
        deviceRepository = deviceRepository,
        notifyRepository = notifyRepository,
        cardFeedRepository = cardFeedRepository,
        cardDetailRepository = cardDetailRepository,
        eventRepository = eventRepository,
    )

    private fun profileOrchestrator() = ProfileUseCaseOrchestrator(
        context = context,
        contentResolver = contentResolver,
        profileRepository = profileRepository,
        pagerRepository = pagerRepository,
        signUpRepository = signUpRepository,
        cardFeedRepository = cardFeedRepository,
    )

    private fun tagOrchestrator() = TagUseCaseOrchestrator(
        pagerRepository = pagerRepository,
        tagRepository = tagRepository,
        deviceRepository = deviceRepository,
        eventRepository = eventRepository,
        cardFeedRepository = cardFeedRepository,
    )

    private fun splashOrchestrator() = SplashUseCaseOrchestrator(
        deviceRepository = deviceRepository,
        profileRepository = profileRepository,
        splashRepository = splashRepository,
    )

    private fun reportOrchestrator() = ReportUseCaseOrchestrator(
        reportsRepository = reportsRepository,
    )

    private fun settingsOrchestrator() = SettingsUseCaseOrchestrator(
        membersRepository = membersRepository,
        deviceRepository = deviceRepository,
        appVersionRepository = appVersionRepository,
        pagerRepository = pagerRepository,
        notifyRepository = notifyRepository,
        cardDetailRepository = cardDetailRepository,
        signUpRepository = signUpRepository,
        interceptorManger = interceptorManger,
        eventRepository = eventRepository,
    )

    private fun detailOrchestrator() = CardDetailUseCaseOrchestrator(
        cardDetailRepository = cardDetailRepository,
        cardFeedRepository = cardFeedRepository,
        pagerRepository = pagerRepository,
        deviceRepository = deviceRepository,
        eventRepository = eventRepository,
    )

    private fun signUpOrchestrator() = SignUpUseCaseOrchestrator(
        context = context,
        signUpRepository = signUpRepository,
        deviceRepository = deviceRepository,
        membersRepository = membersRepository,
        interceptorManger = interceptorManger,
        profileRepository = profileRepository,
        eventRepository = eventRepository,
    )

    private fun writeOrchestrator() = WriteUseCaseOrchestrator(
        context = context,
        cardFeedRepository = cardFeedRepository,
        cardDetailRepository = cardDetailRepository,
        deviceRepository = deviceRepository,
        eventRepository = eventRepository,
        membersRepository = membersRepository,
    )

    private fun notice(id: Int) = Notice(
        content = "content-$id",
        url = "",
        createdAt = "2026-06-28T00:00:00",
        noticeType = "announcement",
        id = id,
    )

    private fun profile(userId: Long, nickname: String) = ProfileInfo(
        userId = userId,
        nickname = nickname,
        profileImgName = "",
        profileImageUrl = "",
        totalVisitCnt = 0,
        todayVisitCnt = 0,
        cardCnt = 0,
        followingCnt = 0,
        followerCnt = 0,
        bio = "",
        isBlocked = false,
        isAlreadyFollowing = false,
    )
}
