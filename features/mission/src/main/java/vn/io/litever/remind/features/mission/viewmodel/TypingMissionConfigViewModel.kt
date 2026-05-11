package vn.io.litever.remind.features.mission.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.domain.repository.MissionRepository
import vn.io.litever.remind.core.model.Phrase
import javax.inject.Inject

@HiltViewModel
class TypingMissionConfigViewModel @Inject constructor(
    private val missionRepository: MissionRepository
) : ViewModel() {
    val basicPhrases = flow {
        val phrases = missionRepository.getPredefinedPhrases()["basic"] ?: emptyList()
        emit(phrases)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPhrases = MutableStateFlow<List<Phrase>>(emptyList())
    val selectedPhrases: StateFlow<List<Phrase>> = _selectedPhrases.asStateFlow()

    fun loadSelectedPhrases(ids: List<Long>, alarmId: Long) {
        viewModelScope.launch {
            val phrases = missionRepository.getPhrasesByIds(ids, alarmId)
            _selectedPhrases.value = phrases
        }
    }
}










