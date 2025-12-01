package com.phew.sooum.navigation

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.model.navigation.ProfileArgs
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core_common.log.SooumLog
import com.phew.feed.navigation.navigateToFeedGraph
import com.phew.home.navigation.navigateToHomeGraph
import com.phew.presentation.detail.navigation.navigateToDetailGraph
import com.phew.profile.navigateToProfileGraphWithArgs
import com.phew.sign_up.navigation.SIGN_UP_GRAPH
import com.phew.splash.navigation.SPLASH_GRAPH

/**
 *  TODO 기획에서 양식 받아야 함
 */
class DeepLinkHandler {
    
    companion object {
        private const val TAG = "DeepLinkHandler"
        
        fun handleDeepLink(navController: NavHostController, deepLinkUrl: String?) {
            if (deepLinkUrl.isNullOrBlank()) {
                SooumLog.d(TAG, "딥링크가 없습니다.")
                return
            }
            
            SooumLog.d(TAG, "딥링크 처리 시작: $deepLinkUrl")
            
            try {
                when {
                    deepLinkUrl.startsWith("sooum://feed") -> {
                        navigateToFeed(navController)
                    }
                    
                    deepLinkUrl.startsWith("sooum://detail/") -> {
                        val cardId = extractCardIdFromUrl(deepLinkUrl)
                        if (cardId != null) {
                            navigateToDetail(navController, cardId)
                        } else {
                            SooumLog.e(TAG, "카드 ID를 추출할 수 없습니다: $deepLinkUrl")
                            navigateToFeed(navController)
                        }
                    }
                    
                    deepLinkUrl.startsWith("sooum://profile/") -> {
                        val userId = extractUserIdFromUrl(deepLinkUrl)
                        if (userId != null) {
                            navigateToProfile(navController, userId)
                        } else {
                            SooumLog.e(TAG, "사용자 ID를 추출할 수 없습니다: $deepLinkUrl")
                            navigateToFeed(navController)
                        }
                    }
                    
                    deepLinkUrl.startsWith("sooum://notify") -> {
                        navigateToNotification(navController)
                    }
                    
                    else -> {
                        SooumLog.w(TAG, "알 수 없는 딥링크 형식: $deepLinkUrl")
                        navigateToFeed(navController)
                    }
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "딥링크 처리 중 오류 발생: ${e.message}")
                navigateToFeed(navController)
            }
        }
        
        private fun extractCardIdFromUrl(url: String): Long? {
            return try {
                val path = url.removePrefix("sooum://detail/")
                path.toLongOrNull()
            } catch (e: Exception) {
                SooumLog.e(TAG, "카드 ID 추출 실패: ${e.message}")
                null
            }
        }
        
        private fun extractUserIdFromUrl(url: String): Long? {
            return try {
                val path = url.removePrefix("sooum://profile/")
                path.toLongOrNull()
            } catch (e: Exception) {
                SooumLog.e(TAG, "사용자 ID 추출 실패: ${e.message}")
                null
            }
        }
        
        private fun navigateToFeed(navController: NavHostController) {
            SooumLog.d(TAG, "피드로 이동")
            navController.navigateToHomeGraph(
                navOptions = navOptions {
                    popUpTo(SPLASH_GRAPH) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            )
        }
        
        private fun navigateToDetail(navController: NavHostController, cardId: Long) {
            SooumLog.d(TAG, "카드 상세로 이동: $cardId")
            ensureHomeGraph(navController) {
                navController.navigateToDetailGraph(
                    cardDetailArgs = CardDetailArgs(cardId = cardId)
                )
            }
        }
        
        private fun navigateToProfile(navController: NavHostController, userId: Long) {
            SooumLog.d(TAG, "프로필로 이동: $userId")
            ensureHomeGraph(navController) {
                navController.navigateToProfileGraphWithArgs(
                    ProfileArgs(userId = userId)
                )
            }
        }
        
        private fun navigateToNotification(navController: NavHostController) {
            SooumLog.d(TAG, "알림으로 이동")
            ensureHomeGraph(navController) {
                navController.navigateToFeedGraph()
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
                    restoreState = true
                }
            )
        }

        private fun String?.isHomeDestination(): Boolean {
            if (this.isNullOrEmpty()) return false
            return HomeTabType.entries.any { tab ->
                this.startsWith(tab.graph) || this.startsWith(tab.route)
            }
        }
    }
}
