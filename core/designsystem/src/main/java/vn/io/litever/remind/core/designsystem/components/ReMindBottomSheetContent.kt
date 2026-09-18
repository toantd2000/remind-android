package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import vn.io.litever.designsystem.theme.LiteverTheme

/**
 * Standard content container for bottom sheets in ReMind.
 *
 * Provides consistent title formatting and spacing. When [title] is null or not provided,
 * the content starts immediately without unnecessary empty space.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param title Optional title composable for the bottom sheet header.
 * @param content The main content of the bottom sheet.
 */
@Composable
fun ReMindBottomSheetContent(
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (title != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LiteverTheme.spacing.medium,
                        end = LiteverTheme.spacing.medium,
                        bottom = LiteverTheme.spacing.smallMedium
                    )
            ) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                ) {
                    title()
                }
            }
        }
        content()
    }
}

/**
 * String title overload of [ReMindBottomSheetContent].
 *
 * @param title Optional plain text title for the bottom sheet header.
 * @param modifier The modifier to be applied to the layout.
 * @param content The main content of the bottom sheet.
 */
@Composable
fun ReMindBottomSheetContent(
    title: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val titleComposable: (@Composable () -> Unit)? = if (title != null) {
        {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    } else {
        null
    }

    ReMindBottomSheetContent(
        modifier = modifier,
        title = titleComposable,
        content = content
    )
}
