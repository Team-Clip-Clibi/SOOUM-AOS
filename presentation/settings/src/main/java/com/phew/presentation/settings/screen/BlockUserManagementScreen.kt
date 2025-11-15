package com.phew.presentation.settings.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.phew.core_design.AppBar.IconLeftAppBar
import com.phew.core_design.Danger
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.SmallButton
import com.phew.core_design.TextComponent
import com.phew.domain.model.BlockMember
import com.phew.presentation.settings.R
import com.phew.presentation.settings.viewmodel.BlockUserManagementUiEffect
import com.phew.presentation.settings.viewmodel.BlockUserManagementUiState
import com.phew.presentation.settings.viewmodel.BlockUserManagementViewModel
import kotlinx.coroutines.flow.collectLatest
import com.phew.core_design.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BlockUserManagementRoute(
    modifier: Modifier = Modifier,
    viewModel: BlockUserManagementViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val blockUsers = viewModel.blockUsers.collectAsLazyPagingItems()
    val context = LocalContext.current

    LaunchedEffect(blockUsers) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                BlockUserManagementUiEffect.ShowUnblockSuccess -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.block_user_diable),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is BlockUserManagementUiEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                BlockUserManagementUiEffect.RefreshBlockList -> {
                    blockUsers.refresh()
                }
            }
        }
    }

    BlockUserManagementScreen(
        modifier = modifier,
        uiState = uiState,
        blockMembers = blockUsers,
        onBackPressed = onBackPressed,
        onRefresh = { blockUsers.refresh() },
        onUnblockClick = viewModel::showUnblockDialog,
        onUnblockConfirm = viewModel::unblockUser,
        onUnblockCancel = viewModel::hideUnblockDialog
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockUserManagementScreen(
    modifier: Modifier,
    uiState: BlockUserManagementUiState,
    blockMembers: LazyPagingItems<BlockMember>,
    onBackPressed: () -> Unit,
    onRefresh: () -> Unit,
    onUnblockClick: (BlockMember) -> Unit,
    onUnblockConfirm: () -> Unit,
    onUnblockCancel: () -> Unit
) {
    Scaffold(
        modifier = modifier.background(NeutralColor.WHITE),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeutralColor.WHITE)
            ) {
                IconLeftAppBar(
                    image = DesignR.drawable.ic_left,
                    onClick = onBackPressed,
                    appBarText = stringResource(R.string.block_user_top_title)
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = blockMembers.loadState.refresh is LoadState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier.padding(innerPadding)
        ) {
            when {
                blockMembers.loadState.refresh is LoadState.Loading && blockMembers.itemCount == 0 -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                blockMembers.loadState.refresh !is LoadState.Loading && blockMembers.itemCount == 0 -> {
                    EmptyBlockListContent()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NeutralColor.WHITE),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(blockMembers.itemCount) { index ->
                            val blockMember = blockMembers[index]
                            if (blockMember != null) {
                                BlockUserItem(
                                    blockMember = blockMember,
                                    onUnblockClick = { onUnblockClick(blockMember) }
                                )
                            }
                        }

                        if (blockMembers.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Unblock dialog
    if (uiState.showUnblockDialog && uiState.selectedBlockMember != null) {
        val selectedMember = uiState.selectedBlockMember
        DialogComponent.DefaultButtonTwo(
            title = stringResource(R.string.block_user_dialog_title),
            description = stringResource(R.string.block_user_dialog_content, selectedMember.blockMemberNickname),
            buttonTextStart = stringResource(R.string.block_user_dialog_cancel),
            buttonTextEnd = stringResource(R.string.block_user_dialog_disable),
            onClick = onUnblockConfirm,
            onDismiss = onUnblockCancel,
            rightButtonBaseColor = Danger.M_RED
        )
    }
}

@Composable
private fun BlockUserItem(
    blockMember: BlockMember,
    onUnblockClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(blockMember.blockMemberProfileImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(DesignR.drawable.ic_profile),
                error = painterResource(DesignR.drawable.ic_profile)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = blockMember.blockMemberNickname,
                style = TextComponent.BODY_1_M_14,
                color = NeutralColor.GRAY_600,
                modifier = Modifier.weight(1f)
            )
        }

        SmallButton.NoIconPrimary(
            buttonText = stringResource(R.string.block_user_diable),
            onClick = onUnblockClick,
            modifier = Modifier
        )
    }
}

@Composable
private fun EmptyBlockListContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralColor.WHITE)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_hide_stoke),
            contentDescription = "No blocked users",
            modifier = Modifier.size(24.dp),
            tint = NeutralColor.GRAY_200
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.block_no_user),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400
        )
    }
}
