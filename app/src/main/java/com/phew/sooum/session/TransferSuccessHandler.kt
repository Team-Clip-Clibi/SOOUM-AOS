package com.phew.sooum.session

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.phew.core.ui.model.navigation.OnBoardingArgs
import com.phew.core.ui.state.SooumAppState
import com.phew.core_common.di.ApplicationScope
import com.phew.domain.interceptor.InterceptorManger
import com.phew.sign_up.navigation.navigateToOnBoarding
import com.phew.splash.navigation.SPLASH_GRAPH
import com.phew.sooum.MainActivity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class TransferSuccessHandler @Inject constructor(
    private val interceptorManger: InterceptorManger,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    fun handleFromService(context: Context) {
        applicationScope.launch {
            performLogout()
            withContext(Dispatchers.Main) {
                launchOnboardingActivity(context)
            }
        }
    }

    suspend fun handleFromDeepLink(
        navController: NavHostController,
        appState: SooumAppState?
    ) {
        performLogout()
        withContext(Dispatchers.Main) {
            appState?.updateDeepLinkNavigating(false)
            navController.navigateToOnBoarding(
                args = OnBoardingArgs(showWithdrawalDialog = false),
                navOptions = navOptions {
                    popUpTo(SPLASH_GRAPH) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            )
        }
    }

    private suspend fun performLogout() {
        withContext(Dispatchers.IO) {
            interceptorManger.deleteAll()
            interceptorManger.resetToken()
        }
    }

    private fun launchOnboardingActivity(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            TRANSFER_SUCCESS_DEEP_LINK.toUri(),
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("deep_link", TRANSFER_SUCCESS_DEEP_LINK)
        }
        context.startActivity(intent)
    }

    companion object {
        const val TRANSFER_SUCCESS_DEEP_LINK = "sooum://transfer-success"
    }
}
