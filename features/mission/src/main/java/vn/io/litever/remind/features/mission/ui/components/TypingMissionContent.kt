package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.features.mission.R
import kotlin.math.roundToInt

/**
 * Giao diện nhiệm vụ Gõ chữ (Typing Mission) chuẩn Gamification:
 * - Khi gõ sai: Rung cảnh báo + Lắc nhẹ khung văn bản.
 * - Khi gõ đúng 100%: Thẻ sáng xanh lá, hiện huy hiệu "Chính xác! ✓" chuẩn bị bấm Tiếp tục.
 */
@Composable
fun TypingMissionContent(
    targetPhrase: Phrase?,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onSubmit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    val successColor = LiteverTheme.colors.success
    val errorColor = LiteverTheme.colors.error
    val hintColor = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.38f)

    val targetContent = targetPhrase?.content ?: ""
    val isMatched = userInput == targetContent && targetContent.isNotEmpty()
    var prevLength by remember { mutableStateOf(0) }

    // Rung và lắc khi gõ thêm ký tự sai
    LaunchedEffect(userInput) {
        if (userInput.length > prevLength) {
            val lastIndex = userInput.length - 1
            val isCurrentCharWrong = lastIndex >= targetContent.length || userInput[lastIndex] != targetContent[lastIndex]
            if (isCurrentCharWrong) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                coroutineScope.launch {
                    shakeOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = keyframes {
                            durationMillis = 250
                            -8f at 40
                            8f at 80
                            -6f at 120
                            6f at 160
                            -3f at 200
                            0f at 250
                        }
                    )
                }
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
        prevLength = userInput.length
    }

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
        // 1. Phía trên: Banner hướng dẫn
        MissionInstructionBanner(
            icon = ReMindIcons.EditNote,
            requirementText = stringResource(R.string.mission_typing_requirement)
        )

        // 2. Phía dưới: Vùng làm nhiệm vụ (Interactive Typing Board)
        val borderColor by animateColorAsState(
            targetValue = if (isMatched) LiteverTheme.colors.success else LiteverTheme.colors.primary.copy(alpha = 0.25f),
            label = "typingBorderColor"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { focusRequester.requestFocus() }
                ),
            shape = LiteverTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = LiteverTheme.colors.surfaceContainerLowest
            ),
            border = BorderStroke(1.5.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(LiteverTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.smallMedium)
            ) {
                // Header của bảng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall)
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Spellcheck,
                            contentDescription = null,
                            tint = if (isMatched) LiteverTheme.colors.success else LiteverTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.mission_typing_board_header),
                            style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = LiteverTheme.colors.onSurface
                        )
                    }

                    if (isMatched) {
                        Surface(
                            shape = LiteverTheme.shapes.small,
                            color = LiteverTheme.colors.successContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = ReMindIcons.CheckCircle,
                                    contentDescription = null,
                                    tint = LiteverTheme.colors.onSuccessContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = stringResource(R.string.mission_typing_completed_badge),
                                    style = LiteverTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LiteverTheme.colors.onSuccessContainer
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = LiteverTheme.colors.surfaceContainerHighest.copy(alpha = 0.5f),
                    thickness = 1.dp
                )

                // Vùng nhập văn bản
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
                        cursorBrush = SolidColor(if (isMatched) LiteverTheme.colors.success else LiteverTheme.colors.primary),
                        visualTransformation = visualTransformation,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (isMatched) onSubmit()
                            }
                        ),
                        singleLine = false
                    )
                }
            }
        }

        // 3. Nút Tiếp tục riêng cho nhiệm vụ Gõ chữ (2/3 chiều ngang, căn giữa)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            LvButton(
                onClick = onSubmit,
                enabled = isMatched,
                semantic = LvSemantic.Primary,
                modifier = Modifier.fillMaxWidth(2f / 3f)
            ) {
                Text(
                    text = stringResource(R.string.action_continue),
                    style = LiteverTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(LiteverTheme.spacing.extraSmall))
                Icon(
                    imageVector = ReMindIcons.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
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
