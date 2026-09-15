package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.model.MissionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionSelectionBottomSheet(
    onDismissRequest: () -> Unit,
    onMissionTypeSelected: (MissionType) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LiteverTheme.colors.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = LiteverTheme.spacing.extraLarge)
        ) {
            Text(
                text = stringResource(R.string.mission_selection_title),
                style = LiteverTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(LiteverTheme.spacing.mediumLarge)
            )

            val missionTypes = listOf(
                MissionItem(
                    type = MissionType.TYPING,
                    title = stringResource(R.string.mission_typing),
                    description = stringResource(R.string.mission_typing_desc),
                    icon = Icons.Rounded.Keyboard,
                    isAvailable = true
                ),
                MissionItem(
                    type = MissionType.MEMORY_FIND_COLOR_TILES,
                    title = stringResource(R.string.mission_memory_tiles),
                    description = stringResource(R.string.mission_memory_tiles_desc),
                    icon = Icons.Rounded.GridView,
                    isAvailable = true
                ),
                MissionItem(
                    type = MissionType.MATH,
                    title = stringResource(R.string.mission_math),
                    description = stringResource(R.string.mission_math_desc),
                    icon = Icons.Rounded.Calculate,
                    isAvailable = false
                ),
                MissionItem(
                    type = MissionType.SHAKE,
                    title = stringResource(R.string.mission_shake),
                    description = stringResource(R.string.mission_shake_desc),
                    icon = Icons.Rounded.Smartphone,
                    isAvailable = false
                ),
                MissionItem(
                    type = MissionType.QR_CODE,
                    title = stringResource(R.string.mission_qr_code),
                    description = stringResource(R.string.mission_qr_code_desc),
                    icon = Icons.Rounded.QrCodeScanner,
                    isAvailable = false
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = LiteverTheme.spacing.extraSmall)
            ) {
                items(missionTypes) { item ->
                    val isAvailable = item.isAvailable

                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isAvailable) 1f else 0.5f)
                            .clickable(enabled = isAvailable) {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    onMissionTypeSelected(item.type)
                                }
                            },
                        headlineContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.title,
                                    style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                if (!isAvailable) {
                                    Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
                                    Surface(
                                        color = LiteverTheme.colors.surfaceVariant,
                                        shape = LiteverTheme.shapes.extraSmall
                                    ) {
                                        Text(
                                            text = stringResource(R.string.coming_soon),
                                            style = LiteverTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            modifier = Modifier.padding(horizontal = LiteverTheme.spacing.extraSmall, vertical = LiteverTheme.spacing.tiny),
                                            color = LiteverTheme.colors.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        },
                        supportingContent = {
                            Text(
                                text = item.description,
                                style = LiteverTheme.typography.bodySmall
                            )
                        },
                        leadingContent = {
                            Surface(
                                shape = LiteverTheme.shapes.medium,
                                color = if (isAvailable)
                                    LiteverTheme.colors.primaryContainer.copy(alpha = 0.5f)
                                else
                                    LiteverTheme.colors.surfaceVariant,
                                border = if (isAvailable)
                                    BorderStroke(1.dp, LiteverTheme.colors.primary.copy(alpha = 0.1f))
                                else null
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.padding(LiteverTheme.spacing.smallMedium).size(LiteverTheme.spacing.large),
                                    tint = if (isAvailable) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

private data class MissionItem(
    val type: MissionType,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isAvailable: Boolean = true
)
