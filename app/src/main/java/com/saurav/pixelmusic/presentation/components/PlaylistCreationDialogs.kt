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
import androidx.compose.animation.core.Animatable
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
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        AnimatedContent(
            targetState = isRefining && !isGenerating,
            transitionSpec = {
                (fadeIn(tween(260, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(260, easing = FastOutSlowInEasing))) togetherWith
                        (fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                                scaleOut(targetScale = 0.96f, animationSpec = tween(180, easing = FastOutSlowInEasing)))
            },
            label = "ai_playlist_sheet_refine_state"
        ) { showRefine ->
            if (showRefine) {
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
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(bottom = 32.dp)
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Row: Clean close button (hidden while actively generating)
                    AnimatedVisibility(visible = !isGenerating) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
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
                    }

                    Spacer(modifier = Modifier.height(if (isGenerating) 18.dp else 8.dp))

                    // Persistently mounted 4 large Material You expressive shapes with shadow + glow
                    // Smoothly transitions from gentle floating to active spinning/zooming/moving/morphing
                    FloatingExpressiveShapes(isGenerating = isGenerating && !isSuccess)

                    Spacer(modifier = Modifier.height(22.dp))

                    AnimatedContent(
                        targetState = isGenerating,
                        transitionSpec = {
                            (fadeIn(tween(320, easing = FastOutSlowInEasing)) +
                                    slideInVertically(animationSpec = tween(320, easing = FastOutSlowInEasing)) { it / 6 }) togetherWith
                                    (fadeOut(tween(200, easing = FastOutSlowInEasing)) +
                                            slideOutVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { -it / 6 })
                        },
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
                            AiPlaylistMainTuneView(
                                prompt = basePrompt,
                                onPromptChange = { basePrompt = it },
                                selectedLength = selectedLength,
                                selectedEnergy = selectedEnergy,
                                error = error,
                                onOpenRefine = { isRefining = true },
                                onGenerate = triggerGeneration
                            )
                        }
                    }
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
    onGenerate: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Headline
        Text(
            text = "What do you want to hear?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Describe a vibe, scenario, or mood",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Prompt input container with dynamic app palette & Material You arrow button
        Surface(
            shape = AbsoluteSmoothCornerShape(28.dp, 60),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
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
                    targetValue = if (isPressed) 28.dp else 20.dp,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Horizontal scrollable line of thick Material You expressive shortcut pills
        ExpressiveQuickSelectRow(
            selectedPrompt = prompt,
            onPillClick = { vibe ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPromptChange(vibe)
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Refine Section Card following app theme palette
        Surface(
            shape = AbsoluteSmoothCornerShape(22.dp, 60),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
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
 * Material You Expressive polar shapes used for both the 4 large floating hero shapes
 * and the 1:1 icon containers inside the horizontal shortcut pills.
 */
private enum class AiExpressiveShapeType {
    PILL,
    COOKIE,
    FOUR_HEAD_COOKIE,
    ROUNDED_TRIANGLE,
    ROUNDED_SQUARE,
    CIRCLE,
    SIX_COOKIE,
    SUNNY_COOKIE;

    fun radiusAt(th: Double): Double {
        return when (this) {
            PILL -> {
                val a = th + PI / 6.0
                val c = abs(cos(a)) / 0.62
                val s = abs(sin(a)) / 1.0
                0.96 / (c.pow(4.2) + s.pow(4.2)).pow(1.0 / 4.2)
            }
            COOKIE -> {
                // 9-sided Material You Cookie
                0.84 + 0.16 * cos(9.0 * th)
            }
            FOUR_HEAD_COOKIE -> {
                // 4-head Material You Cookie / Clover
                0.80 + 0.20 * cos(4.0 * th + PI / 4.0)
            }
            ROUNDED_TRIANGLE -> {
                // Smooth 3-vertex rounded equilateral triangle
                val a = th - PI / 2.0
                0.78 + 0.175 * cos(3.0 * a) + 0.035 * cos(6.0 * a)
            }
            ROUNDED_SQUARE -> {
                // 1:1 Material You superellipse squircle
                val c = abs(cos(th))
                val s = abs(sin(th))
                0.85 / (c.pow(4.4) + s.pow(4.4)).pow(1.0 / 4.4)
            }
            CIRCLE -> 0.90
            SIX_COOKIE -> {
                // 6-head Material You Cookie
                0.83 + 0.17 * cos(6.0 * th)
            }
            SUNNY_COOKIE -> {
                // 12-head Material You Sunny Cookie
                0.86 + 0.14 * cos(12.0 * th)
            }
        }
    }
}

private class AiMorphingExpressiveShape(
    private val from: AiExpressiveShapeType,
    private val to: AiExpressiveShapeType,
    private val progress: Float,
    private val steps: Int = 180
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val count = steps.coerceAtLeast(90)
        val twoPi = 2.0 * PI
        val clampedT = progress.coerceIn(0f, 1f).toDouble()

        val baseRadius = min(size.width, size.height) * 0.485f
        val centerX = size.width * 0.5f
        val centerY = size.height * 0.5f

        val path = Path().apply {
            for (i in 0 until count) {
                val theta = (twoPi * i) / count
                val rFrom = from.radiusAt(theta)
                val rTo = to.radiusAt(theta)
                val r = (1.0 - clampedT) * rFrom + clampedT * rTo
                val radiusPx = (baseRadius * r).toFloat()
                val x = centerX + (radiusPx * cos(theta)).toFloat()
                val y = centerY + (radiusPx * sin(theta)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        return Outline.Generic(path)
    }
}

/**
 * 4 Large Floating Material You Expressive shapes with rich, visible multi-layer shadow + glow.
 * Idle: Large Pill, Cookie, Rounded Triangle, and 1:1 Rounded Square gently floating & breathing.
 * Generating: Reacts smoothly by spinning, subtly zooming in/out, moving in orbital waves,
 * and continuously morphing across Cookie, 4-Head Cookie, Circle, Rounded Triangle, 1:1 Shape, Pill, etc.
 */
@Composable
private fun FloatingExpressiveShapes(isGenerating: Boolean) {
    val activityFactor by animateFloatAsState(
        targetValue = if (isGenerating) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "floating_shapes_activity"
    )

    var spinPhase by remember { mutableFloatStateOf(0f) }
    var orbitPhase by remember { mutableFloatStateOf(0f) }
    var morphPhase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastFrameMs = -1L
        while (true) {
            withInfiniteAnimationFrameMillis { frameMs ->
                val dt = if (lastFrameMs < 0L) 0f else ((frameMs - lastFrameMs).coerceIn(0L, 64L) / 1000f)
                lastFrameMs = frameMs

                spinPhase = (spinPhase + dt * (14f + 82f * activityFactor)) % 3600f
                orbitPhase = (orbitPhase + dt * (1.35f + 2.15f * activityFactor)) % 628.318f
                morphPhase = (morphPhase + dt * (0.22f + 0.88f * activityFactor)) % 600f
            }
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val secondary = MaterialTheme.colorScheme.secondary
    val fourthColor = lerp(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.primary,
        if (isDark) 0.28f else 0.48f
    )

    val colors = listOf(primary, tertiary, secondary, fourthColor)

    val morphSequence = remember {
        listOf(
            AiExpressiveShapeType.PILL,
            AiExpressiveShapeType.COOKIE,
            AiExpressiveShapeType.FOUR_HEAD_COOKIE,
            AiExpressiveShapeType.ROUNDED_TRIANGLE,
            AiExpressiveShapeType.ROUNDED_SQUARE,
            AiExpressiveShapeType.CIRCLE,
            AiExpressiveShapeType.SIX_COOKIE,
            AiExpressiveShapeType.SUNNY_COOKIE
        )
    }

    val idleBaseShapes = remember {
        listOf(
            AiExpressiveShapeType.PILL,
            AiExpressiveShapeType.COOKIE,
            AiExpressiveShapeType.ROUNDED_TRIANGLE,
            AiExpressiveShapeType.ROUNDED_SQUARE
        )
    }
    val idleCompanionShapes = remember {
        listOf(
            AiExpressiveShapeType.FOUR_HEAD_COOKIE,
            AiExpressiveShapeType.SUNNY_COOKIE,
            AiExpressiveShapeType.SIX_COOKIE,
            AiExpressiveShapeType.FOUR_HEAD_COOKIE
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEachIndexed { index, shapeColor ->
                val phaseOffset = index * 1.57f

                // Smooth floating & orbital movement
                val verticalAmp = 4.5f + 6.5f * activityFactor
                val horizontalAmp = 1.5f + 5.5f * activityFactor
                val verticalMovement = sin((orbitPhase + phaseOffset).toDouble()).toFloat() * verticalAmp
                val horizontalMovement = cos((orbitPhase * 0.85f + phaseOffset).toDouble()).toFloat() * horizontalAmp

                // Subtle zooming in/out ("little not so much")
                val zoomAmp = 0.03f + 0.055f * activityFactor
                val currentScale = 1.0f + sin((orbitPhase * 1.15f + phaseOffset).toDouble()).toFloat() * zoomAmp

                // Smooth rotation: gentle rocking when idle, continuous expressive spinning when generating
                val idleRock = sin((orbitPhase * 0.6f + phaseOffset).toDouble()).toFloat() * 8f
                val direction = if (index % 2 == 0) 1f else -1f
                val speedMultiplier = 1f + (index % 3) * 0.12f
                val activeSpin = spinPhase * direction * speedMultiplier
                val rotationDegrees = idleRock * (1f - activityFactor) + activeSpin * activityFactor

                // Expressive shape selection & continuous morphing
                val expressiveShape = if (activityFactor < 0.05f) {
                    val idleMorph = ((sin((orbitPhase * 0.5f + phaseOffset).toDouble()).toFloat() + 1f) * 0.5f) * 0.28f
                    AiMorphingExpressiveShape(
                        from = idleBaseShapes[index % idleBaseShapes.size],
                        to = idleCompanionShapes[index % idleCompanionShapes.size],
                        progress = idleMorph
                    )
                } else {
                    val seqSize = morphSequence.size
                    val cyclePos = (morphPhase + index * 1.75f) % seqSize
                    val fromIdx = cyclePos.toInt() % seqSize
                    val toIdx = (fromIdx + 1) % seqSize
                    val rawT = cyclePos - cyclePos.toInt()
                    val smoothT = ((1.0 - cos(rawT * PI)) * 0.5).toFloat()
                    AiMorphingExpressiveShape(
                        from = morphSequence[fromIdx],
                        to = morphSequence[toIdx],
                        progress = smoothT
                    )
                }

                val highlightTint = lerp(shapeColor, Color.White, if (isDark) 0.22f else 0.28f)
                val deepShade = lerp(shapeColor, Color.Black, if (isDark) 0.18f else 0.12f)
                val glowAlpha = 0.52f + 0.22f * activityFactor

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .offset(x = horizontalMovement.dp, y = verticalMovement.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Layer 1: Wide expensive ambient glow halo
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .offset(y = 7.dp)
                            .graphicsLayer {
                                scaleX = currentScale * (1.12f + 0.08f * activityFactor)
                                scaleY = currentScale * (1.12f + 0.08f * activityFactor)
                            }
                            .blur(20.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        shapeColor.copy(alpha = glowAlpha),
                                        shapeColor.copy(alpha = glowAlpha * 0.48f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Layer 2: Shape-matched colored drop shadow bloom
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .offset(y = 6.dp)
                            .graphicsLayer {
                                scaleX = currentScale
                                scaleY = currentScale
                                rotationZ = rotationDegrees
                            }
                            .blur(10.dp)
                            .clip(expressiveShape)
                            .background(shapeColor.copy(alpha = 0.48f + 0.16f * activityFactor))
                    )

                    // Layer 3: Large hero Material You Expressive shape with rich elevation & specular finish
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .graphicsLayer {
                                scaleX = currentScale
                                scaleY = currentScale
                                rotationZ = rotationDegrees
                            }
                            .shadow(
                                elevation = (10f + 6f * activityFactor).dp,
                                shape = expressiveShape,
                                ambientColor = shapeColor,
                                spotColor = shapeColor
                            )
                            .clip(expressiveShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(highlightTint, shapeColor, deepShade)
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.38f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.10f)
                                    )
                                ),
                                shape = expressiveShape
                            )
                    )
                }
            }
        }
    }
}

/**
 * Horizontal scrollable line of thick, expressive Material You shortcut pills.
 * Each pill has a left-side 1:1 Material You expressive shape (Cookie, 4-Head Cookie,
 * Circle, Rounded Triangle, 1:1 Rounded Square, etc.) housing an icon, right-side text,
 * and rich Material You shape-morphing touch feedback.
 */
private data class VibeItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val baseShape: AiExpressiveShapeType,
    val morphedShape: AiExpressiveShapeType
)

@Composable
private fun ExpressiveQuickSelectRow(
    selectedPrompt: String,
    onPillClick: (String) -> Unit
) {
    val vibes = remember {
        listOf(
            VibeItem(
                label = "Feel good",
                icon = Icons.Rounded.Whatshot,
                baseShape = AiExpressiveShapeType.COOKIE,
                morphedShape = AiExpressiveShapeType.FOUR_HEAD_COOKIE
            ),
            VibeItem(
                label = "Upbeat morning",
                icon = Icons.Rounded.WbSunny,
                baseShape = AiExpressiveShapeType.FOUR_HEAD_COOKIE,
                morphedShape = AiExpressiveShapeType.SUNNY_COOKIE
            ),
            VibeItem(
                label = "Workout energy",
                icon = Icons.Rounded.FitnessCenter,
                baseShape = AiExpressiveShapeType.ROUNDED_TRIANGLE,
                morphedShape = AiExpressiveShapeType.SIX_COOKIE
            ),
            VibeItem(
                label = "Chill evening",
                icon = Icons.Rounded.NightsStay,
                baseShape = AiExpressiveShapeType.CIRCLE,
                morphedShape = AiExpressiveShapeType.FOUR_HEAD_COOKIE
            ),
            VibeItem(
                label = "Late night drive",
                icon = Icons.Rounded.DirectionsCar,
                baseShape = AiExpressiveShapeType.ROUNDED_SQUARE,
                morphedShape = AiExpressiveShapeType.COOKIE
            ),
            VibeItem(
                label = "Music to study",
                icon = Icons.Rounded.Headphones,
                baseShape = AiExpressiveShapeType.SIX_COOKIE,
                morphedShape = AiExpressiveShapeType.ROUNDED_TRIANGLE
            ),
            VibeItem(
                label = "Party hype",
                icon = Icons.Rounded.Celebration,
                baseShape = AiExpressiveShapeType.SUNNY_COOKIE,
                morphedShape = AiExpressiveShapeType.FOUR_HEAD_COOKIE
            ),
            VibeItem(
                label = "Acoustic coffee",
                icon = Icons.Rounded.MusicNote,
                baseShape = AiExpressiveShapeType.COOKIE,
                morphedShape = AiExpressiveShapeType.ROUNDED_SQUARE
            ),
            VibeItem(
                label = "Nostalgic 2000s",
                icon = Icons.Rounded.History,
                baseShape = AiExpressiveShapeType.FOUR_HEAD_COOKIE,
                morphedShape = AiExpressiveShapeType.CIRCLE
            ),
            VibeItem(
                label = "Piano peaceful",
                icon = Icons.Rounded.Piano,
                baseShape = AiExpressiveShapeType.ROUNDED_SQUARE,
                morphedShape = AiExpressiveShapeType.SIX_COOKIE
            ),
            VibeItem(
                label = "Deep focus",
                icon = Icons.Rounded.Star,
                baseShape = AiExpressiveShapeType.ROUNDED_TRIANGLE,
                morphedShape = AiExpressiveShapeType.COOKIE
            ),
            VibeItem(
                label = "Radio hits",
                icon = Icons.Rounded.GraphicEq,
                baseShape = AiExpressiveShapeType.CIRCLE,
                morphedShape = AiExpressiveShapeType.SUNNY_COOKIE
            )
        )
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(vibes, key = { _, item -> item.label }) { index, vibe ->
            val isSelected = selectedPrompt.trim().equals(vibe.label, ignoreCase = true)
            ExpressiveQuickSelectPill(
                vibe = vibe,
                index = index,
                isSelected = isSelected,
                onClick = { onPillClick(vibe.label) }
            )
        }
    }
}

@Composable
private fun ExpressiveQuickSelectPill(
    vibe: VibeItem,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tap pulse animation so even a quick tap plays a full, lush Material You shape-morphing effect
    val tapPulse = remember { Animatable(0f) }

    val activeMorphTarget = when {
        isPressed -> 1f
        tapPulse.value > 0.01f -> tapPulse.value
        isSelected -> 0.65f
        else -> 0f
    }

    val morphFraction by animateFloatAsState(
        targetValue = activeMorphTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pill_morph_fraction"
    )

    // Bouncy touch scale feedback
    val pillScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.93f
            tapPulse.value > 0.5f -> 1.03f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pill_scale"
    )

    // True full stadium pill (32.dp radius on 64.dp height) when idle,
    // morphing into an expressive asymmetric Material You contour on touch/selection
    val primaryRadius = (32f - 14f * morphFraction).coerceAtLeast(14f).dp
    val secondaryRadius = 32.dp

    val pillShape = if (index % 2 == 0) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTL = primaryRadius,
            smoothnessAsPercentTL = 60,
            cornerRadiusTR = secondaryRadius,
            smoothnessAsPercentTR = 60,
            cornerRadiusBL = secondaryRadius,
            smoothnessAsPercentBL = 60,
            cornerRadiusBR = primaryRadius,
            smoothnessAsPercentBR = 60
        )
    } else {
        AbsoluteSmoothCornerShape(
            cornerRadiusTL = secondaryRadius,
            smoothnessAsPercentTL = 60,
            cornerRadiusTR = primaryRadius,
            smoothnessAsPercentTR = 60,
            cornerRadiusBL = primaryRadius,
            smoothnessAsPercentBL = 60,
            cornerRadiusBR = secondaryRadius,
            smoothnessAsPercentBR = 60
        )
    }

    // Left-side 1:1 Material You expressive shape (Cookie, 4-Head Cookie, Circle, Rounded Triangle, 1:1 Square, etc.)
    val leftIconExpressiveShape = remember(vibe.baseShape, vibe.morphedShape, morphFraction) {
        AiMorphingExpressiveShape(
            from = vibe.baseShape,
            to = vibe.morphedShape,
            progress = morphFraction
        )
    }

    val isHighlighted = isPressed || isSelected || tapPulse.value > 0.25f

    val pillContainerColor by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(180),
        label = "pill_bg_color"
    )

    val badgeContainerColor by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        animationSpec = tween(180),
        label = "pill_badge_color"
    )

    val badgeIconTint by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        },
        animationSpec = tween(180),
        label = "pill_icon_tint"
    )

    Surface(
        onClick = {
            onClick()
            scope.launch {
                tapPulse.snapTo(0.25f)
                tapPulse.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                tapPulse.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        },
        interactionSource = interactionSource,
        shape = pillShape,
        color = pillContainerColor,
        shadowElevation = if (isHighlighted) 4.dp else 2.dp,
        modifier = Modifier
            .height(64.dp)
            .graphicsLayer {
                scaleX = pillScale
                scaleY = pillScale
            }
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 22.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Icon inside a 1:1 Material You expressive shape with morphing & rotation
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .graphicsLayer {
                        rotationZ = morphFraction * 26f * (if (index % 2 == 0) 1f else -1f)
                        val iconScale = 1f + 0.08f * morphFraction
                        scaleX = iconScale
                        scaleY = iconScale
                    }
                    .shadow(
                        elevation = if (isHighlighted) 4.dp else 1.dp,
                        shape = leftIconExpressiveShape,
                        ambientColor = badgeContainerColor,
                        spotColor = badgeContainerColor
                    )
                    .clip(leftIconExpressiveShape)
                    .background(badgeContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vibe.icon,
                    contentDescription = null,
                    tint = badgeIconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right side: Bold text label
            Text(
                text = vibe.label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = if (isHighlighted) {
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
            .padding(top = 4.dp, bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Headline (the 4 floating Material You expressive shapes remain mounted right above this view)
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
