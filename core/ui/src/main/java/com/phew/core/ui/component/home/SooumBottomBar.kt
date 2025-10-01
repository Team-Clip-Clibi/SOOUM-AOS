package com.phew.core.ui.component.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.phew.core.ui.compose.ComposableVisibleState
import com.phew.core.ui.util.extension.shouldShowBottomBar

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
        visibleState.setEnabled(shouldShowBottomBar)
    }


}