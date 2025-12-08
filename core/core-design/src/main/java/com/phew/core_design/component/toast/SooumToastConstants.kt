package com.phew.core_design.component.toast

internal object SooumToastConstants {
    /**
     * 토스트 로그 출력 여부
     */
    const val DEBUG_ENABLED = false

    /**
     * 토스트가 즉시 표시되도록 할 것인가 여부
     * + 기본값 true
     * + 안드로이드 시스템 Toast 는 표시 시간 보장됨
     * + Sooum 시스템 Toast 는 표시중인  토스트를 취소하고 요청된 토스트를 즉시 표시함
     */
    const val INSTANT_MODE_ENABLED = true
}