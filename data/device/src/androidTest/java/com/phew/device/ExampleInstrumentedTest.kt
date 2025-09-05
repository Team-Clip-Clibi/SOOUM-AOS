package com.phew.device

import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun deviceId_returnsAndroidId() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val expected = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val device = DeviceImpl(ctx)
        val actual = device.deviceId()
        assertNotNull(actual)
        assertTrue(actual.isNotBlank())
        assertEquals(expected, actual)
    }
    @Test
    fun deviceId_throws_whenResolverFails() {
        val base = ApplicationProvider.getApplicationContext<Context>()
        val throwingCtx = object : ContextWrapper(base) {
            override fun getContentResolver(): ContentResolver {
                throw RuntimeException("boom")
            }
        }

        val device = DeviceImpl(throwingCtx)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { device.deviceId() }
        }
        assertTrue(ex.message?.contains("Device ID not fount") == true)
    }
}