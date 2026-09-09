package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.LiteVerButtonDefaults
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.alarms.R

@Composable
fun PermissionWarningBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(LiteverTheme.spacing.medium),
        color = LiteverTheme.colors.errorContainer,
        shape = LiteverTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(LiteverTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = LiteverTheme.colors.error
                )
                Spacer(modifier = Modifier.width(LiteverTheme.spacing.smallMedium))
                Text(
                    text = stringResource(R.string.permission_warning_banner_title),
                    style = LiteverTheme.typography.titleSmall,
                    color = LiteverTheme.colors.error,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))
            
            Text(
                text = stringResource(R.string.permission_warning_banner_desc),
                style = LiteverTheme.typography.bodySmall,
                color = LiteverTheme.colors.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(LiteverTheme.spacing.smallMedium))

            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End),
                shape = LiteVerButtonDefaults.shape,
                colors = LiteVerButtonDefaults.destructiveColors()
            ) {
                Text(stringResource(R.string.permission_warning_banner_action))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionWarningBannerPreview() {
    ReMindTheme {
        PermissionWarningBanner(onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionWarningBannerDarkPreview() {
    ReMindTheme(darkTheme = true) {
        PermissionWarningBanner(onClick = {})
    }
}










