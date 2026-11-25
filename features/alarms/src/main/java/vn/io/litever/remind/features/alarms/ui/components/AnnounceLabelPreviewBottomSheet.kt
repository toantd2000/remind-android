package vn.io.litever.remind.features.alarms.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.common.audio.TtsLanguageOption
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.components.ReMindGroupCard
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsDivider
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.features.alarms.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnounceLabelPreviewBottomSheet(
    label: String,
    isSpeaking: Boolean,
    isLanguageDataMissing: Boolean = false,
    selectedLanguage: TtsLanguageOption = TtsLanguageOption.AUTO,
    onLanguageSelected: (TtsLanguageOption) -> Unit = {},
    onPreviewClick: () -> Unit,
    onStopClick: () -> Unit,
    onInstallVoiceDataClick: () -> Unit = {},
    onOpenTtsSettingsClick: () -> Unit = {},
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
) {
    LaunchedEffect(Unit) {
        onPreviewClick()
    }

    DisposableEffect(Unit) {
        onDispose { onStopClick() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.announce_label_tts_title)
        ) {
            AnnounceLabelPreviewBottomSheetContent(
                label = label,
                isSpeaking = isSpeaking,
                isLanguageDataMissing = isLanguageDataMissing,
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                onPreviewClick = onPreviewClick,
                onStopClick = onStopClick,
                onInstallVoiceDataClick = onInstallVoiceDataClick,
                onOpenTtsSettingsClick = onOpenTtsSettingsClick,
                onCloseClick = onDismissRequest
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnnounceLabelPreviewBottomSheetContent(
    label: String,
    isSpeaking: Boolean,
    isLanguageDataMissing: Boolean = false,
    selectedLanguage: TtsLanguageOption = TtsLanguageOption.AUTO,
    onLanguageSelected: (TtsLanguageOption) -> Unit = {},
    onPreviewClick: () -> Unit,
    onStopClick: () -> Unit,
    onInstallVoiceDataClick: () -> Unit = {},
    onOpenTtsSettingsClick: () -> Unit = {},
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFallback = label.isBlank()
    val spokenText = if (isFallback) {
        stringResource(R.string.tts_preview_label_default)
    } else {
        label
    }
    val resolvedLocale = selectedLanguage.resolveLocale(spokenText)
    val resolvedLanguageName = when (resolvedLocale.language) {
        "vi" -> stringResource(R.string.tts_language_vi)
        "en" -> stringResource(R.string.tts_language_en)
        "zh" -> stringResource(R.string.tts_language_zh)
        else -> resolvedLocale.getDisplayLanguage(Locale.getDefault())
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
    val selectedLanguageDisplay = when (selectedLanguage) {
        TtsLanguageOption.AUTO -> stringResource(R.string.tts_language_auto_detected, resolvedLanguageName)
        TtsLanguageOption.VIETNAMESE -> stringResource(R.string.tts_language_vi)
        TtsLanguageOption.ENGLISH -> stringResource(R.string.tts_language_en)
        TtsLanguageOption.CHINESE -> stringResource(R.string.tts_language_zh)
    }
    val statusTint by animateColorAsState(
        targetValue = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "ttsStatusTint"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(bottom = MaterialTheme.spacing.medium)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
    ) {
        ReMindGroupCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Icon(
                        imageVector = if (isSpeaking) ReMindIcons.GraphicEq else ReMindIcons.Speaker,
                        contentDescription = null,
                        tint = statusTint,
                        modifier = Modifier.size(MaterialTheme.spacing.mediumLarge)
                    )
                    Text(
                        text = if (isSpeaking) {
                            stringResource(R.string.tts_preview_sheet_speaking)
                        } else {
                            stringResource(R.string.tts_preview_sheet_ready)
                        },
                        style = if (isSpeaking) {
                            LiteverTokens.typography.bodySmallEmphasized
                        } else {
                            MaterialTheme.typography.bodySmall
                        },
                        color = statusTint
                    )
                }

                ReMindSettingsDivider(inset = false)

                Text(
                    text = "“$spokenText”",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isFallback) {
                    Text(
                        text = stringResource(R.string.tts_preview_label_fallback_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                ReMindSettingsDivider(inset = false)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.tts_preview_language_label, selectedLanguageDisplay),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onOpenTtsSettingsClick) {
                        Icon(
                            imageVector = ReMindIcons.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(MaterialTheme.spacing.medium)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Text(
                            text = stringResource(R.string.tts_preview_open_settings),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    TtsLanguageOption.entries.forEach { option ->
                        val chipLabel = when (option) {
                            TtsLanguageOption.AUTO -> stringResource(R.string.tts_language_auto)
                            TtsLanguageOption.VIETNAMESE -> stringResource(R.string.tts_language_vi)
                            TtsLanguageOption.ENGLISH -> stringResource(R.string.tts_language_en)
                            TtsLanguageOption.CHINESE -> stringResource(R.string.tts_language_zh)
                        }
                        FilterChip(
                            selected = selectedLanguage == option,
                            onClick = { onLanguageSelected(option) },
                            label = {
                                Text(
                                    text = chipLabel,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }
        }

        if (isLanguageDataMissing) {
            ReMindGroupCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.tts_preview_missing_voice_data),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = onInstallVoiceDataClick) {
                            Text(text = stringResource(R.string.tts_preview_install_voice_data))
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
        ) {
            FilledTonalButton(
                onClick = onCloseClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(text = stringResource(R.string.close_text))
            }

            val speakButtonContent: @Composable RowScope.() -> Unit = {
                Icon(
                    imageVector = if (isSpeaking) ReMindIcons.Pause else ReMindIcons.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = if (isSpeaking) {
                        stringResource(R.string.tts_preview_action_stop)
                    } else {
                        stringResource(R.string.tts_preview_action_replay)
                    }
                )
            }
            if (isSpeaking) {
                OutlinedButton(
                    onClick = onStopClick,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp),
                    content = speakButtonContent
                )
            } else {
                Button(
                    onClick = onPreviewClick,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp),
                    content = speakButtonContent
                )
            }
        }
    }
}

@Preview(name = "TTS Preview - Speaking Light", showBackground = true)
@Composable
private fun AnnounceLabelPreviewBottomSheetSpeakingPreview() {
    ReMindTheme {
        AnnounceLabelPreviewBottomSheetContent(
            label = "Morning Workout",
            isSpeaking = true,
            onPreviewClick = {},
            onStopClick = {},
            onCloseClick = {}
        )
    }
}

@Preview(
    name = "TTS Preview - Idle Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
private fun AnnounceLabelPreviewBottomSheetIdleDarkPreview() {
    ReMindTheme(darkTheme = true) {
        AnnounceLabelPreviewBottomSheetContent(
            label = "",
            isSpeaking = false,
            isLanguageDataMissing = true,
            onPreviewClick = {},
            onStopClick = {},
            onCloseClick = {}
        )
    }
}
