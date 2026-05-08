package vn.io.litever.remind.features.settings.ui

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.components.*
import vn.io.litever.remind.features.settings.R

@Composable
fun AttributionsScreen(
    onNavigateToLicenses: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    ReMindScaffold(
        topBar = {
            ReMindTopAppBar(
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
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                ReMindSettingsGroup {
                    SettingsItem(
                        title = stringResource(R.string.attribution_oss_title),
                        subtitle = stringResource(R.string.attribution_oss_desc),
                        icon = Icons.Rounded.Code,
                        onClick = onNavigateToLicenses
                    )

                    SettingsItem(
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
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
