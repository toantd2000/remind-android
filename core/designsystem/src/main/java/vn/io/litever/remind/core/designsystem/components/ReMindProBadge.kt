package vn.io.litever.remind.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing

/**
 * Shared brand gradient brush used for Pro plan surfaces (badges, icons) across the app,
 * keeping visual parity between TopAppBar badges and the Settings Pro header.
 */
@Composable
fun rememberReMindProGradient(): Brush = Brush.linearGradient(
    colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )
)

/**
 * Simple, unified Pro/Free plan badge shared across TopAppBars and Settings.
 *
 * - **Pro (`isPro = true`):** Brand gradient (`primary` -> `tertiary`) with `onPrimary` label.
 * - **Free (`isPro = false`):** Neutral surface (`surfaceContainerHigh`) with subtle outline and `onSurfaceVariant` label.
 *
 * @param isPro Whether the user currently has an active Pro plan.
 * @param modifier Optional [Modifier] applied to the outer pill container.
 * @param onClick Optional click handler. When provided, the pill becomes interactive.
 */
@Composable
fun ReMindPlanBadge(
    isPro: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val proGradient = rememberReMindProGradient()
    val containerModifier = if (isPro) {
        Modifier.background(brush = proGradient)
    } else {
        Modifier.background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
    }
    val contentColor = if (isPro) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val border = if (isPro) {
        null
    } else {
        BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
    val label = stringResource(if (isPro) R.string.plan_badge_pro else R.string.plan_badge_free)

    Surface(
        modifier = modifier
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = CircleShape,
        color = Color.Transparent,
        border = border
    ) {
        Box(
            modifier = containerModifier.padding(
                horizontal = MaterialTheme.spacing.smallMedium,
                vertical = MaterialTheme.spacing.extraSmall
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = LiteverTokens.typography.labelSmallEmphasized,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Compact Pro/Free pill button placed in TopAppBar actions to surface current plan status
 * and navigate to the paywall/subscription screen.
 */
@Composable
fun ReMindProTopBarButton(
    isPro: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReMindPlanBadge(
        isPro = isPro,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * Compact `PRO` pill tag indicating a locked Pro feature.
 *
 * @param showLockIcon Whether to include a leading lock icon (use `false` when paired with
 *   [ReMindProUnlockButton] in the same row to avoid duplicate lock icons).
 */
@Composable
fun ReMindProTag(
    modifier: Modifier = Modifier,
    showLockIcon: Boolean = true
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (showLockIcon) {
                Icon(
                    imageVector = ReMindIcons.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(10.dp)
                )
            }
            Text(
                text = stringResource(R.string.plan_badge_pro),
                style = LiteverTokens.typography.labelSmallEmphasized,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

/**
 * Compact trailing unlock icon button shown in place of a [androidx.compose.material3.Switch]
 * on Pro-gated settings rows when the user does not have an active Pro subscription.
 */
@Composable
fun ReMindProUnlockButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = ReMindIcons.Lock,
            contentDescription = stringResource(R.string.pro_unlock_action),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(name = "Plan Badges - Light", showBackground = true)
@Composable
private fun ReMindProBadgeLightPreview() {
    ReMindTheme {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.smallMedium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReMindProTopBarButton(isPro = false, onClick = {})
            ReMindProTopBarButton(isPro = true, onClick = {})
            ReMindProTag()
            ReMindProUnlockButton(onClick = {})
        }
    }
}

@Preview(
    name = "Plan Badges - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
private fun ReMindProBadgeDarkPreview() {
    ReMindTheme(darkTheme = true) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.smallMedium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReMindProTopBarButton(isPro = false, onClick = {})
            ReMindProTopBarButton(isPro = true, onClick = {})
            ReMindProTag()
            ReMindProUnlockButton(onClick = {})
        }
    }
}
