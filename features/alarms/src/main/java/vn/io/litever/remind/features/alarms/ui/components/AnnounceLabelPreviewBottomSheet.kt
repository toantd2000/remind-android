package vn.io.litever.remind.features.alarms.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.audio.TtsLanguageOption
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.components.ReMindGroupCard
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.alarms.R

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
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
        containerColor = LiteverTheme.colors.surface,
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
        else -> resolvedLocale.getDisplayLanguage(java.util.Locale.getDefault())
            .replaceFirstChar { it.titlecase(java.util.Locale.getDefault()) }
    }
    val selectedLanguageDisplay = when (selectedLanguage) {
        TtsLanguageOption.AUTO -> stringResource(R.string.tts_language_auto_detected, resolvedLanguageName)
        TtsLanguageOption.VIETNAMESE -> stringResource(R.string.tts_language_vi)
        TtsLanguageOption.ENGLISH -> stringResource(R.string.tts_language_en)
        TtsLanguageOption.CHINESE -> stringResource(R.string.tts_language_zh)
    }
    val statusTint by animateColorAsState(
        targetValue = if (isSpeaking) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant,
        label = "ttsStatusTint"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LiteverTheme.spacing.medium)
            .padding(bottom = LiteverTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
    ) {
        ReMindGroupCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LiteverTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.smallMedium)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    Icon(
                        imageVector = if (isSpeaking) ReMindIcons.GraphicEq else ReMindIcons.Speaker,
                        contentDescription = null,
                        tint = statusTint,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (isSpeaking) {
                            stringResource(R.string.tts_preview_sheet_speaking)
                        } else {
                            stringResource(R.string.tts_preview_sheet_ready)
                        },
                        style = LiteverTheme.typography.bodySmall.copy(
                            fontWeight = if (isSpeaking) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = statusTint
                    )
                }

                HorizontalDivider(
                    color = LiteverTheme.colors.outlineVariant.copy(alpha = 0.3f)
                )

                Text(
                    text = "“$spokenText”",
                    style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LiteverTheme.colors.onSurface
                )

                if (isFallback) {
                    Text(
                        text = stringResource(R.string.tts_preview_label_fallback_hint),
                        style = LiteverTheme.typography.bodySmall,
                        color = LiteverTheme.colors.onSurfaceVariant
                    )
                }

                HorizontalDivider(
                    color = LiteverTheme.colors.outlineVariant.copy(alpha = 0.3f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.tts_preview_language_label, selectedLanguageDisplay),
                        style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LiteverTheme.colors.primary,
                        modifier = Modifier.weight(1f)
                    )
                    LvButton(
                        onClick = onOpenTtsSettingsClick,
                        type = LvButtonType.Text
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.tts_preview_open_settings),
                            style = LiteverTheme.typography.labelMedium
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                    style = LiteverTheme.typography.labelMedium
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
                        .padding(LiteverTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Warning,
                            contentDescription = null,
                            tint = LiteverTheme.colors.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.tts_preview_missing_voice_data),
                            style = LiteverTheme.typography.bodySmall,
                            color = LiteverTheme.colors.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        LvButton(
                            onClick = onInstallVoiceDataClick,
                            type = LvButtonType.Outlined
                        ) {
                            Text(
                                text = stringResource(R.string.tts_preview_install_voice_data),
                                style = LiteverTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.smallMedium)
        ) {
            LvButton(
                onClick = onCloseClick,
                type = LvButtonType.Tonal,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.close_text),
                    style = LiteverTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            LvButton(
                onClick = if (isSpeaking) onStopClick else onPreviewClick,
                type = if (isSpeaking) LvButtonType.Outlined else LvButtonType.Filled,
                modifier = Modifier
                    .weight(1.4f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = if (isSpeaking) ReMindIcons.Pause else ReMindIcons.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSpeaking) {
                        stringResource(R.string.tts_preview_action_stop)
                    } else {
                        stringResource(R.string.tts_preview_action_replay)
                    },
                    style = LiteverTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Preview(showBackground = true)
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AnnounceLabelPreviewBottomSheetIdleDarkPreview() {
    ReMindTheme {
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
