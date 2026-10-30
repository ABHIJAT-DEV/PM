@file:OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)

package com.saurav.pixelmusic.presentation.components

import android.content.res.Resources
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.saurav.pixelmusic.R
import com.saurav.pixelmusic.ui.theme.GoogleSansRounded
import com.saurav.pixelmusic.ui.theme.LocalLiquidGlassEnabled
import kotlin.math.max
import kotlin.math.min
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

@Composable
fun PlaylistCreationTypeDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onManualSelected: () -> Unit,
    onAiSelected: () -> Unit,
    isAiEnabled: Boolean = true,
    onSetupAiClick: (() -> Unit)? = null
) {
    if (!visible) return

    val dialogShape = AbsoluteSmoothCornerShape(
        cornerRadiusTL = 24.dp,
        smoothnessAsPercentTL = 60,
        cornerRadiusTR = 24.dp,
        smoothnessAsPercentTR = 60,
        cornerRadiusBL = 40.dp,
        smoothnessAsPercentBL = 60,
        cornerRadiusBR = 40.dp,
        smoothnessAsPercentBR = 60
    )

    val isLiquidGlass = LocalLiquidGlassEnabled.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val glassBorderBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = if (isDark) listOf(
                Color.White.copy(alpha = 0.07f),
                Color.White.copy(alpha = 0.02f)
            ) else listOf(
                Color.White.copy(alpha = 0.14f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    }
    val sheenBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.05f else 0.10f),
                Color.Transparent
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            shape = dialogShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = if (isLiquidGlass) 0.dp else 6.dp,
            shadowElevation = if (isLiquidGlass) 0.dp else 6.dp,
            modifier = Modifier.then(
                if (isLiquidGlass) {
                    Modifier
                        .graphicsLayer { shadowElevation = 0f }
                        .border(BorderStroke(0.75.dp, glassBorderBrush), dialogShape)
                        .clip(dialogShape)
                } else Modifier
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.presentation_batch_e_create_playlist_title),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = GoogleSansRounded,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(R.string.presentation_batch_e_create_playlist_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }

                CreationModeCard(
                    title = stringResource(R.string.presentation_batch_e_creation_mode_manual),
                    subtitle = stringResource(R.string.presentation_batch_e_creation_mode_manual_subtitle),
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    onClick = onManualSelected,
                    enabled = true,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                CreationModeCard(
                    title = stringResource(R.string.presentation_batch_e_creation_mode_ai),
                    subtitle = if (isAiEnabled) {
                        stringResource(R.string.presentation_batch_e_creation_mode_ai_subtitle_enabled)
                    } else {
                        stringResource(R.string.presentation_batch_e_creation_mode_ai_subtitle_disabled)
                    },
                    icon = {
                        Icon(
                            imageVector = if (isAiEnabled) Icons.Rounded.AutoAwesome else Icons.Rounded.Key,
                            contentDescription = null,
                            tint = if (isAiEnabled) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    onClick = {
                        if (isAiEnabled) {
                            onAiSelected()
                        } else {
                            onDismiss()
                            onSetupAiClick?.invoke()
                        }
                    },
                    enabled = true,
                    containerColor = if (isAiEnabled) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                    contentColor = if (isAiEnabled) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (!isAiEnabled) {
                    FilledTonalButton(
                        onClick = {
                            onDismiss()
                            onSetupAiClick?.invoke()
                        },
                        elevation = if (isLiquidGlass) ButtonDefaults.filledTonalButtonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp) else ButtonDefaults.filledTonalButtonElevation(),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isLiquidGlass) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.55f else 0.70f) else MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassInteractiveSurface(shape = CircleShape),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Rounded.Key, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.presentation_batch_e_setup_api_key))
                    }
                }
            }
        }
    }
}

@Composable
private fun CreationModeCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color
) {
    val isLiquidGlass = LocalLiquidGlassEnabled.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val glassBorderBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = if (isDark) listOf(
                Color.White.copy(alpha = 0.07f),
                Color.White.copy(alpha = 0.02f)
            ) else listOf(
                Color.White.copy(alpha = 0.14f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    }
    val sheenBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.05f else 0.10f),
                Color.Transparent
            )
        )
    }
    val cardShape = AbsoluteSmoothCornerShape(
        cornerRadiusTL = 22.dp,
        smoothnessAsPercentTL = 60,
        cornerRadiusTR = 22.dp,
        smoothnessAsPercentTR = 60,
        cornerRadiusBL = 22.dp,
        smoothnessAsPercentBL = 60,
        cornerRadiusBR = 22.dp,
        smoothnessAsPercentBR = 60
    )

    Card(
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(containerColor = if (isLiquidGlass) containerColor.copy(alpha = if (isDark) 0.55f else 0.70f) else containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLiquidGlass) 0.dp else 2.dp),
        shape = cardShape,
        modifier = Modifier.liquidGlassInteractiveSurface(shape = cardShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = contentColor.copy(alpha = 0.16f),
                shape = AbsoluteSmoothCornerShape(
                    cornerRadiusTL = 12.dp,
                    smoothnessAsPercentTL = 60,
                    cornerRadiusTR = 18.dp,
                    smoothnessAsPercentTR = 60,
                    cornerRadiusBL = 18.dp,
                    smoothnessAsPercentBL = 60,
                    cornerRadiusBR = 12.dp,
                    smoothnessAsPercentBR = 60
                )
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    icon()
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAiPlaylistDialog(
    visible: Boolean,
    isGenerating: Boolean,
    isSuccess: Boolean = false,
    status: String? = null,
    error: String?,
    onDismiss: () -> Unit,
    onGenerate: (playlistName: String?, prompt: String, minLength: Int, maxLength: Int) -> Unit
) {
    if (visible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = {
                if (!isGenerating) {
                    onDismiss()
                }
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            },
            shape = AbsoluteSmoothCornerShape(
                cornerRadiusTL = 28.dp,
                smoothnessAsPercentTL = 60,
                cornerRadiusTR = 28.dp,
                smoothnessAsPercentTR = 60
            )
        ) {
            CreateAiPlaylistSheetContent(
                isGenerating = isGenerating,
                isSuccess = isSuccess,
                status = status,
                error = error,
                onDismiss = onDismiss,
                onGenerate = onGenerate
            )
        }
    }
}

@Composable
private fun CreateAiPlaylistSheetContent(
    isGenerating: Boolean,
    isSuccess: Boolean,
    status: String?,
    error: String?,
    onDismiss: () -> Unit,
    onGenerate: (playlistName: String?, prompt: String, minLength: Int, maxLength: Int) -> Unit
) {
    var basePrompt by rememberSaveable { mutableStateOf("") }
    var playlistName by rememberSaveable { mutableStateOf("") }
    var selectedLength by rememberSaveable { mutableStateOf("Auto") }
    var selectedEnergy by rememberSaveable { mutableStateOf("Auto") }
    var isRefining by rememberSaveable { mutableStateOf(false) }

    val triggerGeneration: () -> Unit = {
        if (basePrompt.isNotBlank()) {
            val builtPrompt = buildString {
                append(basePrompt.trim())
                if (selectedEnergy != "Auto") {
                    append(". Energy: $selectedEnergy.")
                }
                if (selectedLength != "Auto") {
                    append(". Length: $selectedLength tracks.")
                }
            }
            val (minCount, maxCount) = when (selectedLength) {
                "Up to 15" -> 10 to 15
                "Up to 25" -> 15 to 25
                "Up to 50" -> 25 to 50
                else -> 12 to 30
            }
            onGenerate(playlistName.trim().ifBlank { null }, builtPrompt, minCount, maxCount)
        }
    }

    AnimatedContent(
        targetState = isGenerating,
        transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(200)) },
        label = "ai_playlist_sheet_generation_state"
    ) { generating ->
        if (generating) {
            AiPlaylistCreationAnimationView(
                status = status,
                isSuccess = isSuccess,
                error = error,
                onDismiss = onDismiss
            )
        } else {
            AnimatedContent(
                targetState = isRefining,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "ai_playlist_sheet_refine_state"
            ) { refining ->
                if (refining) {
                    AiPlaylistRefineView(
                        prompt = basePrompt,
                        playlistName = playlistName,
                        onPlaylistNameChange = { playlistName = it },
                        selectedLength = selectedLength,
                        onLengthChange = { selectedLength = it },
                        selectedEnergy = selectedEnergy,
                        onEnergyChange = { selectedEnergy = it },
                        onBack = { isRefining = false },
                        onDismiss = onDismiss
                    )
                } else {
                    AiPlaylistMainTuneView(
                        prompt = basePrompt,
                        onPromptChange = { basePrompt = it },
                        selectedLength = selectedLength,
                        selectedEnergy = selectedEnergy,
                        error = error,
                        onOpenRefine = { isRefining = true },
                        onDismiss = onDismiss,
                        onGenerate = triggerGeneration
                    )
                }
            }
        }
    }
}

@Composable
private fun AiPlaylistMainTuneView(
    prompt: String,
    onPromptChange: (String) -> Unit,
    selectedLength: String,
    selectedEnergy: String,
    error: String?,
    onOpenRefine: () -> Unit,
    onDismiss: () -> Unit,
    onGenerate: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row: Clean close button only (Header text and icon removed per user request)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Floating Material You expressive shapes with dynamic shadow & glow
        FloatingExpressiveShapes(isGenerating = false)

        Spacer(modifier = Modifier.height(18.dp))

        // Large Headline
        Text(
            text = "What do you want to hear?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Describe a vibe, scenario, or mood",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Prompt input container with dynamic app palette & Material You arrow button
        Surface(
            shape = AbsoluteSmoothCornerShape(26.dp, 60),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (prompt.isEmpty()) {
                        Text(
                            text = "Upbeat morning…",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                    BasicTextField(
                        value = prompt,
                        onValueChange = onPromptChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = false,
                        maxLines = 3
                    )
                }

                // Material You Expressive Action Button
                val canSubmit = prompt.isNotBlank()
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                val buttonScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "arrow_btn_scale"
                )
                val buttonCorner by animateDpAsState(
                    targetValue = if (isPressed) 26.dp else 18.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "arrow_btn_corner"
                )

                Surface(
                    onClick = {
                        if (canSubmit) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onGenerate()
                        }
                    },
                    enabled = canSubmit,
                    interactionSource = interactionSource,
                    shape = AbsoluteSmoothCornerShape(buttonCorner, 60),
                    color = if (canSubmit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = if (canSubmit) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    modifier = Modifier
                        .size(width = 64.dp, height = 54.dp)
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Generate",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = AbsoluteSmoothCornerShape(16.dp, 60),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Thick Material You quick-select pills in a responsive FlowRow (no squishing, no wrapping bugs!)
        ExpressiveQuickSelectGrid(
            onPillClick = { vibe ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPromptChange(vibe)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Refine Section Card following app theme palette
        Surface(
            shape = AbsoluteSmoothCornerShape(22.dp, 60),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .fillMaxWidth()
                .clip(AbsoluteSmoothCornerShape(22.dp, 60))
                .clickable(onClick = onOpenRefine)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = AbsoluteSmoothCornerShape(12.dp, 60)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Refine your mix",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RefineBadge(text = "Length: $selectedLength")
                        RefineBadge(text = "Energy: $selectedEnergy")
                    }
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Refine options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AiPlaylistRefineView(
    prompt: String,
    playlistName: String,
    onPlaylistNameChange: (String) -> Unit,
    selectedLength: String,
    onLengthChange: (String) -> Unit,
    selectedEnergy: String,
    onEnergyChange: (String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .imePadding()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Refine your mix",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            FilledTonalIconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Prompt title echo
        Text(
            text = prompt.ifBlank { "Your mix" },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Playlist length card (Full-filled chips, no borders)
        Surface(
            shape = AbsoluteSmoothCornerShape(22.dp, 60),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Playlist length",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Auto", "Up to 15", "Up to 25", "Up to 50").forEach { option ->
                        ExpressiveChoiceChip(
                            label = option,
                            selected = selectedLength == option,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onLengthChange(option)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Energy card (Full-filled chips, no borders)
        Surface(
            shape = AbsoluteSmoothCornerShape(22.dp, 60),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Energy",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Auto", "Calm", "Steady", "Energetic").forEach { option ->
                        ExpressiveChoiceChip(
                            label = option,
                            selected = selectedEnergy == option,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onEnergyChange(option)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Optional custom playlist name
        Surface(
            shape = AbsoluteSmoothCornerShape(22.dp, 60),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Custom playlist title (optional)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = onPlaylistNameChange,
                    placeholder = { Text("Auto-generated by AI if left blank") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AbsoluteSmoothCornerShape(14.dp, 60)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Auto follows your request. Selecting energy adds a requirement; fewer songs may match.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Done button following Material You app palette with shape morphing touch feedback
        val doneInteractionSource = remember { MutableInteractionSource() }
        val isDonePressed by doneInteractionSource.collectIsPressedAsState()
        val doneScale by animateFloatAsState(
            targetValue = if (isDonePressed) 0.95f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "done_scale"
        )
        val doneCorner by animateDpAsState(
            targetValue = if (isDonePressed) 16.dp else 26.dp,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "done_corner"
        )

        Surface(
            onClick = onBack,
            interactionSource = doneInteractionSource,
            shape = AbsoluteSmoothCornerShape(doneCorner, 60),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .graphicsLayer {
                    scaleX = doneScale
                    scaleY = doneScale
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 4 Floating Material You expressive shapes with dynamic shadow + glow.
 * When isGenerating == true, they react by spinning, gently zooming in/out, moving along orbital paths, and morphing shapes!
 */
@Composable
private fun FloatingExpressiveShapes(isGenerating: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating_shapes_infinite")

    // Continuous rotation for generation state
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isGenerating) 3000 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shape_spin"
    )

    // Breathing phase for floating / zooming
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isGenerating) 1800 else 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shape_pulse"
    )

    // Corner morphing animation during generation
    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isGenerating) 1600 else 3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shape_morph"
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val secondary = MaterialTheme.colorScheme.secondary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val colors = listOf(primary, tertiary, secondary, primaryContainer)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEachIndexed { index, shapeColor ->
                // Staggered orbital motion
                val phaseOffset = index * 1.57f
                val verticalMovement = kotlin.math.sin(pulsePhase + phaseOffset) * (if (isGenerating) 12f else 4f)
                val horizontalMovement = kotlin.math.cos(pulsePhase + phaseOffset) * (if (isGenerating) 8f else 0f)

                // Zooming in/out (little, not so much)
                val baseScale = 1.0f + kotlin.math.sin(pulsePhase + phaseOffset) * (if (isGenerating) 0.12f else 0.04f)

                // Dynamic corner radius morphing
                val cornerRadiusA = (8 + (morphProgress * 16)).dp
                val cornerRadiusB = (24 - (morphProgress * 14)).dp

                val expressiveShape = when (index % 4) {
                    0 -> AbsoluteSmoothCornerShape(
                        cornerRadiusTL = cornerRadiusA,
                        cornerRadiusTR = cornerRadiusB,
                        cornerRadiusBL = cornerRadiusB,
                        cornerRadiusBR = cornerRadiusA,
                        smoothnessAsPercentTL = 60,
                        smoothnessAsPercentBR = 60
                    )
                    1 -> AbsoluteSmoothCornerShape(
                        cornerRadiusTL = cornerRadiusB,
                        cornerRadiusTR = cornerRadiusA,
                        cornerRadiusBL = cornerRadiusA,
                        cornerRadiusBR = cornerRadiusB,
                        smoothnessAsPercentTR = 60,
                        smoothnessAsPercentBL = 60
                    )
                    2 -> AbsoluteSmoothCornerShape(
                        cornerRadiusTL = cornerRadiusA,
                        cornerRadiusTR = cornerRadiusA,
                        cornerRadiusBL = cornerRadiusB,
                        cornerRadiusBR = cornerRadiusA,
                        smoothnessAsPercentTL = 60
                    )
                    else -> AbsoluteSmoothCornerShape(
                        cornerRadiusTL = cornerRadiusB,
                        cornerRadiusTR = cornerRadiusA,
                        cornerRadiusBL = cornerRadiusB,
                        cornerRadiusBR = cornerRadiusB,
                        smoothnessAsPercentBR = 60
                    )
                }

                Box(
                    modifier = Modifier
                        .offset(x = horizontalMovement.dp, y = verticalMovement.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Soft glow aura
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .graphicsLayer {
                                scaleX = baseScale
                                scaleY = baseScale
                                if (isGenerating) {
                                    rotationZ = spinAngle * (if (index % 2 == 0) 1 else -1)
                                }
                            }
                            .blur(8.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(shapeColor.copy(alpha = 0.6f), shapeColor.copy(alpha = 0.2f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )

                    // Main expressive surface with Material You shadow
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                scaleX = baseScale
                                scaleY = baseScale
                                if (isGenerating) {
                                    rotationZ = spinAngle * (if (index % 2 == 0) 1 else -1)
                                }
                            }
                            .shadow(
                                elevation = if (isGenerating) 10.dp else 4.dp,
                                shape = expressiveShape,
                                ambientColor = shapeColor,
                                spotColor = shapeColor
                            )
                            .clip(expressiveShape)
                            .background(shapeColor)
                    )
                }
            }
        }
    }
}

/**
 * Grid of thick, full-filled Material You quick select pills arranged in a responsive FlowRow.
 * Each pill has an icon embedded in a distinct Material You expressive shape on the left,
 * text on the right, and rich touch/shape-morphing feedback!
 */
private data class VibeItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpressiveQuickSelectGrid(
    onPillClick: (String) -> Unit
) {
    val vibes = listOf(
        VibeItem("Feel good", Icons.Rounded.Whatshot),
        VibeItem("Upbeat morning", Icons.Rounded.WbSunny),
        VibeItem("Workout energy", Icons.Rounded.FitnessCenter),
        VibeItem("Chill evening", Icons.Rounded.NightsStay),
        VibeItem("Late night drive", Icons.Rounded.DirectionsCar),
        VibeItem("Music to study", Icons.Rounded.Headphones),
        VibeItem("Party hype", Icons.Rounded.Celebration),
        VibeItem("Acoustic coffee", Icons.Rounded.MusicNote),
        VibeItem("Nostalgic 2000s", Icons.Rounded.History),
        VibeItem("Piano peaceful", Icons.Rounded.Piano),
        VibeItem("Deep focus", Icons.Rounded.Star),
        VibeItem("Radio hits", Icons.Rounded.GraphicEq)
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        vibes.forEachIndexed { index, vibe ->
            ExpressiveQuickSelectPill(
                vibe = vibe,
                shapeVariant = index % 3,
                onClick = { onPillClick(vibe.label) }
            )
        }
    }
}

@Composable
private fun ExpressiveQuickSelectPill(
    vibe: VibeItem,
    shapeVariant: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth touch scale animation
    val pillScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pill_scale"
    )

    // Shape corner morphing on touch
    val outerCorner by animateDpAsState(
        targetValue = if (isPressed) 26.dp else 16.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pill_outer_corner"
    )

    val innerCorner by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 11.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pill_inner_corner"
    )

    // Material You expressive shape for the inner icon badge
    val innerShape = when (shapeVariant) {
        0 -> AbsoluteSmoothCornerShape(
            cornerRadiusTL = innerCorner,
            cornerRadiusTR = innerCorner * 1.5f,
            cornerRadiusBL = innerCorner * 1.5f,
            cornerRadiusBR = innerCorner,
            smoothnessAsPercentTL = 60
        )
        1 -> AbsoluteSmoothCornerShape(
            cornerRadiusTL = innerCorner * 1.5f,
            cornerRadiusTR = innerCorner,
            cornerRadiusBL = innerCorner,
            cornerRadiusBR = innerCorner * 1.5f,
            smoothnessAsPercentTR = 60
        )
        else -> AbsoluteSmoothCornerShape(innerCorner, 60)
    }

    val pillContainerColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(150),
        label = "pill_bg_color"
    )

    val pillShape = AbsoluteSmoothCornerShape(outerCorner, 60)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = pillShape,
        color = pillContainerColor,
        shadowElevation = if (isPressed) 1.dp else 2.dp,
        modifier = Modifier
            .graphicsLayer {
                scaleX = pillScale
                scaleY = pillScale
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Icon inside a Material You expressive shape
            Surface(
                shape = innerShape,
                color = if (isPressed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = vibe.icon,
                        contentDescription = null,
                        tint = if (isPressed) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right side: Bold text label
            Text(
                text = vibe.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isPressed) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * Full-filled choice chip (no borders) with shape morphing & spring touch feedback.
 */
@Composable
private fun ExpressiveChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "chip_scale"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (selected) 22.dp else if (isPressed) 18.dp else 12.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "chip_corner"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primary
            isPressed -> MaterialTheme.colorScheme.surfaceContainerHighest
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(180),
        label = "chip_color"
    )

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val chipShape = AbsoluteSmoothCornerShape(cornerRadius, 60)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = chipShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = if (selected) 3.dp else 0.dp,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RefineBadge(text: String) {
    Surface(
        shape = AbsoluteSmoothCornerShape(10.dp, 60),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AiPlaylistCreationAnimationView(
    status: String?,
    isSuccess: Boolean,
    error: String?,
    onDismiss: () -> Unit
) {
    val phases = listOf(
        "Tuning to your taste & listening patterns…",
        "Scanning local & cloud tracks for matching vibes…",
        "Discovering fresh recommendations & flow…",
        "Harmonizing energy transitions & playlist order…",
        "Synthesizing smart titles & final touches…"
    )
    var phaseIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(isSuccess, error) {
        if (!isSuccess && error == null) {
            while (true) {
                kotlinx.coroutines.delay(2200)
                phaseIndex = (phaseIndex + 1) % phases.size
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Floating reactive Material You expressive shapes that spin, zoom in/out, move and morph!
        FloatingExpressiveShapes(isGenerating = !isSuccess)

        Spacer(modifier = Modifier.height(28.dp))

        // Headline
        Text(
            text = if (isSuccess) "Mix synthesized!" else "Creating your AI Playlist…",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Animated stage subtitle
        AnimatedContent(
            targetState = if (isSuccess) {
                "Opening your new playlist now…"
            } else {
                status ?: phases[phaseIndex]
            },
            transitionSpec = {
                (slideInVertically { height -> height / 2 } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height / 2 } + fadeOut())
            },
            label = "ai_generation_phase_text"
        ) { currentText ->
            Text(
                text = currentText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Animated Equalizer Visualizer Bars
        if (!isSuccess && error == null) {
            AnimatedEqualizerBars()
        }

        // If error occurred
        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = AbsoluteSmoothCornerShape(16.dp, 60),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedEqualizerBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars_transition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "eq_phase"
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(28.dp)
    ) {
        listOf(0.0f, 1.2f, 2.4f, 3.6f, 4.8f).forEach { offset ->
            val heightFraction = 0.3f + 0.7f * ((kotlin.math.sin(phase + offset) + 1f) / 2f)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height((26 * heightFraction).dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(primary, tertiary)
                        )
                    )
            )
        }
    }
}
