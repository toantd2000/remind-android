package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.R

/**
 * Domain-specific illustration types for ReMind application (Sky & Celestial theme).
 */
enum class ReMindIllustrationType {
    NO_ALARM,
    ACTIVE_ALARM,
    NO_REMINDER,
    ACTIVE_REMINDER
}

/**
 * Renders domain-specific M3 vector status illustrations for ReMind
 * (Vầng trăng đội mũ ngủ, Mặt trời mắt lấp lánh & Chuông rung, Đám mây thảnh thơi, Chuông thông báo mây).
 */
@Composable
fun ReMindIllustration(
    type: ReMindIllustrationType,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    contentDescription: String? = null
) {
    val resId = when (type) {
        ReMindIllustrationType.NO_ALARM -> R.drawable.il_state_no_alarm
        ReMindIllustrationType.ACTIVE_ALARM -> R.drawable.il_state_active_alarm
        ReMindIllustrationType.NO_REMINDER -> R.drawable.il_state_no_reminder
        ReMindIllustrationType.ACTIVE_REMINDER -> R.drawable.il_state_active_reminder
    }
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
}
