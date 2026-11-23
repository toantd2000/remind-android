package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import androidx.compose.material3.Button
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.features.mission.R

@Composable
fun BarcodeMissionContent(
    targetBarcode: String,
    label: String,
    onInteraction: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val mismatchTemplate = stringResource(R.string.barcode_mismatch_error, label.ifBlank { targetBarcode })
    val scanFailedMessage = stringResource(R.string.barcode_scan_failed)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(LiteverTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
    ) {
        Icon(
            imageVector = ReMindIcons.QrCodeScanner,
            contentDescription = null,
            tint = LiteverTheme.colors.primary,
            modifier = Modifier.size(64.dp)
        )

        if (label.isNotBlank() || targetBarcode.isNotBlank()) {
            Surface(
                shape = LiteverTheme.shapes.medium,
                color = LiteverTheme.colors.surfaceContainerHigh
            ) {
                Text(
                    text = label.ifBlank { targetBarcode },
                    style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LiteverTheme.colors.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage.orEmpty(),
                style = LiteverTheme.typography.bodyMedium,
                color = LiteverTheme.colors.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onInteraction()
                errorMessage = null
                val options = GmsBarcodeScannerOptions.Builder()
                    .enableAutoZoom()
                    .build()
                val scanner = GmsBarcodeScanning.getClient(context, options)
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        val scanned = barcode.rawValue.orEmpty()
                        if (targetBarcode.isBlank() || scanned == targetBarcode) {
                            onComplete()
                        } else {
                            errorMessage = mismatchTemplate
                        }
                    }
                    .addOnFailureListener {
                        errorMessage = scanFailedMessage
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = ReMindIcons.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.barcode_scan_to_dismiss),
                style = LiteverTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
