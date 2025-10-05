package com.phew.token

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.domain.dto.Token
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.NetworkRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class TokenMangerTest {
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var networkRepository: NetworkRepository
    private lateinit var tokenManager: TokenMangerImpl
    private val oldToken = Token("old_access_token_value", "old_refresh_token_value")
    private val newToken = Token("new_access_token_value", "new_refresh_token_value")

    @Before
    fun setup() {
        deviceRepository = mockk()
        networkRepository = mockk()
        tokenManager = TokenMangerImpl(deviceRepository, networkRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun requestUpdateToken_return_true_on_successful_refresh_and_save() = runTest {
        coEvery { networkRepository.requestRefreshToken(oldToken) } returns DataResult.Success(
            newToken
        )
        coEvery { deviceRepository.saveToken(BuildConfig.TOKEN_KEY, newToken) } returns true
        val result = tokenManager.requestUpdateToken(oldToken)
        assertTrue(result)
        coVerify(exactly = 1) { networkRepository.requestRefreshToken(oldToken) }
        coVerify(exactly = 1) { deviceRepository.saveToken(BuildConfig.TOKEN_KEY, newToken) }
    }

    @Test
    fun requestUpdateToken_return_true_on_network_failed_refresh() = runTest {
        val networkError = Exception("Network failed")
        coEvery { networkRepository.requestRefreshToken(oldToken) } returns DataResult.Fail(
            code = APP_ERROR_CODE,
            message = null,
            networkError
        )
        val result = tokenManager.requestUpdateToken(oldToken)
        assertFalse(result)
        coVerify(exactly = 1) { networkRepository.requestRefreshToken(oldToken) }
        coVerify(exactly = 0) { deviceRepository.saveToken(BuildConfig.TOKEN_KEY, newToken) }
    }

    @Test
    fun requestUpdateToken_should_return_false_on_save_failed() = runTest {
        coEvery { networkRepository.requestRefreshToken(oldToken) } returns DataResult.Success(
            newToken
        )
        coEvery { deviceRepository.saveToken(BuildConfig.TOKEN_KEY, newToken) } returns false
        val result = tokenManager.requestUpdateToken(oldToken)
        assertFalse(result)
        coVerify(exactly = 1) { networkRepository.requestRefreshToken(oldToken) }
        coVerify(exactly = 1) { deviceRepository.saveToken(BuildConfig.TOKEN_KEY, newToken) }
    }

    @Test
    fun requestUpdateToken_current_calls_should_only_perform_refresh_once_success() =
        runTest(timeout = 1000.milliseconds) {
            coEvery { networkRepository.requestRefreshToken(oldToken) } coAnswers {
                kotlinx.coroutines.delay(50)
                DataResult.Success(newToken)
            }
            coEvery { deviceRepository.saveToken(BuildConfig.TOKEN_KEY, newToken) } coAnswers {
                kotlinx.coroutines.delay(50)
                true
            }
            val firstCall = async { tokenManager.requestUpdateToken(oldToken) }
            val secondCall = async { tokenManager.requestUpdateToken(oldToken) }
            val firstResult = firstCall.await()
            val secondResult = secondCall.await()
            assertTrue(firstResult)
            assertTrue(secondResult)
            coVerify(exactly = 1) { networkRepository.requestRefreshToken(oldToken) }
            coVerify(exactly = 1) { deviceRepository.saveToken(BuildConfig.TOKEN_KEY, newToken) }
        }

    @Test
    fun requestUpdateToken_current_calls_should_only_perform_refresh_once_fail() =
        runTest(timeout = 1000.milliseconds) {
            val networkException = Exception("Network Error")
            coEvery { networkRepository.requestRefreshToken(oldToken) } coAnswers {
                kotlinx.coroutines.delay(50)
                DataResult.Fail(code = APP_ERROR_CODE, message = null, networkException)
            }
            val firstCall = async { tokenManager.requestUpdateToken(oldToken) }
            val secondCall = async { tokenManager.requestUpdateToken(oldToken) }
            val firstResult = firstCall.await()
            val secondResult = secondCall.await()
            assertFalse(firstResult)
            assertFalse(secondResult)
            coVerify(exactly = 1) { networkRepository.requestRefreshToken(oldToken) }
            coVerify(exactly = 0) { deviceRepository.saveToken(any(), any()) }
        }
}