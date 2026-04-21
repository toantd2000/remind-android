package vn.io.litever.remind.features.mission.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.features.mission.viewmodel.PhraseSelectionViewModel

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
    var showAddDialog by remember { mutableStateOf(false) }

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
        onComplete = { onPhrasesSelected(selectedIds.toList()) },
        onAddCustomPhraseClick = { showAddDialog = true },
        onDeleteCustomPhrase = viewModel::deletePhrase
    )

    if (showAddDialog) {
        AddCustomPhraseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { content, isShared ->
                viewModel.addCustomPhrase(content, isShared)
                showAddDialog = false
            }
        )
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
    onComplete: () -> Unit,
    onAddCustomPhraseClick: () -> Unit,
    onDeleteCustomPhrase: (Phrase) -> Unit
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
                onBackClick = onBackClick,
                actions = {
                    TextButton(onClick = onComplete) {
                        Text(stringResource(R.string.mission_complete))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
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
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(phrases) { phrase ->
                            PhraseItem(
                                phrase = phrase,
                                isSelected = selectedIds.contains(phrase.id),
                                onToggle = { onTogglePhrase(phrase.id) },
                                onDelete = if (phrase.isCustom) { { onDeleteCustomPhrase(phrase) } } else null
                            )
                        }
                    }
                }

                if (currentCategory == "custom") {
                    FloatingActionButton(
                        onClick = onAddCustomPhraseClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun PhraseItem(
    phrase: Phrase,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(phrase.content) },
        supportingContent = if (phrase.isCustom) {
            { Text(stringResource(if (phrase.isShared) R.string.mission_shared else R.string.mission_private)) }
        } else null,
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                    }
                }
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() }
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AddCustomPhraseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isShared by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mission_add_custom_phrase)) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(stringResource(R.string.mission_phrase_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isShared,
                        onCheckedChange = { isShared = it }
                    )
                    Text(
                        text = stringResource(R.string.mission_shared),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "Shared phrases can be used across all alarms.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text, isShared) },
                enabled = text.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.mission_cancel))
            }
        }
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PhraseSelectionScreenPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        PhraseSelectionScreen(
            predefinedPhrases = mapOf(
                "motivation" to listOf(
                    Phrase(id = 1, content = "Wake up and be awesome", categoryId = "motivation"),
                    Phrase(id = 2, content = "Success starts now", categoryId = "motivation")
                ),
                "basic" to listOf(
                    Phrase(id = 3, content = "I am awake", categoryId = "basic")
                )
            ),
            customPhrases = listOf(
                Phrase(id = 4, content = "Custom phrase 1", categoryId = "custom", isCustom = true)
            ),
            selectedIds = setOf(1, 4),
            onBackClick = {},
            onTogglePhrase = {},
            onComplete = {},
            onAddCustomPhraseClick = {},
            onDeleteCustomPhrase = {}
        )
    }
}
