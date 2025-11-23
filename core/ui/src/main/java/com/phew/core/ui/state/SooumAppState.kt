package com.phew.core.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phew.core.ui.component.home.HomeTabType
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
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    //  추후 사용 예정
    val isHomeLevelDestination: Boolean
        @Composable get() = HomeTabType.entries
            .map { it.route }
            .contains(currentDestination?.route ?: "")

    // Feed scroll to top event
    private val _feedScrollToTopEvent = MutableSharedFlow<Unit>()
    val feedScrollToTopEvent = _feedScrollToTopEvent.asSharedFlow()
    
    suspend fun scrollFeedToTop() {
        _feedScrollToTopEvent.emit(Unit)
    }

    companion object {
        private const val TAG = "SooumAppState"
    }
}