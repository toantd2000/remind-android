package vn.io.litever.designsystem.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme

/**
 * Standard branded defaults for Material 3 buttons in Litever applications.
 *
 * Provides branded shapes, accessible color palettes, content paddings, and elevations
 * for standard [androidx.compose.material3.Button], [androidx.compose.material3.OutlinedButton],
 * and [androidx.compose.material3.TextButton].
 */
object LiteVerButtonDefaults {

    /**
     * Standard branded shape for Litever buttons.
     * Overrides Material 3's default pill shape with [LiteverTheme.shapes.medium].
     */
    val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = LiteverTheme.shapes.medium

    /**
     * Standard content padding for filled and outlined buttons.
     */
    val ContentPadding: PaddingValues = PaddingValues(
        horizontal = 24.dp,
        vertical = 10.dp
    )

    /**
     * Compact content padding for smaller buttons.
     */
    val SmallContentPadding: PaddingValues = PaddingValues(
        horizontal = 16.dp,
        vertical = 6.dp
    )

    /**
     * Content padding for text buttons.
     */
    val TextButtonContentPadding: PaddingValues = PaddingValues(
        horizontal = 12.dp,
        vertical = 8.dp
    )

    val MinHeight: Dp = 40.dp
    val MinWidth: Dp = 64.dp
    val IconSize: Dp = 18.dp
    val IconSpacing: Dp = 8.dp

    /**
     * Primary button colors using the active theme's primary palette.
     */
    @Composable
    fun primaryColors(
        containerColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    /**
     * Secondary button colors using the active theme's secondary palette.
     */
    @Composable
    fun secondaryColors(
        containerColor: Color = MaterialTheme.colorScheme.secondary,
        contentColor: Color = MaterialTheme.colorScheme.onSecondary,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    /**
     * Tonal button colors using the active theme's secondary container palette.
     */
    @Composable
    fun tonalColors(
        containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.filledTonalButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    /**
     * Destructive / Error button colors for dangerous operations.
     */
    @Composable
    fun destructiveColors(
        containerColor: Color = MaterialTheme.colorScheme.error,
        contentColor: Color = MaterialTheme.colorScheme.onError,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    /**
     * Success button colors for positive confirmations and completed flows.
     */
    @Composable
    fun successColors(
        containerColor: Color = LiteverTheme.colors.success,
        contentColor: Color = LiteverTheme.colors.onSuccess,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )


    /**
     * Outlined button colors.
     */
    @Composable
    fun outlinedColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.primary,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.outlinedButtonColors(
        containerColor = containerColor,
        contentColor = contentColor, 
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    /**
     * Text button colors.
     */
    @Composable
    fun textColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.primary,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    /**
     * Standard border stroke for [androidx.compose.material3.OutlinedButton].
     */
    @Composable
    fun outlinedBorder(
        enabled: Boolean = true,
        color: Color = MaterialTheme.colorScheme.outline,
        disabledColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        width: Dp = 1.dp
    ): BorderStroke = BorderStroke(
        width = width,
        color = if (enabled) color else disabledColor
    )

    /**
     * Button elevation configuration.
     */
    @Composable
    fun elevation(
        defaultElevation: Dp = 1.dp,
        pressedElevation: Dp = 2.dp,
        focusedElevation: Dp = 1.dp,
        hoveredElevation: Dp = 2.dp,
        disabledElevation: Dp = 0.dp
    ): ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        disabledElevation = disabledElevation
    )

    /**
     * Flat elevation with 0.dp across all interaction states.
     */
    /**
     * Resolves the appropriate [ButtonColors] based on visual [LvButtonType] and [LvSemantic] intent.
     */
    @Composable
    fun resolveColors(type: LvButtonType, semantic: LvSemantic): ButtonColors {
        return when (type) {
            LvButtonType.Filled -> when (semantic) {
                LvSemantic.Primary -> primaryColors()
                LvSemantic.Secondary -> secondaryColors()
                LvSemantic.Tertiary -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
                LvSemantic.Text -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
                LvSemantic.Success -> successColors()
                LvSemantic.Destructive -> destructiveColors()
                LvSemantic.Warning -> ButtonDefaults.buttonColors(
                    containerColor = LiteverTheme.colors.warning,
                    contentColor = LiteverTheme.colors.onWarning
                )
            }
            LvButtonType.Tonal -> when (semantic) {
                LvSemantic.Primary -> tonalColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                LvSemantic.Secondary -> tonalColors()
                LvSemantic.Tertiary -> tonalColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                LvSemantic.Text -> tonalColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LvSemantic.Success -> tonalColors(
                    containerColor = LiteverTheme.colors.successContainer,
                    contentColor = LiteverTheme.colors.onSuccessContainer
                )
                LvSemantic.Destructive -> tonalColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                LvSemantic.Warning -> tonalColors(
                    containerColor = LiteverTheme.colors.warningContainer,
                    contentColor = LiteverTheme.colors.onWarningContainer
                )
            }
            LvButtonType.Outlined -> when (semantic) {
                LvSemantic.Primary -> outlinedColors()
                LvSemantic.Secondary -> outlinedColors(contentColor = MaterialTheme.colorScheme.secondary)
                LvSemantic.Tertiary -> outlinedColors(contentColor = MaterialTheme.colorScheme.tertiary)
                LvSemantic.Text -> outlinedColors(contentColor = MaterialTheme.colorScheme.onSurface)
                LvSemantic.Success -> outlinedColors(contentColor = LiteverTheme.colors.success)
                LvSemantic.Destructive -> outlinedColors(contentColor = MaterialTheme.colorScheme.error)
                LvSemantic.Warning -> outlinedColors(contentColor = LiteverTheme.colors.warning)
            }
            LvButtonType.Text -> when (semantic) {
                LvSemantic.Primary -> textColors()
                LvSemantic.Secondary -> textColors(contentColor = MaterialTheme.colorScheme.secondary)
                LvSemantic.Tertiary -> textColors(contentColor = MaterialTheme.colorScheme.tertiary)
                LvSemantic.Text -> textColors(contentColor = MaterialTheme.colorScheme.onSurface)
                LvSemantic.Success -> textColors(contentColor = LiteverTheme.colors.success)
                LvSemantic.Destructive -> textColors(contentColor = MaterialTheme.colorScheme.error)
                LvSemantic.Warning -> textColors(contentColor = LiteverTheme.colors.warning)
            }
        }
    }

    /**
     * Resolves border stroke when [type] is [LvButtonType.Outlined], otherwise returns null.
     * Uses Material 3 standard [MaterialTheme.colorScheme.outline] for default/primary/secondary/neutral
     * and specific semantic colors for Tertiary, Success, Destructive, and Warning states.
     */
    @Composable
    fun resolveBorder(type: LvButtonType, semantic: LvSemantic, enabled: Boolean = true): BorderStroke? {
        if (type != LvButtonType.Outlined) return null
        val color = when (semantic) {
            LvSemantic.Primary, LvSemantic.Secondary, LvSemantic.Tertiary -> MaterialTheme.colorScheme.outline
            LvSemantic.Text -> MaterialTheme.colorScheme.outlineVariant
            LvSemantic.Success -> LiteverTheme.colors.successContainer.copy(alpha = 0.38f)
            LvSemantic.Destructive -> MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
            LvSemantic.Warning -> LiteverTheme.colors.warning.copy(alpha = 0.38f)
        }
        return outlinedBorder(enabled = enabled, color = color)
    }

    /**
     * Resolves the appropriate [IconButtonColors] based on visual [LvButtonType] and [LvSemantic] intent.
     */
    @Composable
    fun resolveIconButtonColors(type: LvButtonType, semantic: LvSemantic): IconButtonColors {
        return when (type) {
            LvButtonType.Filled -> when (semantic) {
                LvSemantic.Primary -> IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                LvSemantic.Secondary -> IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
                LvSemantic.Tertiary -> IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
                LvSemantic.Text -> IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
                LvSemantic.Success -> IconButtonDefaults.filledIconButtonColors(
                    containerColor = LiteverTheme.colors.success,
                    contentColor = LiteverTheme.colors.onSuccess
                )
                LvSemantic.Destructive -> IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
                LvSemantic.Warning -> IconButtonDefaults.filledIconButtonColors(
                    containerColor = LiteverTheme.colors.warning,
                    contentColor = LiteverTheme.colors.onWarning
                )
            }
            LvButtonType.Tonal -> when (semantic) {
                LvSemantic.Primary -> IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                LvSemantic.Secondary -> IconButtonDefaults.filledTonalIconButtonColors()
                LvSemantic.Tertiary -> IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                LvSemantic.Text -> IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LvSemantic.Success -> IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = LiteverTheme.colors.successContainer,
                    contentColor = LiteverTheme.colors.onSuccessContainer
                )
                LvSemantic.Destructive -> IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                LvSemantic.Warning -> IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = LiteverTheme.colors.warningContainer,
                    contentColor = LiteverTheme.colors.onWarningContainer
                )
            }
            LvButtonType.Outlined -> when (semantic) {
                LvSemantic.Primary -> IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
                LvSemantic.Secondary -> IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
                LvSemantic.Tertiary -> IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
                LvSemantic.Text -> IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
                LvSemantic.Success -> IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = LiteverTheme.colors.success
                )
                LvSemantic.Destructive -> IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
                LvSemantic.Warning -> IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = LiteverTheme.colors.warning
                )
            }
            LvButtonType.Text -> when (semantic) {
                LvSemantic.Primary -> IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
                LvSemantic.Secondary -> IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
                LvSemantic.Tertiary -> IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
                LvSemantic.Text -> IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
                LvSemantic.Success -> IconButtonDefaults.iconButtonColors(
                    contentColor = LiteverTheme.colors.success
                )
                LvSemantic.Destructive -> IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
                LvSemantic.Warning -> IconButtonDefaults.iconButtonColors(
                    contentColor = LiteverTheme.colors.warning
                )
            }
        }
    }
}

/**
 * Type alias to support canonical camelCase spelling.
 */
typealias LiteverButtonDefaults = LiteVerButtonDefaults
