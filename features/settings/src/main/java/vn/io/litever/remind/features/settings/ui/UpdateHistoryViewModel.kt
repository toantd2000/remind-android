package vn.io.litever.remind.features.settings.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import javax.inject.Inject

data class UpdateHistoryUiState(
    val changelogItems: List<ChangelogItem> = emptyList(),
    val language: String = "en",
    val isLoading: Boolean = true
)

@HiltViewModel
class UpdateHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataSource: AlarmPreferencesDataSource
) : ViewModel() {

    private val changelogItems = loadChangelog(context)

    val uiState: StateFlow<UpdateHistoryUiState> = combine(
        flowOf(changelogItems),
        preferencesDataSource.language
    ) { items, lang ->
        UpdateHistoryUiState(
            changelogItems = items,
            language = lang,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateHistoryUiState()
    )

    private fun loadChangelog(context: Context): List<ChangelogItem> {
        return try {
            val jsonString = context.assets.open("changelog.json").bufferedReader().use { it.readText() }
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<List<ChangelogItem>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
