package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.features.mission.R

/**
 * Giao diện nhiệm vụ Gõ chữ (Typing Mission) được tối ưu hóa theo thiết kế Stitch:
 * - Phía trên: Banner hướng dẫn ngắn gọn (MissionInstructionBanner).
 * - Phía dưới: Bảng gõ chữ tương tác (Interactive Board) với tiêu đề "Văn bản cần gõ",
 *   hiển thị trực quan từng ký tự đúng (xanh) / sai (đỏ) / gợi ý (mờ), không có vùng mẹo thừa.
 */
@Composable
fun TypingMissionContent(
    targetPhrase: Phrase?,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    val successColor = LiteverTheme.colors.success
    val errorColor = LiteverTheme.colors.error
    val hintColor = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.38f)

    val visualTransformation = remember(targetPhrase, userInput, successColor, errorColor, hintColor) {
        VisualTransformation { text ->
            val input = text.text
            val target = targetPhrase?.content ?: ""
            val annotatedString = buildAnnotatedString {
                for (i in target.indices) {
                    when {
                        i < input.length -> {
                            if (input[i] == target[i]) {
                                withStyle(
                                    style = SpanStyle(
                                        color = successColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {
                                    append(input[i])
                                }
                            } else {
                                withStyle(
                                    style = SpanStyle(
                                        color = errorColor,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                    )
                                ) {
                                    append(input[i])
                                }
                            }
                        }
                        else -> {
                            withStyle(
                                style = SpanStyle(
                                    color = hintColor,
                                    fontWeight = FontWeight.Normal
                                )
                            ) {
                                append(target[i])
                            }
                        }
                    }
                }
                if (input.length > target.length) {
                    withStyle(
                        style = SpanStyle(
                            color = errorColor,
                            fontWeight = FontWeight.Bold,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    ) {
                        append(input.substring(target.length))
                    }
                }
            }

            TransformedText(
                text = annotatedString,
                offsetMapping = object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int = offset
                    override fun transformedToOriginal(offset: Int): Int = offset.coerceAtMost(input.length)
                }
            )
        }
    }

    val textStyle = LiteverTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
        lineHeight = 30.sp,
        textAlign = TextAlign.Start
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
    ) {
        // 1. Phía trên: Banner hướng dẫn ngắn gọn
        MissionInstructionBanner(
            icon = Icons.Rounded.EditNote,
            requirementText = stringResource(R.string.mission_typing_requirement)
        )

        // 2. Phía dưới: Vùng làm nhiệm vụ (Interactive Typing Board) chiếm trọn phần không gian còn lại
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { focusRequester.requestFocus() }
                ),
            shape = LiteverTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = LiteverTheme.colors.surfaceContainerLowest
            ),
            border = BorderStroke(
                1.5.dp,
                LiteverTheme.colors.primary.copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(LiteverTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.smallMedium)
            ) {
                // Header của bảng: [icon] Văn bản cần gõ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Spellcheck,
                        contentDescription = null,
                        tint = LiteverTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.mission_typing_board_header),
                        style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = LiteverTheme.colors.onSurface
                    )
                }

                HorizontalDivider(
                    color = LiteverTheme.colors.surfaceContainerHighest.copy(alpha = 0.5f),
                    thickness = 1.dp
                )

                // Vùng nhập văn bản chính lấp đầy Card và cuộn độc lập
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(bottom = LiteverTheme.spacing.small),
                    contentAlignment = Alignment.TopStart
                ) {
                    BasicTextField(
                        value = userInput,
                        onValueChange = onUserInputChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = textStyle,
                        cursorBrush = SolidColor(LiteverTheme.colors.primary),
                        visualTransformation = visualTransformation,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        singleLine = false
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TypingMissionContentPreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TypingMissionContent(
                targetPhrase = Phrase(
                    content = "Dậy sớm để dẫn đầu, kích hoạt sự tỉnh táo cho một ngày tràn đầy năng lượng.",
                    categoryId = "motivation"
                ),
                userInput = "Dậy sớm để ",
                onUserInputChange = {}
            )
        }
    }
}
