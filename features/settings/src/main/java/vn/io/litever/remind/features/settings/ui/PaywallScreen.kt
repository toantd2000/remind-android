package vn.io.litever.remind.features.settings.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.AdState
import vn.io.litever.remind.core.ads.api.LocalAdManager
import vn.io.litever.remind.core.designsystem.components.ReMindPlanBadge
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.BillingPlanType
import vn.io.litever.remind.core.model.ProProduct
import vn.io.litever.remind.core.model.ProProductIds
import vn.io.litever.remind.core.model.PurchaseStatus
import vn.io.litever.remind.features.settings.R

@Composable
fun PaywallRoute(
    initialSlide: Int = 0,
    onNavigateBack: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val productsUnavailableMsg = stringResource(R.string.paywall_products_unavailable)
    val restoreSuccessMsg = stringResource(R.string.paywall_restore_success)
    val restoreEmptyMsg = stringResource(R.string.paywall_restore_empty)

    LaunchedEffect(uiState.purchaseStatus) {
        when (val status = uiState.purchaseStatus) {
            is PurchaseStatus.Success -> {
                viewModel.clearPurchaseStatus()
                onNavigateBack()
            }
            is PurchaseStatus.Restored -> {
                Toast.makeText(context, restoreSuccessMsg, Toast.LENGTH_SHORT).show()
                viewModel.clearPurchaseStatus()
                onNavigateBack()
            }
            is PurchaseStatus.NothingToRestore -> {
                Toast.makeText(context, restoreEmptyMsg, Toast.LENGTH_LONG).show()
                viewModel.clearPurchaseStatus()
            }
            is PurchaseStatus.Error -> {
                Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                viewModel.clearPurchaseStatus()
            }
            is PurchaseStatus.Cancelled -> {
                viewModel.clearPurchaseStatus()
            }
            else -> {}
        }
    }

    PaywallScreen(
        uiState = uiState,
        initialSlide = initialSlide,
        onSelectPlan = viewModel::selectPlan,
        onPurchaseClick = {
            val activity = context.findPaywallActivity()
            if (activity != null) {
                val launched = viewModel.purchaseSelectedPlan(activity)
                if (!launched) {
                    Toast.makeText(context, productsUnavailableMsg, Toast.LENGTH_SHORT).show()
                }
            }
        },
        onManageSubscriptionClick = {
            openPlaySubscriptions(context)
        },
        onRestoreClick = viewModel::restorePurchases,
        onRewardGranted = { viewModel.grant24hAdFreeReward(context) },
        onCloseClick = onNavigateBack
    )
}

@Composable
fun PaywallScreen(
    uiState: PaywallUiState,
    initialSlide: Int = 0,
    onSelectPlan: (BillingPlanType) -> Unit,
    onPurchaseClick: () -> Unit,
    onManageSubscriptionClick: () -> Unit = {},
    onRestoreClick: () -> Unit,
    onRewardGranted: () -> Unit,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    val notReadyMessage = stringResource(R.string.rewarded_ad_not_ready)
    var showAdLoading by remember { mutableStateOf(false) }
    var showThankYouDialog by remember { mutableStateOf(false) }

    if (!isInspection) {
        val adManager = LocalAdManager.current
        val adState by adManager.adState.collectAsState()
        LaunchedEffect(adState, showAdLoading) {
            if (showAdLoading) {
                when (adState) {
                    is AdState.Loaded -> {
                        showAdLoading = false
                        val activity = context.findPaywallActivity()
                        if (activity != null) {
                            adManager.showAd(activity, AdPlacement.SUPPORT_REWARDED) {
                                onRewardGranted()
                                showThankYouDialog = true
                            }
                        }
                    }
                    is AdState.Failed -> {
                        showAdLoading = false
                        Toast.makeText(context, notReadyMessage, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    val pagerState = rememberPagerState(
        initialPage = initialSlide.coerceIn(0, 5),
        pageCount = { 6 }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.extraSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = ReMindIcons.Close,
                        contentDescription = stringResource(R.string.close_text),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!uiState.isPro) {
                    val isRestoring = uiState.purchaseStatus == PurchaseStatus.Restoring
                    TextButton(
                        onClick = onRestoreClick,
                        enabled = !isRestoring && uiState.purchaseStatus != PurchaseStatus.Loading
                    ) {
                        if (isRestoring) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = ReMindIcons.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Text(
                            text = stringResource(
                                if (isRestoring) {
                                    R.string.paywall_restoring_purchases
                                } else {
                                    R.string.paywall_restore_purchases
                                }
                            ),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        .padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.smallMedium
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isPro) {
                        Button(
                            onClick = onManageSubscriptionClick,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.paywall_manage_subscription),
                                style = LiteverTokens.typography.titleMediumEmphasized
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.paywall_cancel_anytime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                        val ctaText = when (uiState.selectedPlan) {
                            BillingPlanType.ANNUAL -> stringResource(R.string.paywall_cta_trial)
                            else -> stringResource(R.string.paywall_cta_subscribe)
                        }

                        Button(
                            onClick = onPurchaseClick,
                            enabled = uiState.purchaseStatus != PurchaseStatus.Loading,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (uiState.purchaseStatus == PurchaseStatus.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = ctaText,
                                    style = LiteverTokens.typography.titleMediumEmphasized
                                )
                            }
                        }

                        if (!isInspection) {
                            val adManager = LocalAdManager.current
                            TextButton(
                                onClick = {
                                    val activity = context.findPaywallActivity()
                                    if (activity != null && adManager.isAdLoaded(AdPlacement.SUPPORT_REWARDED)) {
                                        adManager.showAd(activity, AdPlacement.SUPPORT_REWARDED) {
                                            onRewardGranted()
                                            showThankYouDialog = true
                                        }
                                    } else {
                                        adManager.loadAd(AdPlacement.SUPPORT_REWARDED)
                                        showAdLoading = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.paywall_rewarded_ad_cta),
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(
                    if (uiState.isPro) R.string.paywall_hero_title_active else R.string.paywall_hero_title
                ),
                style = LiteverTokens.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            Text(
                text = stringResource(
                    if (uiState.isPro) R.string.paywall_active_desc else R.string.paywall_social_proof
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Carousel: Comparison table + 5 Feature Spotlights (uniform height prevents jump)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> ComparisonSlide()
                    1 -> FeatureSpotlightSlide(
                        icon = ReMindIcons.Extension,
                        title = stringResource(R.string.paywall_slide_missions_title),
                        description = stringResource(R.string.paywall_slide_missions_desc)
                    )
                    2 -> FeatureSpotlightSlide(
                        icon = ReMindIcons.Speaker,
                        title = stringResource(R.string.paywall_slide_tts_title),
                        description = stringResource(R.string.paywall_slide_tts_desc)
                    )
                    3 -> FeatureSpotlightSlide(
                        icon = ReMindIcons.AlarmOn,
                        title = stringResource(R.string.paywall_slide_wakeup_title),
                        description = stringResource(R.string.paywall_slide_wakeup_desc)
                    )
                    4 -> FeatureSpotlightSlide(
                        icon = ReMindIcons.VolumeUp,
                        title = stringResource(R.string.paywall_slide_loud_title),
                        description = stringResource(R.string.paywall_slide_loud_desc)
                    )
                    else -> FeatureSpotlightSlide(
                        icon = ReMindIcons.Schedule,
                        title = stringResource(R.string.paywall_slide_reminder_title),
                        description = stringResource(R.string.paywall_slide_reminder_desc)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

            // Pager Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            if (uiState.isPro) {
                ActiveProMemberCard()
            } else {
                // Plan Selector Cards (Annual & Monthly only)
                val monthlyProduct = uiState.products.firstOrNull { it.planType == BillingPlanType.MONTHLY }
                val annualProduct = uiState.products.firstOrNull { it.planType == BillingPlanType.ANNUAL }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    PlanOptionCard(
                        title = stringResource(R.string.paywall_plan_annual),
                        priceText = "${annualProduct?.formattedPrice ?: "$6.99"}${stringResource(R.string.paywall_plan_annual_suffix)}",
                        badgeText = stringResource(R.string.paywall_plan_annual_badge),
                        selected = uiState.selectedPlan == BillingPlanType.ANNUAL,
                        onClick = { onSelectPlan(BillingPlanType.ANNUAL) }
                    )

                    PlanOptionCard(
                        title = stringResource(R.string.paywall_plan_monthly),
                        priceText = "${monthlyProduct?.formattedPrice ?: "$0.99"}${stringResource(R.string.paywall_plan_monthly_suffix)}",
                        badgeText = null,
                        selected = uiState.selectedPlan == BillingPlanType.MONTHLY,
                        onClick = { onSelectPlan(BillingPlanType.MONTHLY) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }
    }

    if (showAdLoading) {
        AlertDialog(
            onDismissRequest = { showAdLoading = false },
            title = { Text(stringResource(R.string.watch_ad_dialog_title)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.loading_ad_message),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdLoading = false }) {
                    Text(stringResource(R.string.close_text))
                }
            }
        )
    }

    if (showThankYouDialog) {
        AlertDialog(
            onDismissRequest = { showThankYouDialog = false },
            title = { Text(stringResource(R.string.watch_ad_dialog_title)) },
            text = { Text(stringResource(R.string.ads_disabled_reward_message)) },
            confirmButton = {
                Button(onClick = { showThankYouDialog = false }) {
                    Text(stringResource(R.string.close_text))
                }
            }
        )
    }
}

@Composable
private fun ActiveProMemberCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = ReMindIcons.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Text(
                        text = stringResource(R.string.pro_banner_active_title),
                        style = LiteverTokens.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    ReMindPlanBadge(isPro = true)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.pro_banner_active_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}

private val PAYWALL_SLIDE_HEIGHT = 288.dp

@Composable
private fun ComparisonSlide() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(PAYWALL_SLIDE_HEIGHT),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.smallMedium
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(68.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Diamond,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.paywall_col_pro),
                            style = LiteverTokens.typography.labelMediumEmphasized,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.paywall_col_basic),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(56.dp)
                )
            }

            val rows = listOf(
                stringResource(R.string.paywall_feat_basic_alarm) to true,
                stringResource(R.string.paywall_feat_no_ads) to false,
                stringResource(R.string.paywall_feat_mission_chain) to false,
                stringResource(R.string.paywall_feat_voice_tts) to false,
                stringResource(R.string.paywall_feat_wakeup_check) to false,
                stringResource(R.string.paywall_feat_extra_loud) to false,
                stringResource(R.string.paywall_feat_multi_reminder) to false,
                stringResource(R.string.paywall_feat_pro_branding) to false
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                rows.forEachIndexed { index, (label, isFree) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier.width(68.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ReMindIcons.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.width(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFree) ReMindIcons.Check else ReMindIcons.Close,
                                contentDescription = null,
                                tint = if (isFree) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    if (index < rows.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureSpotlightSlide(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(PAYWALL_SLIDE_HEIGHT),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            Text(
                text = title,
                style = LiteverTokens.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlanOptionCard(
    title: String,
    priceText: String,
    badgeText: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (badgeText != null) 10.dp else 0.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onClick),
            shape = MaterialTheme.shapes.large,
            color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
            else MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected,
                    onClick = onClick
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = LiteverTokens.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = priceText,
                    style = LiteverTokens.typography.titleMediumEmphasized,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (badgeText != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Text(
                    text = badgeText,
                    style = LiteverTokens.typography.labelSmallEmphasized,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
    }
}

private fun openPlaySubscriptions(context: Context) {
    val uri = "https://play.google.com/store/account/subscriptions?package=${context.packageName}".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        // Play Store unavailable
    }
}

private fun Context.findPaywallActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Preview(showBackground = true, name = "Paywall - Free (Light)")
@Composable
private fun PaywallScreenPreview() {
    ReMindTheme {
        PaywallScreen(
            uiState = PaywallUiState(
                isPro = false,
                products = listOf(
                    ProProduct(ProProductIds.SUB_MONTHLY, BillingPlanType.MONTHLY, "$0.99"),
                    ProProduct(ProProductIds.SUB_ANNUAL, BillingPlanType.ANNUAL, "$6.99", hasFreeTrial = true, trialDays = 7)
                ),
                selectedPlan = BillingPlanType.ANNUAL
            ),
            onSelectPlan = {},
            onPurchaseClick = {},
            onRestoreClick = {},
            onRewardGranted = {},
            onCloseClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Paywall - Free (Dark)")
@Composable
private fun PaywallScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        PaywallScreen(
            uiState = PaywallUiState(
                isPro = false,
                products = listOf(
                    ProProduct(ProProductIds.SUB_MONTHLY, BillingPlanType.MONTHLY, "$0.99"),
                    ProProduct(ProProductIds.SUB_ANNUAL, BillingPlanType.ANNUAL, "$6.99", hasFreeTrial = true, trialDays = 7)
                ),
                selectedPlan = BillingPlanType.ANNUAL
            ),
            onSelectPlan = {},
            onPurchaseClick = {},
            onRestoreClick = {},
            onRewardGranted = {},
            onCloseClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Paywall - Active Pro")
@Composable
private fun PaywallScreenActiveProPreview() {
    ReMindTheme {
        PaywallScreen(
            uiState = PaywallUiState(isPro = true),
            onSelectPlan = {},
            onPurchaseClick = {},
            onRestoreClick = {},
            onRewardGranted = {},
            onCloseClick = {}
        )
    }
}
