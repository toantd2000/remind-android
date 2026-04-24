package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import vn.io.litever.remind.core.designsystem.components.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import vn.io.litever.remind.features.settings.R

@Composable
fun LicensesScreen(
    onNavigateBack: () -> Unit
) {
    ReMindScaffold(
        topBar = {
            ReMindTopAppBar(
                title = stringResource(R.string.setting_licenses),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        val libraries by produceLibraries()

        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            colors = LibraryDefaults.libraryColors(
                libraryBackgroundColor = MaterialTheme.colorScheme.background,
                libraryContentColor = MaterialTheme.colorScheme.onBackground,
                dialogBackgroundColor = MaterialTheme.colorScheme.surface,
                dialogContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}










