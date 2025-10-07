package com.phew.core.ui.compose

import androidx.lifecycle.Lifecycle
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.compareTo


@Singleton
class LifecycleAwareComposablesImpl @Inject constructor() : LifecycleAwareComposables {
    private val items = mutableListOf<LifecycleAwareItem>()

    override fun getItem(uniqueId: String): LifecycleAwareItem? {
        return synchronized(items) {
            items.find { item -> item.uniqueId == uniqueId }
        }
    }

    override fun getItems(type: ComposableType): List<LifecycleAwareItem> {
        return synchronized(items) {
            items.filter { item -> item.type == type }
        }
    }

    override fun isVisible(item: LifecycleAwareItem): Boolean {
        // 사전 검사 : 찾는 대상이 이미 destroy 상태 인가?
        if (!isVisibleItem(item)) return false

        // 관리 목록의 최후미에 위치하면 표시되는 상태
        // 관리 목록의 다음 위치에 layer 값이 큰 item 이 없으면 표시되는 상태
        var result: Boolean? = null

        synchronized(items) {

            // 보이지 않는 것들 정리
            items.removeIf { lifecycleAwareItem -> !isVisibleItem(lifecycleAwareItem) }

            val targetIndex = items.indexOfLast { it.uniqueId == item.uniqueId }
            if (targetIndex >= 0) {
                // 큰 layer 값의 아이템 없음
                items.forEachIndexed { index, lifecycleAwareItem ->
                    if (index >= targetIndex) {
                        //#1 target 이후의 아이템들의 layer 값 비교 검사
                        // special case: 검사 대상이 bottom-app-bar 이며, 비교할 대상이 SCREEN 이면 검사 생략
                        // why? bottom-app-bar 가 SCREEN 생선되기 전  표시 여부 스스로 결정 한다.
                        //      bottom-app-bar 의 구현 방식으로 인하여 특수 처리 필요하게 됨
                        if (!(item.type == ComposableType.BOTTOM_APP_BAR && lifecycleAwareItem.type == ComposableType.SCREEN)) {
                            if (lifecycleAwareItem.type.layer > item.type.layer) {
                                result = false
                                return@synchronized
                            }
                        }
                        //#2 최후미 까지 검사하였으나, 높은 layer 값이 없다.
                        if (items.lastIndex == index) {
                            result = true
                        }
                    }
                }
            }
        }

        return result ?: false
    }

    override fun register(
        uniqueId: String,
        type: ComposableType,
        event: Lifecycle.Event
    ) {
        // 순차 등록. (기존 삭제 후 추가)
        // 가장 최신 것이 list 의 후단에 위치함
        synchronized(items) {
            items.removeIf { it.uniqueId == uniqueId }
            if (!invisibleEvents.contains(event)) {
                items.add(LifecycleAwareItem(uniqueId = uniqueId, type = type, event = event))
            }
        }
    }

    private fun isVisibleItem(target: LifecycleAwareItem): Boolean {
        return !invisibleEvents.contains(target.event)
    }

    companion object {
        private const val TAG = "LifecycleAwareComposablesImpl"
        private val invisibleEvents = listOf(Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY, Lifecycle.Event.ON_ANY)
    }
}