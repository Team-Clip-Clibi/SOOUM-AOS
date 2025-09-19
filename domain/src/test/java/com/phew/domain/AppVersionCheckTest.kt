package com.phew.domain

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.domain.repository.NetworkRepository
import com.phew.domain.usecase.CheckAppVersion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class AppVersionCheckTest {
    private lateinit var networkRepository: NetworkRepository
    private lateinit var checkAppVersion: CheckAppVersion

    @Before
    fun setUp() {
        networkRepository = mockk()
        checkAppVersion = CheckAppVersion(networkRepository)
    }

    @Test
    fun invoke_whenDebugMode_returnsSuccessWithoutNetworkCall() = runTest {
        val param = CheckAppVersion.Param(appVersion = "1.0.0", isDebugMode = true)
        val result = checkAppVersion(param)
        coVerify(exactly = 0) { networkRepository.requestAppVersion(any(), any()) }
        assertEquals(result, DomainResult.Success(true))
    }

    @Test
    fun invoke_whenUpdateIsRequired_returnsSuccessFalse() = runTest {
        val version = "1.0.0"
        val param = CheckAppVersion.Param(appVersion = version, isDebugMode = false)
        coEvery {
            networkRepository.requestAppVersion(type = BuildConfig.APP_TYPE, appVersion = version)
        } returns DataResult.Success(APP_UPDATE)
        val result = checkAppVersion(param)
        assertEquals(result, DomainResult.Success(false))
    }

    @Test
    fun invoke_whenUpdateIsNotRequired_returnSuccessTrue() = runTest {
        val version = "1.0.0"
        val param = CheckAppVersion.Param(appVersion = version, isDebugMode = false)
        coEvery {
            networkRepository.requestAppVersion(any(), any())
        } returns DataResult.Success("OK")
        val result = checkAppVersion(param)
        assertEquals(result, DomainResult.Success(true))
    }

    @Test
    fun invoke_whenInterNetNotWork_returnFailure() = runTest {
        val version = "1.0.0"
        val param = CheckAppVersion.Param(appVersion = version, isDebugMode = false)
        coEvery {
            networkRepository.requestAppVersion(any(), any())
        } returns DataResult.Fail()
        val result = checkAppVersion(param)
        assertEquals(result , DomainResult.Failure(Unit))
    }
}