package vn.io.litever.remind.features.mission.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.domain.repository.MissionRepository
import vn.io.litever.remind.core.model.Phrase
import javax.inject.Inject

@HiltViewModel
class PhraseSelectionViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val reminderId: Long = savedStateHandle.get<Long>("reminderId") ?: 0L
    
    private val _predefinedPhrases = mutableStateOf<Map<String, List<Phrase>>>(emptyMap())
    val predefinedPhrases: State<Map<String, List<Phrase>>> = _predefinedPhrases

    val customPhrases = missionRepository.getCustomPhrases(reminderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _predefinedPhrases.value = missionRepository.getPredefinedPhrases()
        }
    }

    fun addCustomPhrase(content: String, isShared: Boolean) {
        if (content.isBlank()) return
        viewModelScope.launch {
            missionRepository.savePhrase(
                Phrase(
                    content = content,
                    categoryId = "custom",
                    isCustom = true,
                    isShared = isShared,
                    reminderId = if (isShared) null else reminderId
                )
            )
        }
    }

    fun deletePhrase(phrase: Phrase) {
        viewModelScope.launch {
            missionRepository.deletePhrase(phrase)
        }
    }
}
