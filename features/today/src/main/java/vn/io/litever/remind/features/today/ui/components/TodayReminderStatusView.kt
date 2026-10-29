package vn.io.litever.remind.features.today.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.components.FeedbackStateType
import vn.io.litever.designsystem.components.illustration.LvIllustration
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R as DesignSystemR
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.today.R

/**
 * Hiển thị trạng thái số lượng nhắc nhở đang hoạt động trên TodayScreen.
 * Vùng này CHỈ để hiển thị trạng thái, KHÔNG THỂ tương tác (non-interactive),
 * đồng bộ thiết kế với [TodayNextAlarmView].
 */
@Composable
fun TodayReminderStatusView(
    activeReminderCount: Int,
    modifier: Modifier = Modifier
) {
    if (activeReminderCount > 0) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = LiteverTheme.shapes.large,
            color = LiteverTheme.colors.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(all = LiteverTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.tiny)
            ) {
                // Hàng 1: Tiêu đề Nhắc nhở + Badge số lượng đang bật
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.reminder_status_title),
                        style = LiteverTheme.typography.labelSmall,
                        color = LiteverTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = LiteverTheme.spacing.small)
                    )

                    Surface(
                        shape = LiteverTheme.shapes.small,
                        color = LiteverTheme.colors.tertiaryContainer,
                        contentColor = LiteverTheme.colors.onTertiaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.active_reminders_badge, activeReminderCount),
                            style = LiteverTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Hàng 2: Icon ô vuông + Thông tin số lượng nhắc nhở đang hoạt động
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    Card(
                        modifier = Modifier.size(36.dp),
                        shape = LiteverTheme.shapes.large
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(LiteverTheme.colors.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ReMindIcons.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = LiteverTheme.colors.onSurface
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(2.dp))
                        val statusText = if (activeReminderCount == 1) {
                            stringResource(R.string.active_reminder_single_status_text)
                        } else {
                            stringResource(R.string.active_reminders_status_text, activeReminderCount)
                        }
                        Text(
                            text = statusText,
                            style = LiteverTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = LiteverTheme.colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Hàng 3: Mô tả trạng thái
                Text(
                    text = stringResource(R.string.active_reminders_status_desc),
                    color = LiteverTheme.colors.onSurfaceVariant,
                    style = LiteverTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = LiteverTheme.colors.surfaceContainerLow,
            shape = LiteverTheme.shapes.large,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LiteverTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
            ) {
                LvIllustration(
                    type = FeedbackStateType.EMPTY,
                    maxWidth = 48.dp,
                    modifier = Modifier.size(48.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.no_active_reminders_title),
                        style = LiteverTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = LiteverTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.no_active_reminders_desc),
                        style = LiteverTheme.typography.bodySmall,
                        color = LiteverTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodayReminderStatusViewActivePreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TodayReminderStatusView(activeReminderCount = 3)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodayReminderStatusViewEmptyPreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TodayReminderStatusView(activeReminderCount = 0)
        }
    }
}
