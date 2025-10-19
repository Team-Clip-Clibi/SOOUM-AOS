package com.phew.core.ui.util.extension

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.phew.core.ui.component.home.HomeTabType


fun NavDestination?.isHomeLevelTab(tab: HomeTabType) =
    this?.hierarchy?.any {
        it.route?.contains(tab.route, true) ?: false
    } ?: false

fun NavDestination?.shouldShowBottomBar(homeLevelTabs: List<HomeTabType>): Boolean {
    if (this.isHomeLevelTab(HomeTabType.WRITE)) {
        return false
    }

    return homeLevelTabs
        .filterNot { it == HomeTabType.WRITE }
        .any { tab -> this.isHomeLevelTab(tab) }
}
