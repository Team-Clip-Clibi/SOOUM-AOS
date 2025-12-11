package com.phew.core_design.component.toast

/**
 * 토스트 표시기
 */
interface SooumToastPresenter {
    /**
     * 토스트의 고유 ID 값을 가져온다.
     */
    fun getId(): Int

    /**
     * 토스트를 화면에 표시한다.
     */
    fun show(contextProvider: SooumToastContextProvider, gravity: Int, xOffset: Int, yOffset: Int)

    /**
     * 토스트를 화면에서 숨긴다.
     */
    fun hide(enableAnimation: Boolean = true)
}
