package vn.io.litever.remind.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vn.io.litever.designsystem.components.LiteverAlertDialog

@Composable
fun ReMindAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String? = null,
    dismissButtonText: String? = null,
    onDismissClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
) {
    LiteverAlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButtonText = confirmButtonText,
        onConfirmClick = onConfirmClick,
        modifier = modifier,
        title = title,
        text = text,
        dismissButtonText = dismissButtonText,
        onDismissClick = onDismissClick,
        content = content
    )
}
