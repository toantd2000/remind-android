package vn.io.litever.remind.features.settings.ui

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
import vn.io.litever.remind.features.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributionsScreen(
    onNavigateToLicenses: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.setting_attributions_title),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Text(
                    text = stringResource(R.string.attributions_thanks_message),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(LiteverTheme.spacing.medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                ReMindSettingsGroup {
                    ReMindSettingsItem(
                        title = stringResource(R.string.attribution_oss_title),
                        subtitle = stringResource(R.string.attribution_oss_desc),
                        icon = Icons.Rounded.Code,
                        onClick = onNavigateToLicenses
                    )

                    ReMindSettingsItem(
                        title = stringResource(R.string.attribution_storyset_title),
                        subtitle = stringResource(R.string.attribution_storyset_desc),
                        icon = Icons.Rounded.Palette,
                        onClick = { launchCustomTab(context, "https://storyset.com") }
                    )
                    
                    // Future items can be added here
                }
            }
        }
    }
}

private fun launchCustomTab(context: Context, url: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, url.toUri())
    } catch (e: Exception) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }
}
