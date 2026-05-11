package vn.io.litever.remind.features.mission.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindButton
import vn.io.litever.remind.core.designsystem.components.ReMindOutlinedButton
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
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
        AlertDialog(
            onDismissRequest = { phraseToDelete = null },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.mission_phrase_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePhrase(phraseToDelete!!)
                    phraseToDelete = null
                }) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { phraseToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val context = LocalContext.current
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showAddSheet = false 
                phraseToEdit = null
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
        ) {
            CompositionLocalProvider(LocalContext provides context) {
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

    ReMindScaffold(
        topBar = {
            ReMindTopAppBar(
                title = stringResource(R.string.mission_select_phrases),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                ReMindButton(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedIds.isNotEmpty()
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTabIndex == 2) {
                FloatingActionButton(
                    onClick = onAddCustomPhraseClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val currentCategory = categories[selectedTabIndex]
            val phrases = if (currentCategory == "custom") customPhrases else predefinedPhrases[currentCategory] ?: emptyList()

            Box(modifier = Modifier.fillMaxSize()) {
                if (phrases.isEmpty() && currentCategory == "custom") {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.mission_phrase_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        if (currentCategory == "custom") {
                            val sharedPhrases = phrases.filter { it.source == vn.io.litever.remind.core.model.PhraseSource.USER_SHARED }
                            val privatePhrases = phrases.filter { it.source == vn.io.litever.remind.core.model.PhraseSource.USER_PRIVATE }
                            
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
                                val allSelected = phrases.isNotEmpty() && phrases.all { selectedIds.contains(it.id) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { 
                                        if (allSelected) onDeselectAll(phrases.map { it.id })
                                        else onSelectAll(phrases.map { it.id })
                                    }) {
                                        Text(
                                            text = stringResource(if (allSelected) R.string.action_deselect_all else R.string.action_select_all),
                                            style = MaterialTheme.typography.labelLarge
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
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
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
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onToggle() },
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = phrase.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (phrase.isCustom) {
                    Text(
                        text = stringResource(if (phrase.isShared) R.string.mission_shared else R.string.mission_private),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (phrase.isCustom) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert, 
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (onEdit != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_edit)) },
                                leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                        }
                        if (onDelete != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
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
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(if (editingPhrase != null) R.string.mission_phrase_edit_title else R.string.mission_add_custom_phrase),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= 128) text = it },
            label = { Text(stringResource(R.string.mission_phrase_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5,
            shape = MaterialTheme.shapes.medium,
            supportingText = {
                Text(
                    text = "${text.length}/128",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ),
            shape = MaterialTheme.shapes.medium,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.mission_typing_tip),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { if (editingPhrase == null && canBePrivate) isShared = !isShared },
            color = Color.Transparent
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = isShared,
                    onCheckedChange = { isShared = it },
                    enabled = editingPhrase == null && canBePrivate
                )
                Column() {
                    Text(
                        text = stringResource(R.string.mission_shared),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = stringResource(if (canBePrivate) R.string.mission_shared_desc else R.string.mission_private_disabled_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (canBePrivate) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReMindOutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.cancel))
            }
            
            ReMindButton(
                onClick = { onConfirm(text, isShared) },
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
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










