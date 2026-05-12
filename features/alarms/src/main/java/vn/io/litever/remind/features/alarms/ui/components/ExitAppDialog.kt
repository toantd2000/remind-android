package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    modifier: Modifier = Modifier
) {
    LiteverAlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButtonText = stringResource(R.string.exit_dialog_confirm),
        onConfirmClick = onConfirmExit,
        dismissButtonText = stringResource(R.string.exit_dialog_cancel),
        onDismissClick = onDismissRequest,
        title = stringResource(R.string.exit_dialog_title),
        modifier = modifier
    ) {
        androidx.compose.foundation.layout.Column {
            androidx.compose.material3.Text(
                text = stringResource(R.string.exit_dialog_message),
                style = vn.io.litever.designsystem.theme.LiteverTheme.typography.bodyMedium,
                color = vn.io.litever.designsystem.theme.LiteverTheme.colors.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                LocalAdManager.current.NativeAdView(
                    placement = AdPlacement.EXIT_NATIVE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
