package com.phew.core.ui.component.back

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.navOptions
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.component.home.HomeTabType.Companion.isFeedHome
import com.phew.core.ui.state.SooumAppState
import com.phew.core.ui.util.extension.findActivity
import com.phew.core_common.log.SooumLog
import kotlinx.coroutines.delay

@Composable
fun SooumBackHandler(
    appState: SooumAppState
) {
    val isHomeRoute = appState.isHomeLevelDestination
    val currentRoute = appState.currentDestination?.route

    SooumLog.d(TAG, "isFeedHome? = $currentRoute")

    if (isHomeRoute) {
        if (isFeedHome(currentRoute)) {
            SooumExitBackHandler()
        } else {
            BackHandler {
                val route = requireNotNull(currentRoute)
                appState.navController.navigate(
                    route = HomeTabType.FEED.route,
                    navOptions = navOptions { popUpTo(route) { inclusive = true } }
                )
            }
        }
    } else {
        //  Home Route가 아닐때
        BackHandler(true) {
            SooumOnBackPressed(appState = appState)
        }
    }
}

/**
 *  이전 Route가 존재하면 navController.popBackStack()
 *  존재하지 않으면 Route가 속한 홈 화면 이동
 */
fun SooumOnBackPressed(
    appState: SooumAppState
) {
    val previousRoute = appState.navController.previousBackStackEntry?.destination

    val currentDestination = appState.navController.currentBackStackEntry?.destination

    if (previousRoute != null) { // 이전 Route 스택이 존재 할 경우.
        appState.navController.popBackStack()
    } else { // 이전 Route Stack 이 존재하지 않을 경우
        val route = currentDestination?.route
        val home = HomeTabType.findHome(route)

        val navOptions = if (route != null) {
            navOptions { popUpTo(route) { inclusive = true } }
        } else {
            null
        }

        appState.navController.navigate(
            route = home.route,
            navOptions = navOptions
        )
    }
}

/**
 * Sooum 종료 BackHandler
 * BackPress Toast 처리
 */
@Composable
fun SooumExitBackHandler(
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    var backPressState by remember { mutableStateOf<BackPressState>(BackPressState.Idle) }
    SooumLog.d(TAG, "init backPressState: $backPressState")
    LaunchedEffect(key1 = backPressState) {
        if (backPressState == BackPressState.InitialTouch) {
            delay(2000L)
            backPressState = BackPressState.Idle
        }
    }
    BackHandler {
        SooumLog.d(TAG, "BackHandler triggered - Current backPressState: $backPressState")
        if (backPressState is BackPressState.Idle) {
            SooumLog.d(TAG, "State is Idle - changing to InitialTouch")
            backPressState = BackPressState.InitialTouch

            //  TODO Snackbar로 처리
            Toast.makeText(
                context,
                "뒤로가기 버튼을 한번 더 누르면 종료됩니다.",
                Toast.LENGTH_SHORT
            ).show()
        } else if (backPressState is BackPressState.InitialTouch) {
            SooumLog.d(TAG, "State is InitialTouch - exiting app")
            onDismiss()
            context.findActivity().finish()
        } else {
            SooumLog.d(TAG, "Unexpected state: $backPressState")
        }
    }
}


@Keep
sealed class BackPressState {
    @Keep
    data object Idle : BackPressState()
    @Keep
    data object InitialTouch : BackPressState()
    @Keep
    data object Exit : BackPressState()
}

private const val TAG = "SooumExitBackHandler"
