package vn.io.litever.designsystem.components.textfield

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import vn.io.litever.designsystem.theme.LiteverTheme

object LiteVerTextFieldDefaults {
    val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = LiteverTheme.shapes.medium

    @Composable
    fun shape(): Shape = LiteverTheme.shapes.medium

    @Composable
    fun colors(
        focusedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        unfocusedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
        cursorColor: Color = MaterialTheme.colorScheme.primary,
        focusedIndicatorColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor: Color = Color.Transparent
    ): TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        cursorColor = cursorColor,
        focusedIndicatorColor = focusedIndicatorColor,
        unfocusedIndicatorColor = unfocusedIndicatorColor
    )

    @Composable
    fun textFieldColors(
        focusedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        unfocusedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
        cursorColor: Color = MaterialTheme.colorScheme.primary,
        focusedIndicatorColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor: Color = Color.Transparent
    ): TextFieldColors = colors(
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        cursorColor = cursorColor,
        focusedIndicatorColor = focusedIndicatorColor,
        unfocusedIndicatorColor = unfocusedIndicatorColor
    )

    @Composable
    fun outlinedColors(
        focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline,
        cursorColor: Color = MaterialTheme.colorScheme.primary
    ): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = unfocusedBorderColor,
        cursorColor = cursorColor
    )

    @Composable
    fun outlinedTextFieldColors(
        focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline,
        cursorColor: Color = MaterialTheme.colorScheme.primary
    ): TextFieldColors = outlinedColors(
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = unfocusedBorderColor,
        cursorColor = cursorColor
    )
}

typealias LiteverTextFieldDefaults = LiteVerTextFieldDefaults
