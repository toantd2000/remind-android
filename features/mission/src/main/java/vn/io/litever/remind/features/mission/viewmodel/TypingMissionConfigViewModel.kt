package vn.io.litever.remind.features.mission.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
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
}










