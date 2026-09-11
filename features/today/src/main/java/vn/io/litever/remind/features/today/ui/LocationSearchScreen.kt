package vn.io.litever.remind.features.today.ui

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
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
import vn.io.litever.remind.core.model.LocationSearchResponse
import vn.io.litever.remind.features.today.R
import vn.io.litever.designsystem.components.LiteVerTextFieldDefaults
import vn.io.litever.designsystem.theme.LiteverTheme

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
        onAutomaticLocationSelected = viewModel::onAutomaticLocationSelected,
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
    onAutomaticLocationSelected: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            ReMindTopAppBar(
                title = stringResource(R.string.search_location_title),
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(LiteverTheme.spacing.medium)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_location_placeholder)) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = LiteVerTextFieldDefaults.shape,
                colors = LiteVerTextFieldDefaults.outlinedColors()
            )

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

            if (isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    item {
                        AutomaticLocationItem(
                            isSelected = selectedLocationName.isBlank(),
                            onClick = onAutomaticLocationSelected
                        )
                    }

                    if (searchQuery.isBlank() && selectedLocationName.isNotBlank()) {
                        item {
                            Text(
                                text = "Địa điểm hiện tại",
                                style = MaterialTheme.typography.labelMedium,
                                color = LiteverTheme.colors.primary,
                                modifier = Modifier.padding(bottom = LiteverTheme.spacing.small)
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
fun AutomaticLocationItem(
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = LiteverTheme.shapes.medium,
        color = if (isSelected) {
            LiteverTheme.colors.primaryContainer.copy(alpha = 0.3f)
        } else {
            LiteverTheme.colors.surfaceVariant.copy(alpha = 0.3f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) LiteverTheme.colors.primary.copy(alpha = 0.5f)
            else LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(LiteverTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.MyLocation,
                contentDescription = null,
                tint = if (isSelected) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(LiteverTheme.spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.weather_location_automatic),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) LiteverTheme.colors.primary else LiteverTheme.colors.onSurface
                )
                Text(
                    text = stringResource(R.string.weather_location_automatic_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = LiteverTheme.colors.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = LiteverTheme.colors.primary,
                    modifier = Modifier.size(LiteverTheme.spacing.mediumLarge)
                )
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
        shape = LiteverTheme.shapes.medium,
        color = if (isSelected) {
            LiteverTheme.colors.primaryContainer.copy(alpha = 0.3f)
        } else {
            LiteverTheme.colors.surfaceVariant.copy(alpha = 0.3f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) LiteverTheme.colors.primary.copy(alpha = 0.5f)
            else LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(LiteverTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = if (isSelected) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(LiteverTheme.spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) LiteverTheme.colors.primary else LiteverTheme.colors.onSurface
                )
                Text(
                    text = location.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = LiteverTheme.colors.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = LiteverTheme.colors.primary,
                    modifier = Modifier.size(LiteverTheme.spacing.mediumLarge)
                )
            }
        }
    }
}
