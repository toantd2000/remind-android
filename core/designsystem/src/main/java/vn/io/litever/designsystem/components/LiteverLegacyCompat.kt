package vn.io.litever.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.button.LvIconButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.components.snackbar.LvSnackbarHost
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons

@Composable
fun LiteverScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteverTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon ?: {
            if (onBackClick != null) {
                LvIconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = ReMindIcons.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteverTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    LvTopAppBar(
        title = title,
        modifier = modifier,
        onBackClick = onBackClick,
        actions = actions
    )
}

@Composable
fun LiteverButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit
) {
    LvButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

@Composable
fun LiteverOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    LvButton(
        onClick = onClick,
        modifier = modifier,
        type = LvButtonType.Outlined,
        semantic = LvSemantic.Secondary,
        enabled = enabled,
        content = content
    )
}

@Composable
fun LiteverFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        content = content
    )
}

@Composable
fun LiteverIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    LvIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

@Composable
fun LiteverSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    LvSnackbarHost(
        hostState = hostState,
        modifier = modifier
    )
}

@Composable
fun LiteverCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        border = border,
        content = content
    )
}

@Composable
fun LiteverOutlinedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = modifier,
        content = content
    )
}

@Composable
fun LiteverSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
fun LiteverListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    ListItem(
        headlineContent = headlineContent,
        modifier = modifier,
        leadingContent = leadingContent,
        supportingContent = supportingContent,
        trailingContent = trailingContent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteverModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.BottomSheetDefaults.ContainerColor,
    tonalElevation: androidx.compose.ui.unit.Dp = androidx.compose.material3.BottomSheetDefaults.Elevation,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteverDatePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        content = content
    )
}

@Composable
fun LiteverTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    label: String? = null,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        shape = LiteverTheme.shapes.medium,
        modifier = modifier,
        placeholder = placeholder?.let { { Text(it) } },
        label = label?.let { { Text(it) } },
        isError = isError,
        supportingText = supportingText,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

@Composable
fun LiteverCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = ProgressIndicatorDefaults.circularColor,
    strokeWidth: androidx.compose.ui.unit.Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: androidx.compose.ui.graphics.Color = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
    strokeCap: androidx.compose.ui.graphics.StrokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap
) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        trackColor = trackColor,
        strokeCap = strokeCap
    )
}

@Composable
fun LiteverLinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = ProgressIndicatorDefaults.linearColor,
    trackColor: androidx.compose.ui.graphics.Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: androidx.compose.ui.graphics.StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap
) {
    androidx.compose.material3.LinearProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        strokeCap = strokeCap
    )
}

@Composable
fun LiteverLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = ProgressIndicatorDefaults.linearColor,
    trackColor: androidx.compose.ui.graphics.Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: androidx.compose.ui.graphics.StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap
) {
    androidx.compose.material3.LinearProgressIndicator(
        progress = { progress },
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        strokeCap = strokeCap
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteverTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    TimePickerDialog(
        title = title ?: { Text("") },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            if (confirmButton != null) {
                confirmButton()
            } else {
                TextButton(onClick = { onConfirm?.invoke() }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        },
        dismissButton = {
            if (dismissButton != null) {
                dismissButton()
            } else {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        },
        content = content
    )
}

@Composable
fun LiteverRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteverSingleChoiceSegmentedButtonRow(
    modifier: Modifier = Modifier,
    space: androidx.compose.ui.unit.Dp = androidx.compose.material3.SegmentedButtonDefaults.BorderWidth,
    content: @Composable androidx.compose.material3.SingleChoiceSegmentedButtonRowScope.() -> Unit
) {
    androidx.compose.material3.SingleChoiceSegmentedButtonRow(
        modifier = modifier,
        space = space,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun androidx.compose.material3.SingleChoiceSegmentedButtonRowScope.LiteverSegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape = androidx.compose.material3.SegmentedButtonDefaults.baseShape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: androidx.compose.material3.SegmentedButtonColors = androidx.compose.material3.SegmentedButtonDefaults.colors(),
    border: BorderStroke = androidx.compose.material3.SegmentedButtonDefaults.borderStroke(androidx.compose.material3.MaterialTheme.colorScheme.outline),
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource? = null,
    icon: @Composable () -> Unit = { androidx.compose.material3.SegmentedButtonDefaults.Icon(selected) },
    label: @Composable () -> Unit
) {
    SegmentedButton(
        selected = selected,
        onClick = onClick,
        shape = shape,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        border = border,
        interactionSource = interactionSource,
        icon = icon,
        label = label
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteverFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = androidx.compose.material3.FilterChipDefaults.shape,
    colors: androidx.compose.material3.SelectableChipColors = androidx.compose.material3.FilterChipDefaults.filterChipColors(),
    elevation: androidx.compose.material3.SelectableChipElevation? = androidx.compose.material3.FilterChipDefaults.filterChipElevation(),
    border: BorderStroke? = null,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource? = null
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        interactionSource = interactionSource
    )
}

@Composable
fun LiteverDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = androidx.compose.material3.AlertDialogDefaults.shape,
    containerColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.AlertDialogDefaults.containerColor,
    tonalElevation: androidx.compose.ui.unit.Dp = androidx.compose.material3.AlertDialogDefaults.TonalElevation
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation
    )
}


