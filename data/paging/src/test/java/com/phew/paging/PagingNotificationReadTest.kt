package com.phew.paging

import androidx.paging.PagingSource
import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.dto.FeedLikeNotification
import com.phew.domain.dto.FollowNotification
import com.phew.domain.dto.Notification
import com.phew.domain.dto.UserBlockNotification
import com.phew.domain.dto.UserDeleteNotification
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.NetworkRepository
import com.phew.domain.token.TokenManger
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PagingNotificationReadTest {
    private val networkRepository: NetworkRepository = mockk()
    private val deviceRepository: DeviceRepository = mockk()
    private val tokenManger: TokenManger = mockk()
    private lateinit var pagingSource: PagingNotificationRead

    @Before
    fun setUp() {
        pagingSource = PagingNotificationRead(
            networkRepository = networkRepository,
            deviceRepository = deviceRepository,
            tokenManger = tokenManger
        )
    }

    @Test
    fun refreshLoad_withValidData_returnsCorrectPage() = runTest {
        val mixedDataList = listOf(
            FeedLikeNotification(
                notificationId = 1,
                createTime = "2025-09-30T10:00:00Z",
                nickName = "user",
                userId = 1,
                targetCardId = 0
            ),
            UserBlockNotification(
                notificationId = 3,
                createTime = "2025-09-28T10:00:00Z",
                blockExpirationDateTime = "2026-09-28T10:00:00Z"
            ),
            UserDeleteNotification(
                notificationId = 4,
                createTime = "2025-09-27T10:00:00Z"
            )
        )
        val fakeToken = "refresh" to "access"
        val successResult = DataResult.Success(200 to mixedDataList)
        coEvery { deviceRepository.requestToken(any()) } returns fakeToken
        coEvery { networkRepository.requestNotificationRead(any()) } returns successResult
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 4, placeholdersEnabled = false)
        )
        assertTrue(result is PagingSource.LoadResult.Page)
        val pageResult = result as PagingSource.LoadResult.Page
        assertEquals(3, pageResult.data.size)
        assertEquals(mixedDataList, pageResult.data)
        assertEquals(4L, pageResult.nextKey)
    }

    @Test
    fun refreshLoad_whenRepositoryReturnEmpty_returnsEmptyPage() = runTest {
        val emptyDomainList = emptyList<Notification>()
        val fakeToken = "refresh" to "access"
        val successResult = DataResult.Success(204 to emptyDomainList)
        coEvery { deviceRepository.requestToken(any()) } returns fakeToken
        coEvery { networkRepository.requestNotificationRead(any()) } returns successResult
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 2,
                placeholdersEnabled = false
            )
        )
        val expected = PagingSource.LoadResult.Page(
            data = emptyDomainList,
            prevKey = null,
            nextKey = null
        )
        assertEquals(expected, result)
    }

    @Test
    fun refreshLoad_whenNetworkResultFails_returnError() = runTest {
        val fakeToken = "refresh" to "access"
        val error = Throwable(ERROR_NETWORK)
        val failResult = DataResult.Fail(code = APP_ERROR_CODE, throwable = error)
        coEvery { deviceRepository.requestToken(any()) } returns fakeToken
        coEvery { networkRepository.requestNotificationRead(any()) } returns failResult
        val result = pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false))
        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals(error.message, (result as PagingSource.LoadResult.Error).throwable.message)
    }

    @Test
    fun appendLoad_whenKeyIsValid_loadsNextPageSuccessfully() = runTest {
        val previousKey = 10L
        val nextPageData = listOf(
            FollowNotification(
                notificationId = 9,
                createTime = "2025-09-29T10:00:00Z",
                nickName = "TestUser",
                userId = 2
            )
        )
        val fakeToken = "refresh" to "access"
        val successResult = DataResult.Success(200 to nextPageData)

        coEvery { deviceRepository.requestToken(any()) } returns fakeToken
        coEvery {
            networkRepository.requestNotificationReadPatch(
                accessToken = any(),
                lastId = previousKey
            )
        } returns successResult
        val result = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = previousKey,
                loadSize = 1,
                placeholdersEnabled = false
            )
        )
        val expected = PagingSource.LoadResult.Page(
            data = nextPageData,
            prevKey = null,
            nextKey = 9L
        )
        assertEquals(expected, result)
    }
}