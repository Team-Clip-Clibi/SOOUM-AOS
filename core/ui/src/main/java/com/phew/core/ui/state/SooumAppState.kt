package com.phew.core.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.util.extension.isHomeLevelTab
import com.phew.core_common.log.SooumLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 *  App 전체 State를 처리해야 할 경우 사용 목적
 *  1. coroutine
 *  2. navController
 *  3. networkState (TODO 네트워크 상태 추가 필요)
 *  4. 공지사항 또는 알림을 띄워야 할 경우?!?!
 */
@Composable
fun rememberSooumAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController()
): SooumAppState {
    return remember(
        coroutineScope,
        navController
    ) {
        SooumAppState(
            coroutineScope = coroutineScope,
            navController = navController
        )
    }
}

@Stable
class SooumAppState(
    val coroutineScope: CoroutineScope,
    val navController: NavHostController
) {
    // 딥링크 내비게이션 진행 중인지 추적
    var isDeepLinkNavigating by mutableStateOf(false)
        private set
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    //  추후 사용 예정
    val isHomeLevelDestination: Boolean
        @Composable get() {
            val route = currentDestination?.route ?: return false
            return HomeTabType.entries.any { tab ->
                route.startsWith(tab.route) || route.startsWith(tab.graph)
            }
        }

    // BottomBar 표시 여부 결정 (FEED, TAG에서만 표시)
    val shouldShowBottomBar: Boolean
        @Composable get() {
            val currentRoute = currentDestination?.route ?: return false
            
            SooumLog.d("SooumAppState", "=== shouldShowBottomBar 체크 시작 ===")
            SooumLog.d("SooumAppState", "현재 라우트: $currentRoute")
            SooumLog.d("SooumAppState", "딥링크 진행중: $isDeepLinkNavigating")
            SooumLog.d("SooumAppState", "NavController current: ${navController.currentDestination?.route}")
            
            val isHomeRoute = currentRoute.isHomeRoute()

            // 홈으로 돌아왔으면 딥링크 상태 초기화
            if (isDeepLinkNavigating && isHomeRoute) {
                SooumLog.d("SooumAppState", "딥링크 상태 초기화 - 현재 홈 라우트: $currentRoute")
                isDeepLinkNavigating = false
            }

            // 딥링크 내비게이션 진행 중에는 BottomBar 숨김
            if (isDeepLinkNavigating) {
                SooumLog.d("SooumAppState", "딥링크 진행중으로 BottomBar 숨김")
                return false
            }
            
            // 딥링크가 아니라도 Detail과 관련된 라우트라면 즉시 숨김
            if (currentRoute.contains("detail", ignoreCase = true)) {
                SooumLog.d("SooumAppState", "Detail 관련 라우트로 BottomBar 숨김: $currentRoute")
                return false
            }
            
            // 기타 특정 그래프 경로 체크
            if (currentRoute.startsWith("detail_graph") || currentRoute.contains("detail_route")) {
                SooumLog.d("SooumAppState", "Detail 화면으로 BottomBar 숨김 (구체적 라우트 매칭)")
                return false
            }
            if (currentRoute.startsWith(DETAIL_COMMENT_ROUTE)) {
                SooumLog.d("SooumAppState", "Detail Comment 화면으로 BottomBar 숨김")
                return false
            }
            if (currentRoute.startsWith(SETTING_GRAPH)) {
                SooumLog.d("SooumAppState", "Setting 화면으로 BottomBar 숨김") 
                return false
            }
            if (currentRoute.startsWith(HomeTabType.WRITE.graph)) {
                SooumLog.d("SooumAppState", "Write 화면으로 BottomBar 숨김")
                return false
            }

            // FEED, TAG, MY에서만 BottomBar 표시
            val shouldShow = listOf(HomeTabType.FEED, HomeTabType.TAG, HomeTabType.MY)
                .any { tab -> currentDestination.isHomeLevelTab(tab) }
            
            SooumLog.d("SooumAppState", "BottomBar 표시 여부: $shouldShow")
            
            // 더 상세한 디버깅 로그
            val feedCheck = currentDestination.isHomeLevelTab(HomeTabType.FEED)
            val tagCheck = currentDestination.isHomeLevelTab(HomeTabType.TAG) 
            val myCheck = currentDestination.isHomeLevelTab(HomeTabType.MY)
            
            SooumLog.d("SooumAppState", "FEED 체크: $feedCheck, TAG 체크: $tagCheck, MY 체크: $myCheck")
            SooumLog.d("SooumAppState", "현재 destination: ${currentDestination?.route}")
            SooumLog.d("SooumAppState", "route detail check: ${currentRoute.startsWith("detail_graph")} || ${currentRoute.contains("detail_route")} || ${currentRoute.contains("detail")}")
            SooumLog.d("SooumAppState", "=== shouldShowBottomBar 체크 종료: $shouldShow ===")
            
            return shouldShow
        }

    // Feed scroll to top event
    private val _feedScrollToTopEvent = MutableSharedFlow<Unit>()
    val feedScrollToTopEvent = _feedScrollToTopEvent.asSharedFlow()
    
    suspend fun scrollFeedToTop() {
        _feedScrollToTopEvent.emit(Unit)
    }
    
    // 딥링크 내비게이션 상태 제어
    fun updateDeepLinkNavigating(navigating: Boolean) {
        isDeepLinkNavigating = navigating
    }

    companion object {
        private const val TAG = "SooumAppState"
        private const val SETTING_GRAPH = "setting_graph"
        private const val DETAIL_GRAPH = "detail_graph"
        private const val DETAIL_COMMENT_ROUTE = "comment_route"
    }
}

private fun String.isHomeRoute(): Boolean {
    return HomeTabType.entries.any { tab ->
        this.startsWith(tab.route) || this.startsWith(tab.graph)
    }
}
