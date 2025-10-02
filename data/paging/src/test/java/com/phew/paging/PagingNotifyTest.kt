package com.phew.paging

import android.graphics.pdf.models.selection.PageSelection
import androidx.paging.PagingSource
import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.dto.Notice
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

class PagingNotifyTest {
    private val networkRepository: NetworkRepository = mockk()
    private val deviceRepository: DeviceRepository = mockk()
    private val tokenManger: TokenManger = mockk()
    private lateinit var pagingSource: PagingNotify

    @Before
    fun setUp() {
        pagingSource = PagingNotify(
            networkRepository = networkRepository,
            deviceRepository = deviceRepository,
            tokenManger = tokenManger
        )
    }

    @Test
    fun refreshLoad_withValidData_returnCorrectPage() = runTest {
        val data = listOf(
            Notice(
                title = "test1",
                url = "test2",
                createdAt = "2025-09-30",
                id = 0
            ),
            Notice(
                title = "test2",
                url = "test3",
                createdAt = "2025-09-30",
                id = 1
            ),
            Notice(
                title = "test3",
                url = "test4",
                createdAt = "2025-09-30",
                id = 2
            )
        )
        val fakeToken = "refresh" to "access"
        val successResult = DataResult.Success(200 to data)
        coEvery { deviceRepository.requestToken(any()) } returns fakeToken
        coEvery { networkRepository.requestNotice(any()) } returns successResult
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 4, placeholdersEnabled = false)
        )
        assertTrue(result is PagingSource.LoadResult.Page)
        val pageResult = result as PagingSource.LoadResult.Page
        assertEquals(3, pageResult.data.size)
        assertEquals(data, pageResult.data)
        assertEquals(2, pageResult.nextKey)
    }

    @Test
    fun refreshLoad_whenRepositoryReturnEmpty_returnsEmptyPage() = runTest {
        val emptyData = emptyList<Notice>()
        val fakeToken = "refresh" to "access"
        val successResult = DataResult.Success(204 to emptyData)
        coEvery { deviceRepository.requestToken(any()) } returns fakeToken
        coEvery { networkRepository.requestNotice(any()) } returns successResult
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 2,
                placeholdersEnabled = false
            )
        )
        val expect = PagingSource.LoadResult.Page(
            data = emptyData,
            prevKey = null,
            nextKey = null
        )
        assertEquals(expect, result)
    }

    @Test
    fun refreshLoad_whenNetworkResultFails_returnError() = runTest {
        val fakeToken = "refresh" to "access"
        val error = Throwable(ERROR_NETWORK)
        val failResult = DataResult.Fail(code = APP_ERROR_CODE, throwable = error)
        coEvery { deviceRepository.requestToken(any()) } returns fakeToken
        coEvery { networkRepository.requestNotice(any()) } returns failResult
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 2,
                placeholdersEnabled = false
            )
        )
        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals(error.message, (result as PagingSource.LoadResult.Error).throwable.message)
    }

    @Test
    fun appendLoad_whenKeyIsValid_nextPagesSuccessfully() = runTest {
        val previousKey = 10
        val nexPage = listOf(
            Notice(
                title = "test3",
                url = "test4",
                createdAt = "2025-09-30",
                id = 11
            )
        )
        val fakeToken = "refresh" to "access"
        val successResult = DataResult.Success(200 to nexPage)
        coEvery { deviceRepository.requestToken(any()) } returns fakeToken
        coEvery {
            networkRepository.requestNoticePatch(
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
            data = nexPage,
            prevKey = null,
            nextKey = 11
        )
        assertEquals(expected, result)
    }
}