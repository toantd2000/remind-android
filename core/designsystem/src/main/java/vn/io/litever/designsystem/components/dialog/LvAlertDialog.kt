package vn.io.litever.designsystem.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

/**
 * Standard opinionated AlertDialog for LiteVer applications.
 *
 * Uses LiteVer's moderate squircle shape (10.dp / extraLarge) instead of M3's large 28.dp corners,
 * with built-in styling for confirm and dismiss actions.
 *
 * @param onDismissRequest Called when the user tries to dismiss the dialog.
 * @param confirmButton The confirm button composable (typically an [LvButton]).
 * @param modifier The modifier to be applied to this dialog.
 * @param dismissButton The dismiss button composable (optional).
 * @param icon The icon to be displayed inside the dialog (optional).
 * @param title The title of the dialog.
 * @param text The message content of the dialog.
 * @param shape Corner shape defaulting to [LiteverTheme.shapes.extraLarge] (10.dp).
 * @param containerColor Background container color.
 * @param properties Window dialog properties.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LvAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = LiteverTheme.shapes.extraLarge,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    properties: DialogProperties = DialogProperties()
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = properties
    ) {
        LvAlertDialogContent(
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            icon = icon,
            title = title,
            text = text,
            shape = shape,
            containerColor = containerColor
        )
    }
}

/**
 * The inner visual content of [LvAlertDialog].
 *
 * This composable renders the dialog surface, title, text, and action buttons directly
 * without spawning a separate Android popup window, making it safe to render in Android Studio `@Preview`
 * or embed within custom containers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LvAlertDialogContent(
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = LiteverTheme.shapes.extraLarge,
    containerColor: Color = AlertDialogDefaults.containerColor
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation
    ) {
        Column(
            modifier = Modifier.padding(LiteverTheme.spacing.large)
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .padding(bottom = LiteverTheme.spacing.medium)
                        .align(Alignment.CenterHorizontally)
                ) {
                    icon()
                }
            }
            if (title != null) {
                Box(
                    modifier = Modifier.padding(bottom = LiteverTheme.spacing.smallMedium)
                ) {
                    ProvideTextStyle(
                        value = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) {
                        title()
                    }
                }
            }
            if (text != null) {
                Box(
                    modifier = Modifier.padding(bottom = LiteverTheme.spacing.medium)
                ) {
                    ProvideTextStyle(
                        value = MaterialTheme.typography.bodyMedium
                    ) {
                        text()
                    }
                }
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    LiteverTheme.spacing.small,
                    Alignment.End
                ),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
            ) {
                dismissButton?.invoke()
                confirmButton()
            }
        }
    }
}

@Preview(showBackground = true, name = "LvAlertDialog - Light")
@Composable
fun LvAlertDialogLightPreview() {
    ReMindTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(LiteverTheme.spacing.medium)) {
            LvAlertDialogContent(
                title = { Text("Delete Confirmation") },
                text = { Text("Are you sure you want to permanently delete this transaction? This action cannot be undone.") },
                confirmButton = {
                    LvButton(
                        onClick = {},
                        semantic = LvSemantic.Destructive
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    LvButton(
                        onClick = {},
                        type = LvButtonType.Text,
                        semantic = LvSemantic.Secondary
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "LvAlertDialog - Dark")
@Composable
fun LvAlertDialogDarkPreview() {
    ReMindTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(LiteverTheme.spacing.medium)) {
            LvAlertDialogContent(
                title = { Text("Goal Completed") },
                text = { Text("You have successfully reached your monthly target.") },
                confirmButton = {
                    LvButton(
                        onClick = {},
                        semantic = LvSemantic.Success
                    ) {
                        Text("Awesome")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "LvAlertDialog - Long Buttons Wrapping")
@Composable
fun LvAlertDialogLongButtonsPreview() {
    ReMindTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(LiteverTheme.spacing.medium)) {
            LvAlertDialogContent(
                title = { Text("Unsaved Changes") },
                text = { Text("Do you want to discard your changes or save them to drafts before leaving?") },
                confirmButton = {
                    LvButton(
                        onClick = {},
                        semantic = LvSemantic.Primary
                    ) {
                        Text("Save Changes and Continue Later")
                    }
                },
                dismissButton = {
                    LvButton(
                        onClick = {},
                        type = LvButtonType.Outlined,
                        semantic = LvSemantic.Secondary
                    ) {
                        Text("Discard Everything")
                    }
                }
            )
        }
    }
}
