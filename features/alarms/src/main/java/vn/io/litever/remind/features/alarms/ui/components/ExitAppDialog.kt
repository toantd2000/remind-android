package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverAlertDialog
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.LocalAdManager
import vn.io.litever.remind.features.alarms.R

@Composable
fun ExitAppDialog(
    onDismissRequest: () -> Unit,
    onConfirmExit: () -> Unit,
    isAdFreeActive: Boolean,
    modifier: Modifier = Modifier
) {
    val adManager = LocalAdManager.current

    val title = stringResource(R.string.exit_dialog_title)
    val message = if (isAdFreeActive) {
        stringResource(R.string.exit_dialog_message_ad_free)
    } else {
        stringResource(R.string.exit_dialog_message)
    }
    val confirmText = stringResource(R.string.exit_dialog_confirm)
    val cancelText = stringResource(R.string.exit_dialog_cancel)

    LiteverAlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButtonText = confirmText,
        onConfirmClick = onConfirmExit,
        dismissButtonText = cancelText,
        onDismissClick = onDismissRequest,
        title = title,
        modifier = modifier
    ) {
        Column {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (!isAdFreeActive) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    adManager.NativeAdView(
                        placement = AdPlacement.EXIT_NATIVE,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
