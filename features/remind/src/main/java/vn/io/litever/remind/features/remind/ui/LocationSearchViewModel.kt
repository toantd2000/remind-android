package vn.io.litever.remind.features.remind.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.domain.repository.WeatherRepository
import vn.io.litever.remind.core.model.LocationSearchResponse
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    val selectedLocationName: StateFlow<String> = weatherRepository.getSelectedLocationName()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val selectedLocationCountry: StateFlow<String> = weatherRepository.getSelectedLocationCountry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val searchResults: StateFlow<List<LocationSearchResponse>> = _searchQuery
        .debounce(3000)
        .mapLatest { query ->
            if (query.length <= 2) return@mapLatest emptyList<LocationSearchResponse>()
            _isSearching.value = true
            val results = weatherRepository.searchLocation(query)
            _isSearching.value = false
            results
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onLocationSelected(location: LocationSearchResponse) {
        viewModelScope.launch {
            weatherRepository.saveSelectedLocation(location.name, location.country)
            weatherRepository.refreshWeather(force = true)
        }
    }

    fun onAutomaticLocationSelected() {
        viewModelScope.launch {
            weatherRepository.saveSelectedLocation("", "")
            weatherRepository.refreshWeather(force = true)
        }
    }
}
