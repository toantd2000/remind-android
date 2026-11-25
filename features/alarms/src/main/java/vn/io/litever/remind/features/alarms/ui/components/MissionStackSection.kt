package vn.io.litever.remind.features.alarms.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.R as DesignSystemR
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType

private const val MAX_MISSION_SLOTS = 5

/**
 * Interactive 5-slot Mission stack editor for [vn.io.litever.remind.features.alarms.ui.AlarmEditScreen].
 *
 * Single-point interaction on the 5 slots:
 * - **Filled slots:** Order badge (`1..5`), mission icon, repeat count (`×N`), and a crisp remove badge.
 * - **Next add slot:** Dashed `primary` border inviting the user to add the next mission.
 * - **Future unlocked empty slots:** Subtle muted dashed outline so multiple `+` boxes don't compete.
 * - **Pro-locked slots:** Soft `surfaceContainerHigh` box with lock indicator and slot index.
 */
@Composable
fun MissionStackSection(
    missions: List<Mission>,
    isPro: Boolean,
    onMissionClick: (Mission) -> Unit,
    onMissionRemove: (Mission) -> Unit,
    onAddMissionClick: () -> Unit,
    onLockedSlotClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.smallMedium
            ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            repeat(MAX_MISSION_SLOTS) { slotIndex ->
                val mission = missions.getOrNull(slotIndex)
                val isSlotUnlocked = isPro || slotIndex == 0
                val isNextAddSlot = mission == null && slotIndex == missions.size && isSlotUnlocked

                MissionSlotCard(
                    slotNumber = slotIndex + 1,
                    mission = mission,
                    isUnlocked = isSlotUnlocked,
                    isNextAddSlot = isNextAddSlot,
                    onFilledClick = { mission?.let(onMissionClick) },
                    onRemoveClick = { mission?.let(onMissionRemove) },
                    onAddClick = {
                        if (isSlotUnlocked) {
                            onAddMissionClick()
                        } else {
                            onLockedSlotClick()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        AnimatedVisibility(visible = missions.isEmpty()) {
            Text(
                text = stringResource(DesignSystemR.string.mission_empty_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MissionSlotCard(
    slotNumber: Int,
    mission: Mission?,
    isUnlocked: Boolean,
    isNextAddSlot: Boolean,
    onFilledClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slotShape = MaterialTheme.shapes.medium

    Box(
        modifier = modifier.height(68.dp),
        contentAlignment = Alignment.Center
    ) {
        if (mission != null) {
            Surface(
                onClick = onFilledClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 6.dp, start = 4.dp, end = 6.dp),
                shape = slotShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = MaterialTheme.spacing.extraSmall),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = missionIcon(mission.type),
                        contentDescription = missionTitle(mission.type),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.tiny))
                    Text(
                        text = "×${mission.repeatCount}",
                        style = LiteverTokens.typography.labelSmallEmphasized.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }
            }

            // Order badge (1..5) at TopStart
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(20.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$slotNumber",
                        style = LiteverTokens.typography.labelSmallEmphasized.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )
                }
            }

            // Remove badge (×) at TopEnd
            Surface(
                onClick = onRemoveClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = ReMindIcons.Close,
                        contentDescription = stringResource(DesignSystemR.string.action_delete),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        } else {
            val dashColor = when {
                isNextAddSlot -> MaterialTheme.colorScheme.primary
                isUnlocked -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                else -> Color.Transparent
            }
            val containerColor = when {
                isNextAddSlot -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                isUnlocked -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
            }
            val iconTint = when {
                isNextAddSlot -> MaterialTheme.colorScheme.primary
                isUnlocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }

            Surface(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 6.dp, start = 4.dp, end = 6.dp)
                    .then(
                        if (dashColor != Color.Transparent) {
                            Modifier.drawBehind {
                                val strokeWidth = if (isNextAddSlot) 1.5.dp.toPx() else 1.dp.toPx()
                                val dash = 5.dp.toPx()
                                val gap = 4.dp.toPx()
                                drawRoundRect(
                                    color = dashColor,
                                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                                    style = Stroke(
                                        width = strokeWidth,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)
                                    )
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                shape = slotShape,
                color = containerColor
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) ReMindIcons.Add else ReMindIcons.Lock,
                        contentDescription = if (isUnlocked) {
                            stringResource(DesignSystemR.string.add_mission)
                        } else {
                            stringResource(DesignSystemR.string.plan_badge_pro)
                        },
                        tint = iconTint,
                        modifier = Modifier.size(if (isNextAddSlot) 22.dp else 18.dp)
                    )
                    if (!isUnlocked) {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.tiny))
                        Text(
                            text = "#$slotNumber",
                            style = MaterialTheme.typography.labelSmall,
                            color = iconTint,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun missionIcon(type: MissionType): ImageVector = when (type) {
    MissionType.TYPING -> ReMindIcons.Keyboard
    MissionType.MATH -> ReMindIcons.Calculate
    MissionType.MEMORY_FIND_COLOR_TILES -> ReMindIcons.GridView
    MissionType.SHAKE -> ReMindIcons.Vibration
    MissionType.BARCODE -> ReMindIcons.QrCodeScanner
    MissionType.RANDOM -> ReMindIcons.Refresh
}

@Composable
private fun missionTitle(type: MissionType): String = when (type) {
    MissionType.TYPING -> stringResource(DesignSystemR.string.mission_typing)
    MissionType.MATH -> stringResource(DesignSystemR.string.mission_math)
    MissionType.MEMORY_FIND_COLOR_TILES -> stringResource(DesignSystemR.string.mission_memory_tiles)
    MissionType.SHAKE -> stringResource(DesignSystemR.string.mission_shake)
    MissionType.BARCODE -> stringResource(DesignSystemR.string.mission_barcode)
    MissionType.RANDOM -> stringResource(DesignSystemR.string.mission_random)
}

@Preview(name = "Mission Stack - Empty Free", showBackground = true)
@Composable
private fun MissionStackEmptyFreePreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            ReMindSettingsGroup(title = "Missions") {
                MissionStackSection(
                    missions = emptyList(),
                    isPro = false,
                    onMissionClick = {},
                    onMissionRemove = {},
                    onAddMissionClick = {},
                    onLockedSlotClick = {}
                )
            }
        }
    }
}

@Preview(name = "Mission Stack - Filled Pro Light", showBackground = true)
@Composable
private fun MissionStackFilledProLightPreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            ReMindSettingsGroup(title = "Missions") {
                MissionStackSection(
                    missions = listOf(
                        Mission(id = 1, alarmId = 1, type = MissionType.MATH, order = 0, repeatCount = 3),
                        Mission(id = 2, alarmId = 1, type = MissionType.TYPING, order = 1, repeatCount = 2)
                    ),
                    isPro = true,
                    onMissionClick = {},
                    onMissionRemove = {},
                    onAddMissionClick = {},
                    onLockedSlotClick = {}
                )
            }
        }
    }
}

@Preview(
    name = "Mission Stack - Filled Pro Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
private fun MissionStackFilledProDarkPreview() {
    ReMindTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            ReMindSettingsGroup(title = "Missions") {
                MissionStackSection(
                    missions = listOf(
                        Mission(id = 1, alarmId = 1, type = MissionType.MATH, order = 0, repeatCount = 3),
                        Mission(id = 2, alarmId = 1, type = MissionType.SHAKE, order = 1, repeatCount = 1),
                        Mission(id = 3, alarmId = 1, type = MissionType.BARCODE, order = 2, repeatCount = 1)
                    ),
                    isPro = true,
                    onMissionClick = {},
                    onMissionRemove = {},
                    onAddMissionClick = {},
                    onLockedSlotClick = {}
                )
            }
        }
    }
}
