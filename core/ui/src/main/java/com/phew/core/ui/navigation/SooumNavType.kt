package com.phew.core.ui.navigation

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.navigation.NavType
import com.phew.core.ui.util.JsonSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.Serializable


/**
 *  네비게이션 아규먼트 키
 */
const val NavArgKey = "sooumArgName"

/**
 *  네비게이션 커리 파라미터가 명시된 목적 링크 생성 (주소 명세)
 */
fun String.asNavParam() = this.plus(other = "?$NavArgKey={$NavArgKey}")

/**
 *  네비게이션 쿼리 아규먼트가 주입된 목적 링크 생성 (실제 Query Parameter 추가된 주소 명세)
 */
inline fun <reified T> String.asNavArg(arg: T): String {
    val argName = JsonSerializer.createNav()
        .encodeToString(arg)
        .let { Uri.encode(it)}

    return this.replace(oldValue = "{$NavArgKey}", newValue = argName, ignoreCase = false)
}

/**
 *  네비게이션 쿼리 아큐먼트 추출하기
 */
inline fun <reified T: Serializable> Bundle.getNavArg(): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializable(NavArgKey, T::class.java) as? T
    } else {
        this.getSerializable(NavArgKey) as? T
    }
}

/**
 * 커스텀 [NavType] 생성
 */
inline fun <reified T: Serializable> createNavType(
    isNullableAllowed: Boolean = false
): NavType<T> {
    return object : NavType<T>(isNullableAllowed) {
        override fun put(bundle: Bundle, key: String, value: T) {
            bundle.putSerializable(key, value)
        }

        override fun get(bundle: Bundle, key: String): T? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(key, T::class.java)
            } else {
                bundle.getSerializable(key) as? T
            }
        }

        override fun parseValue(value: String): T {
            return JsonSerializer.createNav().decodeFromString(value)
        }

    }
}