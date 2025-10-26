package com.phew.presentation.detail.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.model.navigation.CardDetailCommentArgs
import com.phew.core.ui.navigation.NavArgKey
import com.phew.core.ui.navigation.asNavArg
import com.phew.core.ui.navigation.asNavParam
import com.phew.core.ui.navigation.createNavType
import com.phew.core.ui.navigation.getNavArg
import com.phew.core_common.log.SooumLog
import com.phew.core_design.slideComposable
import com.phew.presentation.detail.screen.CardDetailRoute

val DETAIL_GRAPH = "detail_graph".asNavParam()

private val DETAIL_ROUTE = "detail_route".asNavParam()
private val COMMENT_ROUTE = "comment_route".asNavParam()

fun NavHostController.navigateToDetailGraph(
    cardDetailArgs: CardDetailArgs,
    navOptions: NavOptions? = null
) {
    SooumLog.i(TAG, "navigateToDetailGraph() $cardDetailArgs")
    this.navigate(DETAIL_GRAPH.asNavArg(cardDetailArgs), navOptions)
}

fun NavHostController.navigateToWriteFromDetail(
    cardId: Long,
    navOptions: NavOptions? = null
) {
    SooumLog.i(TAG, "navigateToWriteFromDetail() cardId: $cardId")
    // TODO: Write 모듈로 네비게이션 구현 필요
}

private fun NavHostController.navigateToDetailRoute(
    cardDetailArgs: CardDetailArgs,
    navOptions: NavOptions? = null
) {
    this.navigate(DETAIL_ROUTE.asNavArg(cardDetailArgs), navOptions)
}


private fun NavHostController.navigateToDetailCommentRoute(
    cardDetailCommentArgs: CardDetailCommentArgs,
    navOptions: NavOptions? = null
) {
    SooumLog.i(TAG, "navigateToDetailRoute() $cardDetailCommentArgs")
    this.navigate(COMMENT_ROUTE.asNavArg(cardDetailCommentArgs), navOptions)
}

fun NavGraphBuilder.detailGraph(
    navController: NavController,
    onBackPressed: () -> Unit,
    onNavigateToWrite: (Long) -> Unit,
    onNavigateToReport: (Long) -> Unit,
    onWriteComplete: () -> Unit = {},
    detailScreen: @Composable (
        CardDetailArgs,
        (CardDetailCommentArgs) -> Unit,
        () -> Unit
    ) -> Unit = { _, _, _ -> },
    commentScreen: @Composable (
        CardDetailCommentArgs,
        (CardDetailCommentArgs) -> Unit,
        () -> Unit
    ) -> Unit = { _, _, _ -> }
) {
    navigation(
        route = DETAIL_GRAPH,
        startDestination = DETAIL_ROUTE,
        arguments = listOf(
            navArgument(NavArgKey) {
                type = createNavType<CardDetailArgs>()
            }
        )
    ) {
        slideComposable(
            route = DETAIL_ROUTE,
        ) { backStackEntry ->
            val args = backStackEntry.arguments?.getNavArg<CardDetailArgs>()
            if (args == null) {
                SooumLog.e(TAG, "CardDetailArgs is null")
                onBackPressed()
            } else {
                CardDetailRoute(
                    args = args,
                    onNavigateToComment = { commentArgs ->
                        navController.navigate(COMMENT_ROUTE.asNavArg(commentArgs))
                    },
                    onNavigateToWrite = { cardId ->
                        onNavigateToWrite(cardId)
                    },
                    onNavigateToReport = onNavigateToReport,
                    onBackPressed = onBackPressed
                )
            }
        }

        slideComposable(
            route = COMMENT_ROUTE,
            arguments = listOf(
                navArgument(NavArgKey) {
                    type = createNavType<CardDetailCommentArgs>()
                }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments?.getNavArg<CardDetailCommentArgs>()
            if (args == null) {
                SooumLog.e(TAG, "CardDetailCommentArgs is null")
                navController.popBackStack()
            } else {
                // //   TODO 스크린 개발되면 수정 예정
//                commentScreen(
//                    args = args,
//                    sooumAppState = sooumAppState,
//                    onNavigateToChildComment = { childArgs ->
//                        navController.navigate(COMMENT_ROUTE.asNavArg(childArgs))
//                    },
//                    onBackPressed = { navController.popBackStack() }
//                )
            }
        }
    }
}

private const val TAG = "CardDetailNavigation"
