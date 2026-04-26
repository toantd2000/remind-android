package vn.io.litever.remind.features.remind.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
import vn.io.litever.remind.core.model.LocationSearchResponse
import vn.io.litever.remind.features.remind.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationSearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val selectedName by viewModel.selectedLocationName.collectAsState()
    val selectedCountry by viewModel.selectedLocationCountry.collectAsState()

    LocationSearchScreen(
        searchQuery = searchQuery,
        searchResults = searchResults,
        isSearching = isSearching,
        selectedLocationName = selectedName,
        selectedLocationCountry = selectedCountry,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onLocationSelected = viewModel::onLocationSelected,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchScreen(
    searchQuery: String,
    searchResults: List<LocationSearchResponse>,
    isSearching: Boolean,
    selectedLocationName: String,
    selectedLocationCountry: String,
    onSearchQueryChange: (String) -> Unit,
    onLocationSelected: (LocationSearchResponse) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReMindScaffold(
        topBar = {
            ReMindTopAppBar(
                title = "Tìm địa điểm",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nhập tên thành phố...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (searchQuery.isBlank() && selectedLocationName.isNotBlank()) {
                        item {
                            Text(
                                text = "Địa điểm hiện tại",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LocationItem(
                                location = LocationSearchResponse(
                                    id = -1,
                                    name = selectedLocationName,
                                    region = "",
                                    country = selectedLocationCountry,
                                    lat = 0.0,
                                    lon = 0.0,
                                    url = ""
                                ),
                                isSelected = true,
                                onClick = {}
                            )
                        }
                    } else {
                        items(searchResults) { location ->
                            val isSelected = location.name == selectedLocationName && 
                                            location.country == selectedLocationCountry
                            LocationItem(
                                location = location,
                                isSelected = isSelected,
                                onClick = { onLocationSelected(location) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationItem(
    location: LocationSearchResponse,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = location.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
