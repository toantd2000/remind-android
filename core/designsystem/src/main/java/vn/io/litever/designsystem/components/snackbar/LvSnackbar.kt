package vn.io.litever.designsystem.components.snackbar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

/**
 * Visuals for [LvSnackbar], allowing you to pass [LvSemantic] alongside standard Material 3 Snackbar arguments.
 */
class LvSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
    val semantic: LvSemantic = LvSemantic.Text
) : SnackbarVisuals

/**
 * A wrapper around Material 3 [Snackbar] that reads [LvSnackbarVisuals] to apply
 * LiteVer [LvSemantic] colors and standard 6.dp shape.
 */
@Composable
fun LvSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    shape: Shape = LiteverTheme.shapes.medium
) {
    val visuals = snackbarData.visuals as? LvSnackbarVisuals
    val semantic = visuals?.semantic ?: LvSemantic.Text

    val containerColor: Color = when (semantic) {
        LvSemantic.Primary -> MaterialTheme.colorScheme.primaryContainer
        LvSemantic.Secondary -> MaterialTheme.colorScheme.secondaryContainer
        LvSemantic.Tertiary -> MaterialTheme.colorScheme.tertiaryContainer
        LvSemantic.Text -> MaterialTheme.colorScheme.onSurface
        LvSemantic.Success -> LiteverTheme.colors.successContainer
        LvSemantic.Destructive -> MaterialTheme.colorScheme.errorContainer
        LvSemantic.Warning -> LiteverTheme.colors.warningContainer
    }

    val contentColor: Color = when (semantic) {
        LvSemantic.Primary -> MaterialTheme.colorScheme.onPrimaryContainer
        LvSemantic.Secondary -> MaterialTheme.colorScheme.onSecondaryContainer
        LvSemantic.Tertiary -> MaterialTheme.colorScheme.onTertiaryContainer
        LvSemantic.Text -> MaterialTheme.colorScheme.surface
        LvSemantic.Success -> LiteverTheme.colors.onSuccessContainer
        LvSemantic.Destructive -> MaterialTheme.colorScheme.onErrorContainer
        LvSemantic.Warning -> LiteverTheme.colors.onWarningContainer
    }

    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionColor = contentColor,
        actionContentColor = contentColor,
        dismissActionContentColor = contentColor
    )
}

/**
 * Standard SnackbarHost configured to use [LvSnackbar] by default.
 */
@Composable
fun LvSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            LvSnackbar(snackbarData = snackbarData)
        }
    )
}

// Previews
@Preview(showBackground = true, name = "LvSnackbar - Light")
@Composable
private fun LvSnackbarPreviewLight() {
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        snackbarHostState.showSnackbar(
            LvSnackbarVisuals(
                message = "This is a success message!",
                actionLabel = "Undo",
                semantic = LvSemantic.Success
            )
        )
    }

    ReMindTheme(darkTheme = false) {
        LvSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "LvSnackbar - Dark")
@Composable
private fun LvSnackbarPreviewDark() {
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        snackbarHostState.showSnackbar(
            LvSnackbarVisuals(
                message = "An error has occurred.",
                actionLabel = "Retry",
                semantic = LvSemantic.Destructive,
                withDismissAction = true
            )
        )
    }

    ReMindTheme(darkTheme = true) {
        LvSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }
}
