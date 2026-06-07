package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.serialization.Serializable
import vn.io.litever.designsystem.components.LiteverCard
import vn.io.litever.designsystem.components.LiteverScaffold
import vn.io.litever.designsystem.components.LiteverTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.settings.R

@Serializable
data class ChangelogItem(
    val versionName: String,
    val date: String,
    val isLatest: Boolean = false,
    val notes: List<String> = emptyList(),
    val multiLangNotes: Map<String, List<String>>? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: UpdateHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LiteverScaffold(
        topBar = {
            LiteverTopAppBar(
                title = stringResource(R.string.setting_history),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(uiState.changelogItems) { _, item ->
                    TimelineItem(
                        item = item,
                        language = uiState.language
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    item: ChangelogItem,
    language: String
) {
    LiteverCard(
        modifier = Modifier
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "v${item.versionName}",
                    style = LiteverTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isLatest) LiteverTheme.colors.primary else LiteverTheme.colors.onSurface
                )
                if (item.isLatest) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = LiteverTheme.colors.primaryContainer.copy(alpha = 0.5f),
                        shape = LiteverTheme.shapes.small,
                        border = BorderStroke(1.dp, LiteverTheme.colors.primary.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "Latest",
                            style = LiteverTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = LiteverTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = item.date,
                style = LiteverTheme.typography.labelMedium,
                color = LiteverTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            val displayNotes = if (language == "vi") {
                item.multiLangNotes?.get("vi") ?: item.notes
            } else {
                item.multiLangNotes?.get("en") ?: item.notes
            }

            displayNotes.forEach { note ->
                Row(
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "•",
                        style = LiteverTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp),
                        color = LiteverTheme.colors.primary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = note,
                        style = LiteverTheme.typography.bodyMedium,
                        color = LiteverTheme.colors.onSurface
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun UpdateHistoryScreenPreview() {
    ReMindTheme {
        UpdateHistoryScreen(onNavigateBack = {})
    }
}
