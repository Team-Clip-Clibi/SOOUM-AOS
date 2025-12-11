package com.phew.splash

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary

@Composable
fun Splash(
    viewModel: SplashViewModel,
    signUp: () -> Unit,
    update: () -> Unit,
    finish: () -> Unit,
    home: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.saveNotify(isGranted)
        }
    )
    val view = LocalView.current

    // [이 화면 전용 설정] 시스템 바 영역 무시하고 전체 화면 사용하기
    if (!view.isInEditMode) {
        androidx.compose.runtime.DisposableEffect(Unit) {
            val window = (view.context as android.app.Activity).window
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)

            // 1. 현재 설정 저장 (나갈 때 복구용)
            val originalStatusBarColor = window.statusBarColor
            val originalNavColor = window.navigationBarColor

            // 2. "시스템 바 영역 무시하고 꽉 채우기" 설정 (핵심!)
            // false로 설정하면 뷰가 시스템 바 뒤까지 확장됩니다.
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

            // 3. 확장된 뷰가 보이도록 시스템 바 배경색을 투명으로 변경
            // (이걸 안 하면 확장은 되는데 흰색 바가 위를 덮어버립니다)
            window.statusBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
            window.navigationBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()

            // 아이콘 색상 밝게 (배경이 파란색이라서)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false

            onDispose {
                // 4. 이 화면을 나갈 때 원래 설정으로 복구 (다른 화면에 영향 주지 않기 위해)
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, true)
                window.statusBarColor = originalStatusBarColor
                window.navigationBarColor = originalNavColor

                // 아이콘 색상 복구 (필요시 true/false 조정)
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
            }
        }
    }
    LaunchedEffect(uiState) {
        when (uiState) {
            UiState.Success -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    viewModel.saveNotify(true)
                } else {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            UiState.SignUpPage -> {
                signUp()
            }

            UiState.FeedPage -> {
                home()
            }

            is UiState.Error -> {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.error_network),
                    duration = SnackbarDuration.Short
                )
                viewModel.initError()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHost(hostState = snackBarHostState)
        },
        containerColor = Primary.MAIN,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Primary.MAIN),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(id = com.phew.core_design.R.drawable.ic_sooum_black),
                contentDescription = "app logo",
                tint = NeutralColor.WHITE,
                modifier = Modifier
                    .width(200.dp)
                    .height(33.dp)
                    .padding(1.dp)
            )
            if (uiState is UiState.Update) {
                DialogComponent.DefaultButtonOne(
                    title = stringResource(R.string.splash_dialog_update_title),
                    description = stringResource(R.string.splash_dialog_update_description),
                    buttonText = stringResource(R.string.splash_dialog_update_btn),
                    onClick = {
                        update()
                    },
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }
}
