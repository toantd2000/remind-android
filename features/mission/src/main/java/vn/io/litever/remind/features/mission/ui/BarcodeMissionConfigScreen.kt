package vn.io.litever.remind.features.mission.ui

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.BarcodeMissionConfig
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.features.mission.R

@Composable
fun BarcodeMissionConfigRoute(
    alarmId: Long,
    initialBarcode: String,
    initialLabel: String,
    onBackClick: () -> Unit,
    onSaveMission: (Mission) -> Unit
) {
    BarcodeMissionConfigScreen(
        initialBarcode = initialBarcode,
        initialLabel = initialLabel,
        onBackClick = onBackClick,
        onSaveClick = { barcode, label ->
            onSaveMission(
                Mission(
                    alarmId = alarmId,
                    type = MissionType.BARCODE,
                    order = 0,
                    repeatCount = 1,
                    config = BarcodeMissionConfig(targetBarcode = barcode, label = label)
                )
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeMissionConfigScreen(
    initialBarcode: String = "",
    initialLabel: String = "",
    onBackClick: () -> Unit,
    onSaveClick: (String, String) -> Unit
) {
    val context = LocalContext.current
    var targetBarcode by remember { mutableStateOf(initialBarcode) }
    var itemLabel by remember { mutableStateOf(initialLabel) }
    val scanFailedMessage = stringResource(R.string.barcode_scan_failed)

    Scaffold(
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.barcode_config_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                Button(
                    onClick = { onSaveClick(targetBarcode.trim(), itemLabel.trim()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_mission_action))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(LiteverTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
        ) {
            ReMindSettingsGroup(title = stringResource(R.string.barcode_registered_label)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LiteverTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = LiteverTheme.shapes.medium,
                        color = LiteverTheme.colors.surfaceContainerHighest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = targetBarcode.ifBlank { stringResource(R.string.barcode_not_set) },
                            style = LiteverTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (targetBarcode.isNotBlank()) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(LiteverTheme.spacing.medium)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            val options = GmsBarcodeScannerOptions.Builder()
                                .enableAutoZoom()
                                .build()
                            val scanner = GmsBarcodeScanning.getClient(context, options)
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    barcode.rawValue?.let { value ->
                                        targetBarcode = value
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, scanFailedMessage, Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = ReMindIcons.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.barcode_scan_to_register))
                    }
                }
            }

            ReMindSettingsGroup(title = stringResource(R.string.barcode_label_hint)) {
                val labelState = rememberTextFieldState(initialText = itemLabel)
                LaunchedEffect(labelState) {
                    snapshotFlow { labelState.text.toString() }.collect {
                        if (it != itemLabel) itemLabel = it
                    }
                }
                BasicTextField(
                    state = labelState,
                    textStyle = LiteverTheme.typography.bodyLarge.copy(color = LiteverTheme.colors.onSurface),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    cursorBrush = SolidColor(LiteverTheme.colors.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LiteverTheme.spacing.medium),
                    decorator = TextFieldDecorator { inner ->
                        if (labelState.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.barcode_label_hint),
                                style = LiteverTheme.typography.bodyLarge,
                                color = LiteverTheme.colors.outlineVariant
                            )
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarcodeMissionConfigScreenPreview() {
    ReMindTheme {
        BarcodeMissionConfigScreen(
            initialBarcode = "8934563138165",
            initialLabel = "Toothpaste",
            onBackClick = {},
            onSaveClick = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BarcodeMissionConfigScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        BarcodeMissionConfigScreen(
            onBackClick = {},
            onSaveClick = { _, _ -> }
        )
    }
}
