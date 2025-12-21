package com.phew.presentation.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.phew.core_design.AppBar.IconLeftAppBar
import com.phew.core_design.NeutralColor
import com.phew.core_design.R as DesignR
import com.phew.presentation.settings.R
import com.phew.core.ui.model.navigation.WebViewUrlArgs
import com.phew.presentation.settings.component.privacy.PrivacyPolicyItemRow
import com.phew.presentation.settings.model.privacy.PrivacyPolicyItem
import com.phew.presentation.settings.model.privacy.PrivacyPolicyItemId
import com.phew.presentation.settings.viewmodel.PrivacyPolicyNavigationEvent
import com.phew.presentation.settings.viewmodel.PrivacyPolicyType
import com.phew.presentation.settings.viewmodel.PrivacyPolicyViewModel

@Composable
internal fun PrivacyPolicyRoute(
    modifier: Modifier = Modifier,
    viewModel: PrivacyPolicyViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onNavigateToWebView: (WebViewUrlArgs) -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is PrivacyPolicyNavigationEvent.NavigateToWebView -> {
                    onNavigateToWebView(event.args)
                }
            }
        }
    }

    PrivacyPolicyScreen(
        modifier = modifier,
        items = viewModel.getPrivacyPolicyItems(),
        onItemClick = { item ->
            val type = when (item.id) {
                PrivacyPolicyItemId.PRIVACY_POLICY_PERSONAL_INFO -> PrivacyPolicyType.PERSONAL_INFO
                PrivacyPolicyItemId.PRIVACY_POLICY_TERMS_OF_SERVICE -> PrivacyPolicyType.TERMS_OF_SERVICE
                PrivacyPolicyItemId.PRIVACY_POLICY_LOCATION_INFO -> PrivacyPolicyType.LOCATION_INFO
            }
            viewModel.onPrivacyPolicyItemClick(type)
        },
        onBackPressed = onBackPressed
    )
}

@Composable
private fun PrivacyPolicyScreen(
    modifier: Modifier = Modifier,
    items: List<PrivacyPolicyItem>,
    onItemClick: (PrivacyPolicyItem) -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeutralColor.WHITE)
            ) {
                IconLeftAppBar(
                    image = DesignR.drawable.ic_left,
                    onClick = onBackPressed,
                    appBarText = stringResource(R.string.privacy_policy_title)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(NeutralColor.WHITE)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            itemsIndexed(items) { index, item ->
                PrivacyPolicyItemRow(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}