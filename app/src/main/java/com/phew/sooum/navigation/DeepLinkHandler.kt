package com.phew.sooum.navigation

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import androidx.core.net.toUri
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.model.navigation.CardDetailCommentArgs
import com.phew.core.ui.state.SooumAppState
import com.phew.core_common.CardDetailTrace
import com.phew.core_common.log.SooumLog
import com.phew.feed.navigation.navigateToFeedGraph
import com.phew.home.navigation.navigateToHomeGraph
import com.phew.presentation.detail.navigation.navigateToDetailCommentDirect
import com.phew.presentation.detail.navigation.navigateToDetailGraph
import com.phew.profile.TAB_FOLLOWER
import com.phew.profile.TAB_FOLLOWING
import com.phew.profile.navigateToFollowScreen
import com.phew.sign_up.navigation.SIGN_UP_GRAPH
import com.phew.splash.navigation.SPLASH_GRAPH
import com.phew.sooum.session.TransferSuccessHandler
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class DeepLinkHandler @Inject constructor(
    private val transferSuccessHandler: TransferSuccessHandler
) {

    suspend fun handleDeepLink(
        navController: NavHostController,
        deepLinkUrl: String?,
        appState: SooumAppState? = null
    ) {
        if (deepLinkUrl.isNullOrBlank()) {
            SooumLog.d(TAG, "딥링크가 없습니다.")
            return
        }

        SooumLog.d(TAG, "딥링크 처리 시작: $deepLinkUrl")

        appState?.updateDeepLinkNavigating(true)

        try {
            when {
                deepLinkUrl.startsWith(TransferSuccessHandler.TRANSFER_SUCCESS_DEEP_LINK) -> {
                    transferSuccessHandler.handleFromDeepLink(navController, appState)
                    return
                }

                deepLinkUrl.startsWith("sooum://feed") -> {
                    navigateToFeed(navController, appState)
                }

                deepLinkUrl.startsWith("sooum://card/") -> {
                    val cardId = extractCardIdFromCardUrl(deepLinkUrl)
                    val backTo = extractBackToParam(deepLinkUrl)
                    val detailView = extractDetailViewParam(deepLinkUrl)
                    if (cardId != null) {
                        navigateToDetail(navController, cardId, backTo, detailView, appState)
                    } else {
                        SooumLog.e(TAG, "카드 ID를 추출할 수 없습니다: $deepLinkUrl")
                        navigateToFeed(navController, appState)
                    }
                }

                deepLinkUrl.startsWith("sooum://follow") -> {
                    val targetTab = extractFollowTab(deepLinkUrl) ?: TAB_FOLLOWER
                    navigateToFollow(navController, appState, targetTab)
                }

                else -> {
                    SooumLog.w(TAG, "알 수 없는 딥링크 형식: $deepLinkUrl")
                    navigateToFeed(navController, appState)
                }
            }
        } catch (e: Exception) {
            SooumLog.e(TAG, "딥링크 처리 중 오류 발생: ${e.message}")
            navigateToFeed(navController, appState)
        }
    }

    private fun extractCardIdFromCardUrl(url: String): Long? {
        return try {
            val path = url.removePrefix("sooum://card/")
            val cardIdString = path.split("?").firstOrNull() ?: path
            cardIdString.toLongOrNull()
        } catch (e: Exception) {
            SooumLog.e(TAG, "카드 ID 추출 실패: ${e.message}")
            null
        }
    }

    private fun extractBackToParam(url: String): String? {
        return try {
            val uri = url.toUri()
            uri.getQueryParameter("backTo")
        } catch (e: Exception) {
            SooumLog.e(TAG, "backTo 파라미터 추출 실패: ${e.message}")
            null
        }
    }

    private fun extractDetailViewParam(url: String): DetailView? {
        return try {
            val uri = url.toUri()
            when (uri.getQueryParameter("view")?.lowercase()) {
                "detail" -> DetailView.DETAIL
                "comment" -> DetailView.COMMENT
                else -> null
            }
        } catch (e: Exception) {
            SooumLog.e(TAG, "view 파라미터 추출 실패: ${e.message}")
            null
        }
    }

    private fun extractFollowTab(url: String): Int? {
        return try {
            val uri = url.toUri()
            when (uri.getQueryParameter("tab")?.lowercase()) {
                "following" -> TAB_FOLLOWING
                "follower" -> TAB_FOLLOWER
                else -> null
            }
        } catch (e: Exception) {
            SooumLog.e(TAG, "follow tab 파라미터 추출 실패: ${e.message}")
            null
        }
    }

    private fun navigateToFeed(navController: NavHostController, appState: SooumAppState? = null) {
        SooumLog.d(TAG, "피드로 이동")
        navController.navigateToHomeGraph(
            navOptions = navOptions {
                popUpTo(SPLASH_GRAPH) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        )
        appState?.updateDeepLinkNavigating(false)
    }

    private fun navigateToDetail(
        navController: NavHostController,
        cardId: Long,
        backTo: String? = null,
        detailView: DetailView? = null,
        appState: SooumAppState? = null
    ) {
        SooumLog.d(TAG, "카드 상세로 이동: $cardId, backTo: $backTo")
        val targetView = detailView ?: DetailView.COMMENT

        appState?.updateDeepLinkNavigating(true)

        ensureHomeGraph(navController) {
            if (targetView == DetailView.DETAIL) {
                navController.navigateToDetailGraph(
                    cardDetailArgs = CardDetailArgs(cardId = cardId),
                    navOptions = navOptions {
                        launchSingleTop = true
                    }
                )
            } else {
                navController.navigateToDetailCommentDirect(
                    cardDetailCommentArgs = CardDetailCommentArgs(
                        cardId = cardId,
                        parentId = 0L,
                        backTo = backTo,
                    )
                )
            }

            val detailListener = object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination,
                    arguments: Bundle?
                ) {
                    val route = destination.route
                    SooumLog.d(TAG, "=== Detail navigation listener - destination: $route ===")

                    if (route?.contains("detail") == true) {
                        controller.removeOnDestinationChangedListener(this)
                        SooumLog.d(TAG, "Detail 화면 도달, 딥링크 내비게이션 완료 예약")

                        appState?.let {
                            CoroutineScope(Dispatchers.Main).launch {
                                it.navController.currentBackStackEntryFlow
                                    .filter { entry -> entry.destination.route?.contains("detail") == true }
                                    .first()

                                SooumLog.d(TAG, "딥링크 내비게이션 완료 처리 시작")
                                it.updateDeepLinkNavigating(false)
                                SooumLog.d(TAG, "딥링크 내비게이션 완료 처리 완료")
                            }
                        }
                    }
                }
            }
            navController.addOnDestinationChangedListener(detailListener)
        }
    }

    private fun navigateToFollow(
        navController: NavHostController,
        appState: SooumAppState? = null,
        selectTab: Int = TAB_FOLLOWER
    ) {
        SooumLog.d(TAG, "팔로워 화면으로 이동 - 홈 → 마이 → 팔로우")

        ensureHomeGraph(navController) {
            appState?.updateDeepLinkNavigating(true)
            SooumLog.d(TAG, "딥링크 상태 설정: true")

            navController.navigateToHomeGraph(
                navOptions = navOptions {
                    popUpTo(SPLASH_GRAPH) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            )
            SooumLog.d(TAG, "홈 그래프로 이동 완료")

            val navigationListener = object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination,
                    arguments: Bundle?
                ) {
                    val route = destination.route
                    SooumLog.d(TAG, "Follow navigation listener - destination: $route")

                    if (route?.startsWith("feed-") == true) {
                        controller.removeOnDestinationChangedListener(this)
                        SooumLog.d(TAG, "홈 화면 도달, MY 탭으로 이동")

                        CoroutineScope(Dispatchers.Main).launch {
                            controller.navigate(HomeTabType.MY.graph) {
                                popUpTo(HomeTabType.FEED.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            SooumLog.d(TAG, "MY 탭으로 이동 요청")

                            delay(100)
                            SooumLog.d(TAG, "팔로우 화면으로 이동")

                            navController.navigateToFollowScreen(
                                isMyProfile = true,
                                selectTab = selectTab,
                                navOptions = navOptions {
                                    launchSingleTop = true
                                }
                            )

                            delay(200)
                            SooumLog.d(TAG, "팔로우 딥링크 내비게이션 완료")
                            appState?.updateDeepLinkNavigating(false)
                        }
                    }
                }
            }
            navController.addOnDestinationChangedListener(navigationListener)
        }
    }

    private fun ensureHomeGraph(
        navController: NavHostController,
        onReady: () -> Unit
    ) {
        if (navController.currentDestination?.route.isHomeDestination()) {
            onReady()
            return
        }

        val listener = object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                SooumLog.d(TAG, "onDestinationChanged: ${destination.route}")
                if (destination.route.isHomeDestination()) {
                    controller.removeOnDestinationChangedListener(this)
                    onReady()
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)

        navController.navigateToHomeGraph(
            navOptions = navOptions {
                popUpTo(SPLASH_GRAPH) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        )
    }

    private fun String?.isHomeDestination(): Boolean {
        if (this.isNullOrEmpty()) return false
        return HomeTabType.entries.any { tab ->
            this.startsWith(tab.graph) || this.startsWith(tab.route)
        }
    }

    private enum class DetailView {
        DETAIL, COMMENT
    }

    companion object {
        private const val TAG = "DeepLinkHandler"
    }
}
