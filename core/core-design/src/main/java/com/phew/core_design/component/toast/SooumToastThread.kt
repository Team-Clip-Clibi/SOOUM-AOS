package com.phew.core_design.component.toast

/**
 * 토스트 작업 쓰레드
 */
internal interface SooumToastThread {

    /**
     * 토스트 작업 큐에 토스트 [presenter] 를 넣는다.
     */
    fun enqueue(presenter: SooumToastPresenter, duration: Int, gravity: Int, xOffset: Int, yOffset: Int, delay: Long)

    /**
     * 토스트 작업 큐내의 특정 토스트 [presenter] 를 취소(삭제)한다.
     * + 작업 대기 큐에 존재하는 경우에만 취소 가능하다.
     */
    fun cancel(presenter: SooumToastPresenter)

    /**
     * 토스트 작업 큐를 모두 삭제한다.
     * + 작업 처리 대기중인 토스트 모두 삭제됨
     */
    fun cancelAll()
}