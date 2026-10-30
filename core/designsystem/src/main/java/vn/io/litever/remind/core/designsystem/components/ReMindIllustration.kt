package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.illustration.IllustrationColors
import vn.io.litever.designsystem.components.illustration.IllustrationDefaults
import vn.io.litever.designsystem.components.illustration.rememberIllustrationPainter

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
 *
 * All illustration colors (stroke, surface, primary, warning, error) are dynamically mapped to
 * the active Material 3 and LiteVer design system tokens. Stroke color automatically matches the text color (`colorScheme.onSurface`).
 */
@Composable
fun ReMindIllustration(
    type: ReMindIllustrationType,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    contentDescription: String? = null,
    colors: IllustrationColors = IllustrationDefaults.colors()
) {
    val paths = ReMindIllustrationPaths.get(type)
    val painter = rememberIllustrationPainter(paths = paths, colors = colors)
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
}
