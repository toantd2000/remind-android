package vn.io.litever.remind.app.ui.components

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import vn.io.litever.remind.app.R
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.LocalAdManager

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

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = onConfirmExit) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(cancelText)
            }
        },
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!isAdFreeActive) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MaterialTheme.spacing.extraSmall)
                    ) {
                        adManager.NativeAdView(
                            placement = AdPlacement.EXIT_NATIVE,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}
