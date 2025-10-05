package com.phew.core.ui.compose

import androidx.lifecycle.Lifecycle


/**
 *  Composable 타입
 */
enum class ComposableType(val layer: Int) {
    BOTTOM_APP_BAR(0), // 바텀 앱 바
    BOTTOM_SHEET(10), // 바텀 시트
    SCREEN(20) // 스크린 [ 전체 사용 컴포넌튼 ex: PopUpScreen ]
}

/**
 * Lifecycle-Aware Composable 들의 상태 기록 및 조회
 * + 특정 Composable 이 활성화된 상태인지 검사하기 위해 사용함
 * + Composable 이 활성화된 것을 대상으로 관리함.
 */
interface LifecycleAwareComposables {

    /**
     * 등록된 특정 id [uniqueId] Composable 을 가져온다.
     * + Active 상태가 아닌 경우 null 을 반환한다.
     */
    fun getItem(uniqueId: String): LifecycleAwareItem?

    /**
     * 등록된 특정 타입 [type] Composable 을 목록 가져온다.
     */
    fun getItems(type: ComposableType): List<LifecycleAwareItem>

    /**
     * 특정 [item] 이 현재 화면에 표시되는가?
     * + 표시? 해당 Composable 이 가리지는 것 없이 보이는가
     */
    fun isVisible(item: LifecycleAwareItem): Boolean

    /**
     * 특정 Composable 을 Lifecycle-Aware 대상으로 등록
     */
    fun register(uniqueId: String, type: ComposableType, event: Lifecycle.Event)
}

/**
 * 편의 함수: 특정 타입 [type] 의 Composable 이 표시중인가?
 * + ex) 바텀 앱 바 표시중인가?
 * + 바텀 앱 바, 바텀 시트는 화면에서 1개만 나타나므로 type 으로 조회 가능하다.
 */
fun LifecycleAwareComposables.isTypeVisible(type: ComposableType): Boolean {
    return this.getItems(type = type).lastOrNull()?.let { this.isVisible(it) } ?: false
}

/**
 * 편의 함수: 특정 ID [uniqueId] 의 Composable 이 표시중인가?
 */
fun LifecycleAwareComposables.isIdVisible(uniqueId: String): Boolean {
    return this.getItem(uniqueId = uniqueId)?.let { this.isVisible(it) } ?: false
}