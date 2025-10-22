package com.phew.core_design

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavDeepLink
import androidx.navigation.NamedNavArgument
import androidx.navigation.compose.composable


fun NavGraphBuilder.slideComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    val spec = tween<IntOffset>(durationMillis = 300, easing = FastOutSlowInEasing)

    composable(
        route = route,
        arguments = arguments,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = spec)
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = spec)
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = spec)
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = spec)
        },
        content = content
    )
}
