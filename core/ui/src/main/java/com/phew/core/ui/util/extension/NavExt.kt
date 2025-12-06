package com.phew.core.ui.util.extension

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.phew.core.ui.component.home.HomeTabType

fun NavDestination?.isHomeLevelTab(tab: HomeTabType) =
    this?.hierarchy?.any {
        it.route?.contains(tab.route, true) ?: false
    } ?: false
