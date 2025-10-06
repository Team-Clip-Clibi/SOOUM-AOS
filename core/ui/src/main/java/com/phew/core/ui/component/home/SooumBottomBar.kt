package com.phew.core.ui.component.home

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.phew.core.ui.compose.ComposableType
import com.phew.core.ui.compose.ComposableVisibleState
import com.phew.core.ui.compose.LifecycleAwareComposableRegister
import com.phew.core.ui.util.extension.isHomeLevelTab
import com.phew.core.ui.util.extension.shouldShowBottomBar
import com.phew.core_design.TextComponent
import com.phew.core_design.component.SooumNavigationBar
import com.phew.core_design.component.SooumNavigationBarItem

@Composable
fun SooumBottomBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    homeTaps: List<HomeTabType> = HomeTabType.entries
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val visibleState = remember { ComposableVisibleState() }

    val shouldShowBottomBar by remember(navBackStackEntry) {
        derivedStateOf {
            navBackStackEntry?.destination?.shouldShowBottomBar(homeTaps) ?: false
        }
    }

    LaunchedEffect(shouldShowBottomBar) {
        // LifecycleAware visible 상태 설정
        visibleState.setEnabled(shouldShowBottomBar)
    }

    // lifecycle-aware composable 로 등록
    LifecycleAwareComposableRegister(uniqueId = TAG, type = ComposableType.BOTTOM_APP_BAR, visibleState = visibleState)

    SooumNavigationBar(
        modifier = modifier
    ) {
        homeTaps.forEach { tab ->
            val selected = navBackStackEntry?.destination.isHomeLevelTab(tab)

            SooumNavigationBarItem(
                selected = selected,
                icon = {
                    Icon(
                        painter = painterResource(id = tab.unselectedIconId),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                },
                selectedIcon = {
                    Icon(
                        painter = painterResource(id = tab.selectedIconId),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.iconTextId),
                        style = TextComponent.CAPTION_1_SB_12,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                },
                onClick = {
                    //  현재 선택이 되어 있는 탭을 제외한 클릭만 동작하도록
                    if (!selected) {
                        navController.navigate(tab.graph) {
                            popUpTo(HomeTabType.FEED.route)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }

}

private const val TAG = "SooumBottomBar"