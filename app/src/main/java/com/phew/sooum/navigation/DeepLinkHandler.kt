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
                    
                    deepLinkUrl.startsWith("sooum://card/") -> {
                        val cardId = extractCardIdFromCardUrl(deepLinkUrl)
                        val backTo = extractBackToParam(deepLinkUrl)
                        if (cardId != null) {
                            navigateToDetail(navController, cardId, backTo)
                        } else {
                            SooumLog.e(TAG, "카드 ID를 추출할 수 없습니다: $deepLinkUrl")
                            navigateToFeed(navController)
                        }
                    }
                    
                    deepLinkUrl.startsWith("sooum://profile/") -> {
                        val userId = extractUserIdFromUrl(deepLinkUrl)
                        val backTo = extractBackToParam(deepLinkUrl)
                        if (userId != null) {
                            navigateToProfile(navController, userId, backTo)
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
        
        private fun extractCardIdFromCardUrl(url: String): Long? {
            return try {
                val path = url.removePrefix("sooum://card/")
                // 쿼리 파라미터 제거 (?backTo=feed 등)
                val cardIdString = path.split("?").firstOrNull() ?: path
                cardIdString.toLongOrNull()
            } catch (e: Exception) {
                SooumLog.e(TAG, "카드 ID 추출 실패: ${e.message}")
                null
            }
        }
        
        private fun extractUserIdFromUrl(url: String): Long? {
            return try {
                val path = url.removePrefix("sooum://profile/")
                // 쿼리 파라미터 제거 (?backTo=feed 등)
                val userIdString = path.split("?").firstOrNull() ?: path
                userIdString.toLongOrNull()
            } catch (e: Exception) {
                SooumLog.e(TAG, "사용자 ID 추출 실패: ${e.message}")
                null
            }
        }
        
        private fun extractBackToParam(url: String): String? {
            return try {
                val uri = android.net.Uri.parse(url)
                uri.getQueryParameter("backTo")
            } catch (e: Exception) {
                SooumLog.e(TAG, "backTo 파라미터 추출 실패: ${e.message}")
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
        
        private fun navigateToDetail(navController: NavHostController, cardId: Long, backTo: String? = null) {
            SooumLog.d(TAG, "카드 상세로 이동: $cardId, backTo: $backTo")
            
            // backTo 파라미터에 따라 적절한 그래프로 먼저 이동
            when (backTo) {
                "tag" -> {
                    // 태그 화면을 백스택에 넣고 카드 상세로 이동
                    ensureHomeGraph(navController) {
                        // TODO: 태그 화면으로 먼저 이동 후 카드 상세
                        navController.navigateToDetailGraph(
                            cardDetailArgs = CardDetailArgs(cardId = cardId)
                        )
                    }
                }
                "my" -> {
                    // 마이 화면을 백스택에 넣고 카드 상세로 이동  
                    ensureHomeGraph(navController) {
                        // TODO: 마이 화면으로 먼저 이동 후 카드 상세
                        navController.navigateToDetailGraph(
                            cardDetailArgs = CardDetailArgs(cardId = cardId)
                        )
                    }
                }
                "feed", null -> {
                    // 기본적으로 피드에서 카드 상세로 이동
                    ensureHomeGraph(navController) {
                        navController.navigateToDetailGraph(
                            cardDetailArgs = CardDetailArgs(cardId = cardId)
                        )
                    }
                }
                else -> {
                    // 알 수 없는 backTo 값이면 기본 동작
                    ensureHomeGraph(navController) {
                        navController.navigateToDetailGraph(
                            cardDetailArgs = CardDetailArgs(cardId = cardId)
                        )
                    }
                }
            }
        }
        
        private fun navigateToProfile(navController: NavHostController, userId: Long, backTo: String? = null) {
            SooumLog.d(TAG, "프로필로 이동: $userId, backTo: $backTo")
            
            // 홈 그래프를 확인한 후 프로필로 이동
            // backTo 파라미터는 참고용으로만 사용 (실제 백스택 동작은 안드로이드 네비게이션에 의존)
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
