package vn.io.litever.remind.features.mission.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.button.LvIconButton
import vn.io.litever.designsystem.components.core.LvSemantic
import androidx.compose.material3.ModalBottomSheet
import vn.io.litever.designsystem.components.dialog.LvAlertDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.components.ReMindGroupCard
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.features.mission.viewmodel.PhraseSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhraseSelectionRoute(
    initialSelectedIds: List<Long>,
    onBackClick: () -> Unit,
    onPhrasesSelected: (List<Long>) -> Unit,
    viewModel: PhraseSelectionViewModel = hiltViewModel()
) {
    val predefinedPhrases by viewModel.predefinedPhrases
    val customPhrases by viewModel.customPhrases.collectAsState()

    var selectedIds by remember { mutableStateOf(initialSelectedIds.toSet()) }
    var showAddSheet by remember { mutableStateOf(false) }
    var phraseToEdit by remember { mutableStateOf<Phrase?>(null) }
    var phraseToDelete by remember { mutableStateOf<Phrase?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    PhraseSelectionScreen(
        predefinedPhrases = predefinedPhrases,
        customPhrases = customPhrases,
        selectedIds = selectedIds,
        onBackClick = onBackClick,
        onTogglePhrase = { id ->
            selectedIds = if (selectedIds.contains(id)) {
                selectedIds - id
            } else {
                selectedIds + id
            }
        },
        onSelectAll = { ids -> selectedIds = selectedIds + ids },
        onDeselectAll = { ids -> selectedIds = selectedIds - ids.toSet() },
        onComplete = { onPhrasesSelected(selectedIds.toList()) },
        onAddCustomPhraseClick = {
            phraseToEdit = null
            showAddSheet = true
        },
        onEditCustomPhraseClick = { phrase ->
            phraseToEdit = phrase
            showAddSheet = true
        },
        onDeleteCustomPhraseClick = { phrase -> phraseToDelete = phrase }
    )

    if (phraseToDelete != null) {
        LvAlertDialog(
            onDismissRequest = { phraseToDelete = null },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.mission_phrase_delete_confirm)) },
            confirmButton = {
                LvButton(
                    onClick = {
                        viewModel.deletePhrase(phraseToDelete!!)
                        phraseToDelete = null
                    },
                    semantic = LvSemantic.Destructive
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                LvButton(
                    onClick = { phraseToDelete = null },
                    type = LvButtonType.Outlined,
                    semantic = LvSemantic.Secondary
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
                phraseToEdit = null
            },
            sheetState = sheetState,
            containerColor = LiteverTheme.colors.surface
        ) {
            ReMindBottomSheetContent(
                title = stringResource(if (phraseToEdit != null) R.string.mission_phrase_edit_title else R.string.mission_add_custom_phrase)
            ) {
                AddCustomPhraseContent(
                    editingPhrase = phraseToEdit,
                    canBePrivate = viewModel.alarmId != 0L,
                    onDismiss = {
                        showAddSheet = false
                        phraseToEdit = null
                    },
                    onConfirm = { content, isShared ->
                        viewModel.saveCustomPhrase(phraseToEdit?.id ?: 0, content, isShared)
                        showAddSheet = false
                        phraseToEdit = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhraseSelectionScreen(
    predefinedPhrases: Map<String, List<Phrase>>,
    customPhrases: List<Phrase>,
    selectedIds: Set<Long>,
    onBackClick: () -> Unit,
    onTogglePhrase: (Long) -> Unit,
    onSelectAll: (List<Long>) -> Unit,
    onDeselectAll: (List<Long>) -> Unit,
    onComplete: () -> Unit,
    onAddCustomPhraseClick: () -> Unit,
    onEditCustomPhraseClick: (Phrase) -> Unit,
    onDeleteCustomPhraseClick: (Phrase) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("motivation", "basic", "custom")
    val tabTitles = listOf(
        stringResource(R.string.mission_phrases_motivation),
        stringResource(R.string.mission_phrases_basic),
        stringResource(R.string.mission_phrases_my)
    )

    Scaffold(
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.mission_select_phrases),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                LvButton(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedIds.isNotEmpty(),
                    semantic = LvSemantic.Primary
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTabIndex == 2) {
                FloatingActionButton(
                    onClick = onAddCustomPhraseClick,
                    containerColor = LiteverTheme.colors.primary,
                    contentColor = LiteverTheme.colors.onPrimary,
                    shape = LiteverTheme.shapes.medium
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = LiteverTheme.colors.background,
                contentColor = LiteverTheme.colors.primary,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
                        color = LiteverTheme.colors.primary
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        },
                    )
                }
            }

            val currentCategory = categories[selectedTabIndex]
            val phrases =
                if (currentCategory == "custom") customPhrases else predefinedPhrases[currentCategory]
                    ?: emptyList()

            Box(modifier = Modifier.fillMaxSize()) {
                if (phrases.isEmpty() && currentCategory == "custom") {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.mission_phrase_empty),
                            style = LiteverTheme.typography.bodyLarge,
                            color = LiteverTheme.colors.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = LiteverTheme.spacing.medium)
                    ) {
                        if (currentCategory == "custom") {
                            val sharedPhrases =
                                phrases.filter { it.source == vn.io.litever.remind.core.model.PhraseSource.USER_SHARED }
                            val privatePhrases =
                                phrases.filter { it.source == vn.io.litever.remind.core.model.PhraseSource.USER_PRIVATE }

                            if (sharedPhrases.isNotEmpty()) {
                                item {
                                    SectionHeader(stringResource(R.string.mission_shared))
                                }
                                items(sharedPhrases) { phrase ->
                                    PhraseItem(
                                        phrase = phrase,
                                        isSelected = selectedIds.contains(phrase.id),
                                        onToggle = { onTogglePhrase(phrase.id) },
                                        onEdit = { onEditCustomPhraseClick(phrase) },
                                        onDelete = { onDeleteCustomPhraseClick(phrase) }
                                    )
                                }
                            }

                            if (privatePhrases.isNotEmpty()) {
                                item {
                                    SectionHeader(stringResource(R.string.mission_private))
                                }
                                items(privatePhrases) { phrase ->
                                    PhraseItem(
                                        phrase = phrase,
                                        isSelected = selectedIds.contains(phrase.id),
                                        onToggle = { onTogglePhrase(phrase.id) },
                                        onEdit = { onEditCustomPhraseClick(phrase) },
                                        onDelete = { onDeleteCustomPhraseClick(phrase) }
                                    )
                                }
                            }
                        } else {
                            item {
                                val allSelected =
                                    phrases.isNotEmpty() && phrases.all { selectedIds.contains(it.id) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = LiteverTheme.spacing.medium,
                                            vertical = LiteverTheme.spacing.small
                                        ),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    LvButton(
                                        onClick = {
                                            if (allSelected) onDeselectAll(phrases.map { it.id })
                                            else onSelectAll(phrases.map { it.id })
                                        },
                                        type = LvButtonType.Text,
                                        semantic = LvSemantic.Primary
                                    ) {
                                        Text(
                                            text = stringResource(if (allSelected) R.string.action_deselect_all else R.string.action_select_all),
                                            style = LiteverTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                            items(phrases) { phrase ->
                                PhraseItem(
                                    phrase = phrase,
                                    isSelected = selectedIds.contains(phrase.id),
                                    onToggle = { onTogglePhrase(phrase.id) },
                                    onEdit = null,
                                    onDelete = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = LiteverTheme.typography.titleSmall,
        color = LiteverTheme.colors.primary,
        modifier = Modifier.padding(start = LiteverTheme.spacing.medium, top = LiteverTheme.spacing.medium, bottom = LiteverTheme.spacing.small)
    )
}

@Composable
fun PhraseItem(
    phrase: Phrase,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = LiteverTheme.spacing.medium,
                vertical = LiteverTheme.spacing.extraSmall
            )
            .clip(LiteverTheme.shapes.medium)
            .clickable { onToggle() },
        shape = LiteverTheme.shapes.medium,
        color = if (isSelected) LiteverTheme.colors.primaryContainer.copy(alpha = 0.3f)
        else LiteverTheme.colors.surface,
        border = BorderStroke(
            1.dp,
            if (isSelected) LiteverTheme.colors.primary.copy(alpha = 0.5f)
            else LiteverTheme.colors.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(LiteverTheme.spacing.medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = LiteverTheme.colors.primary,
                    uncheckedColor = LiteverTheme.colors.outline
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = LiteverTheme.spacing.smallMedium)
            ) {
                Text(
                    text = phrase.content,
                    style = LiteverTheme.typography.bodyLarge,
                    color = LiteverTheme.colors.onSurface
                )
                if (phrase.isCustom) {
                    Text(
                        text = stringResource(if (phrase.isShared) R.string.mission_shared else R.string.mission_private),
                        style = LiteverTheme.typography.labelSmall,
                        color = LiteverTheme.colors.onSurfaceVariant
                    )
                }
            }

            if (phrase.isCustom) {
                Box {
                    LvIconButton(
                        onClick = { showMenu = true },
                        type = LvButtonType.Text,
                        semantic = LvSemantic.Neutral
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More options",
                            tint = LiteverTheme.colors.onSurfaceVariant
                        )
                    }
                    val context = LocalContext.current

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (onEdit != null) {
                            CompositionLocalProvider(LocalContext provides context) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_edit)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.Edit,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onEdit()
                                    }
                                )
                            }
                        }
                        if (onDelete != null) {
                            CompositionLocalProvider(LocalContext provides context) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.action_delete),
                                            color = LiteverTheme.colors.error
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = null,
                                            tint = LiteverTheme.colors.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun AddCustomPhraseContent(
    editingPhrase: Phrase?,
    canBePrivate: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var text by remember(editingPhrase) { mutableStateOf(editingPhrase?.content ?: "") }
    var isShared by remember(editingPhrase) { mutableStateOf(editingPhrase?.isShared ?: true) }

    LaunchedEffect(canBePrivate) {
        if (!canBePrivate) isShared = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LiteverTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
    ) {
        ReMindGroupCard {
            Column(
                modifier = Modifier.padding(LiteverTheme.spacing.medium)
            ) {
                Text(
                    text = stringResource(R.string.mission_phrase_placeholder),
                    style = LiteverTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LiteverTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { if (it.length <= 128) text = it },
                        modifier = Modifier.weight(1f),
                        textStyle = LiteverTheme.typography.bodyLarge.copy(
                            color = LiteverTheme.colors.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = false,
                        maxLines = 5,
                        cursorBrush = SolidColor(LiteverTheme.colors.primary),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.TopStart) {
                                if (text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.mission_phrase_placeholder),
                                        style = LiteverTheme.typography.bodyLarge.copy(
                                            color = LiteverTheme.colors.outlineVariant
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (text.isNotEmpty()) {
                        LvIconButton(
                            onClick = { text = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear text",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.EditNote,
                            contentDescription = null,
                            tint = LiteverTheme.colors.outlineVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${text.length}/128",
                    modifier = Modifier.fillMaxWidth(),
                    style = LiteverTheme.typography.bodySmall,
                    color = LiteverTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LiteverTheme.spacing.medium)
                .clip(LiteverTheme.shapes.large)
                .background(color = LiteverTheme.colors.tertiaryContainer)
                .padding(LiteverTheme.spacing.smallMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null,
                tint = LiteverTheme.colors.onTertiaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(LiteverTheme.spacing.smallMedium))
            Text(
                text = stringResource(R.string.mission_typing_tip),
                style = LiteverTheme.typography.labelMedium,
                color = LiteverTheme.colors.onTertiaryContainer
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LiteverTheme.spacing.medium)
                .clip(LiteverTheme.shapes.large)
                .background(color = LiteverTheme.colors.neutralContainer)
                .padding(vertical = LiteverTheme.spacing.small)
        ) {
            Checkbox(
                checked = isShared,
                onCheckedChange = { isShared = it },
                enabled = editingPhrase == null && canBePrivate,
                colors = CheckboxDefaults.colors(
                    checkedColor = LiteverTheme.colors.neutral,
                    uncheckedColor = LiteverTheme.colors.outline
                )
            )
            Column {
                Text(
                    text = stringResource(R.string.mission_shared),
                    style = LiteverTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(if (canBePrivate) R.string.mission_shared_desc else R.string.mission_private_disabled_desc),
                    style = LiteverTheme.typography.bodySmall,
                    color = if (canBePrivate) LiteverTheme.colors.onSurfaceVariant else LiteverTheme.colors.warning
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LiteverTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.smallMedium)
        ) {
            LvButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                type = LvButtonType.Outlined,
                semantic = LvSemantic.Secondary
            ) {
                Text(stringResource(R.string.cancel))
            }

            LvButton(
                onClick = { onConfirm(text, isShared) },
                enabled = text.isNotBlank(),
                modifier = Modifier.weight(1f),
                semantic = LvSemantic.Primary
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhraseSelectionScreenPreview() {
    ReMindTheme {
        PhraseSelectionScreen(
            predefinedPhrases = emptyMap(),
            customPhrases = emptyList(),
            selectedIds = emptySet(),
            onBackClick = {},
            onTogglePhrase = {},
            onSelectAll = {},
            onDeselectAll = {},
            onComplete = {},
            onAddCustomPhraseClick = {},
            onEditCustomPhraseClick = {},
            onDeleteCustomPhraseClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddCustomPhraseContentPreview() {
    ReMindTheme {
        AddCustomPhraseContent(
            editingPhrase = null,
            canBePrivate = true,
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditCustomPhraseContentPreview() {
    ReMindTheme {
        AddCustomPhraseContent(
            editingPhrase = Phrase(
                id = 1,
                content = "Stay positive, work hard, make it happen.",
                categoryId = "custom",
                isCustom = true,
                isShared = true
            ),
            canBePrivate = true,
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddCustomPhraseContentPrivateDisabledPreview() {
    ReMindTheme {
        AddCustomPhraseContent(
            editingPhrase = null,
            canBePrivate = false,
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}










