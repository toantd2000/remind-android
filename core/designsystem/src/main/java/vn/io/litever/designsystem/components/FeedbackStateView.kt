package vn.io.litever.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.assets.LiteverMascotState
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

/**
 * Type of feedback to communicate through [FeedbackStateView].
 */
enum class FeedbackStateType {
    EMPTY,
    IDLE,
    SUCCESS,
    ERROR,
    WARNING,
    OFFLINE,
    LOADING,
    NOT_FOUND,
    FORBIDDEN,
    INFO,
    SEARCH_EMPTY,
    MAINTENANCE
}

/**
 * Maps each [FeedbackStateType] to its default mascot illustration drawable from `litever-brand`.
 */
@DrawableRes
fun FeedbackStateType.defaultIllustrationRes(): Int = when (this) {
    FeedbackStateType.EMPTY -> LiteverMascotState.EMPTY.drawableRes
    FeedbackStateType.IDLE -> LiteverMascotState.IDLE.drawableRes
    FeedbackStateType.SUCCESS -> LiteverMascotState.SUCCESS.drawableRes
    FeedbackStateType.ERROR -> LiteverMascotState.ERROR.drawableRes
    FeedbackStateType.WARNING -> LiteverMascotState.WARNING.drawableRes
    FeedbackStateType.OFFLINE -> LiteverMascotState.OFFLINE.drawableRes
    FeedbackStateType.LOADING -> LiteverMascotState.LOADING.drawableRes
    FeedbackStateType.NOT_FOUND -> LiteverMascotState.NOT_FOUND.drawableRes
    FeedbackStateType.FORBIDDEN -> LiteverMascotState.FORBIDDEN.drawableRes
    FeedbackStateType.INFO -> LiteverMascotState.MAINTENANCE.drawableRes
    FeedbackStateType.SEARCH_EMPTY -> LiteverMascotState.SEARCH_EMPTY.drawableRes
    FeedbackStateType.MAINTENANCE -> LiteverMascotState.MAINTENANCE.drawableRes
}

@Composable
fun FeedbackStateView(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    type: FeedbackStateType = FeedbackStateType.EMPTY,
    illustration: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    FeedbackStateView(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 360.dp)
            )
        },
        description = description?.let { desc ->
            {
                Text(
                    text = desc,
                    style = LiteverTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 360.dp)
                )
            }
        },
        type = type,
        illustration = illustration,
        action = action
    )
}

@Composable
fun FeedbackStateView(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    description: @Composable (() -> Unit)? = null,
    type: FeedbackStateType = FeedbackStateType.EMPTY,
    illustration: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    val spacing = LiteverTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (illustration != null) {
            illustration()
        } else {
            Image(
                painter = painterResource(id = type.defaultIllustrationRes()),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(160.dp)
            )
        }

        Spacer(modifier = Modifier.height(spacing.mediumLarge))

        if (title != null) {
            title()
        }

        if (description != null) {
            if (title != null) {
                Spacer(modifier = Modifier.height(spacing.small))
            }
            description()
        }

        if (action != null) {
            Spacer(modifier = Modifier.height(spacing.large))
            action()
        }
    }
}

@Preview(showBackground = true, name = "Empty State - Light")
@Composable
private fun FeedbackStateEmptyPreview() {
    ReMindTheme(darkTheme = false) {
        Surface {
            FeedbackStateView(
                title = "Chưa có dữ liệu",
                description = "Nhấn nút bên dưới để bắt đầu.",
                type = FeedbackStateType.EMPTY,
                action = {
                    LvButton(onClick = {}, semantic = LvSemantic.Primary) {
                        Text("Thêm mới")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty State - Dark")
@Composable
private fun FeedbackStateEmptyDarkPreview() {
    ReMindTheme(darkTheme = true) {
        Surface {
            FeedbackStateView(
                title = "Chưa có dữ liệu",
                description = "Nhấn nút bên dưới để bắt đầu.",
                type = FeedbackStateType.EMPTY
            )
        }
    }
}
