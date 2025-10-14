package com.phew.presentation.write.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.phew.presentation.write.viewmodel.WriteViewModel

/**
 *  추후 작업
 *  1. 완료 되면 어디로 이동해야 하는지
 */
@Composable
internal fun WriteRoute(
    modifier: Modifier = Modifier,
    viewModel: WriteViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {

}

@Composable
private fun WriteScreen(
    modifier: Modifier = Modifier,
) {

}

private const val TAG = "WriteScreen"