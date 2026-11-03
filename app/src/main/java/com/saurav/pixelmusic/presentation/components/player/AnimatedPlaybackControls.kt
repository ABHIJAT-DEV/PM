package com.saurav.pixelmusic.presentation.components.player

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.saurav.pixelmusic.presentation.components.LocalMaterialTheme
import com.saurav.pixelmusic.ui.theme.LocalLiquidGlassBlurIntensity
import com.saurav.pixelmusic.ui.theme.LocalLiquidGlassRefractionIntensity
import kotlinx.coroutines.delay
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

private enum class PlaybackButtonType { NONE, PREVIOUS, PLAY_PAUSE, NEXT }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedPlaybackControls(
    isPlayingProvider: () -> Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 90.dp,
    baseWeight: Float = 1f,
    expansionWeight: Float = 1.1f,
    compressionWeight: Float = 0.65f,
    pressAnimationSpec: AnimationSpec<Float>,
    releaseDelay: Long = 220L,
    playPauseCornerPlaying: Dp = 26.dp,
    playPauseCornerPaused: Dp = 26.dp,
    colorOtherButtons: Color = LocalMaterialTheme.current.onPrimary,
    colorPlayPause: Color = LocalMaterialTheme.current.primary,
    tintPlayPauseIcon: Color = LocalMaterialTheme.current.onPrimary,
    tintOtherIcons: Color = LocalMaterialTheme.current.primary,
    colorPreviousButton: Color = colorOtherButtons,
    colorNextButton: Color = colorOtherButtons,
    tintPreviousIcon: Color = tintOtherIcons,
    tintNextIcon: Color = tintOtherIcons,
    playPauseIconSize: Dp = 36.dp,
    iconSize: Dp = 32.dp,
    isLiquidGlass: Boolean = false,
    backdrop: Backdrop? = null,
    morphTracker: ArtworkMorphTracker? = null,
    expansionFractionProvider: (() -> Float)? = null,
    rootCoordsHolder: LayoutCoordsHolder? = null,
    containerWidthPxProvider: () -> Float = { 0f },
) {
    val density = LocalDensity.current
    val miniBtnSizePx = remember(density) { with(density) { 36.dp.toPx() } }
    val miniBtnTopPx = remember(density) { with(density) { 14.dp.toPx() } }
    val miniEndPadPx = remember(density) { with(density) { 12.dp.toPx() } }
    val miniGapPx = remember(density) { with(density) { 8.dp.toPx() } }
    val miniIconSizePx = remember(density) { with(density) { 22.dp.toPx() } }

    val prevBounds = remember { MorphElementBounds() }
    val playBounds = remember { MorphElementBounds() }
    val nextBounds = remember { MorphElementBounds() }
    val isPlaying = isPlayingProvider()
    var lastClicked by remember { mutableStateOf<PlaybackButtonType?>(null) }
    val latestIsPlayingProvider by rememberUpdatedState(newValue = isPlayingProvider)
    val latestLastClicked by rememberUpdatedState(newValue = lastClicked)
    val isPlayPauseLocked =
        lastClicked == PlaybackButtonType.NEXT || lastClicked == PlaybackButtonType.PREVIOUS
    var playPauseVisualState by remember { mutableStateOf(isPlaying) }
    var pendingPlayPauseState by remember { mutableStateOf<Boolean?>(null) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(lastClicked) {
        if (lastClicked != null) {
            delay(releaseDelay)
            lastClicked = null
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            pendingPlayPauseState = true
            return@LaunchedEffect
        }

        val shouldDelay = latestLastClicked != PlaybackButtonType.PLAY_PAUSE
        if (shouldDelay) {
            delay(releaseDelay)
        }
        if (!latestIsPlayingProvider()) {
            pendingPlayPauseState = false
        }
    }

    LaunchedEffect(isPlayPauseLocked, pendingPlayPauseState) {
        if (!isPlayPauseLocked) {
            pendingPlayPauseState?.let {
                playPauseVisualState = it
                pendingPlayPauseState = null
            }
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val isGlassActive = isLiquidGlass && backdrop != null
    val glassBlurIntensity = LocalLiquidGlassBlurIntensity.current
    val glassRefractionIntensity = LocalLiquidGlassRefractionIntensity.current
    val glassRimBorder = remember(isLiquidGlass, isDark) {
        if (!isLiquidGlass) null
        else BorderStroke(
            0.75.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = if (isDark) 0.28f else 0.45f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            fun weightFor(button: PlaybackButtonType): Float = when (lastClicked) {
                button -> expansionWeight
                null -> baseWeight
                else -> compressionWeight
            }

            val prevShape = remember(morphTracker, expansionFractionProvider, miniBtnSizePx) {
                MorphingTransportButtonShape(
                    miniCornerDp = 18.dp,
                    fullCornerDpProvider = { 40.dp },
                    miniBtnSizePx = miniBtnSizePx,
                    morphTracker = morphTracker,
                    expansionFractionProvider = expansionFractionProvider
                )
            }
            val prevWeight by animateFloatAsState(
                targetValue = weightFor(PlaybackButtonType.PREVIOUS),
                animationSpec = pressAnimationSpec,
                label = "prevWeight"
            )
            Box(
                modifier = Modifier
                    .weight(prevWeight)
                    .fillMaxHeight()
                    .captureUntransformedBounds(rootCoordsHolder, prevBounds)
                    .graphicsLayer {
                        if (morphTracker != null && expansionFractionProvider != null) {
                            val s = morphTracker.evaluate(expansionFractionProvider())
                            val cw = rootCoordsHolder?.widthPx?.takeIf { it > 0f }
                                ?: containerWidthPxProvider().takeIf { it > 0f }
                                ?: (size.width * 3f)
                            val miniNextLeftPx = (cw - miniEndPadPx - miniBtnSizePx).coerceAtLeast(0f)
                            val miniPlayLeftPx = (miniNextLeftPx - miniGapPx - miniBtnSizePx).coerceAtLeast(0f)
                            val miniPrevLeftPx = (miniPlayLeftPx - miniGapPx - miniBtnSizePx).coerceAtLeast(0f)
                            val destX = prevBounds.offset.x
                            val destY = prevBounds.offset.y
                            val curW = androidx.compose.ui.util.lerp(miniBtnSizePx, size.width, s.sizeT)
                            val curH = androidx.compose.ui.util.lerp(miniBtnSizePx, size.height, s.sizeT)
                            transformOrigin = TransformOrigin(0f, 0f)
                            scaleX = if (size.width > 0f) curW / size.width else 1f
                            scaleY = if (size.height > 0f) curH / size.height else 1f
                            translationX = (miniPrevLeftPx - destX) * (1f - s.posT)
                            translationY = (miniBtnTopPx - destY) * (1f - s.posT)
                        }
                    }
                    .then(
                        if (isGlassActive) {
                            Modifier
                                .clip(prevShape)
                                .drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { prevShape },
                                    effects = {
                                        vibrancy()
                                        val blurRadius = (12.dp.toPx() * glassBlurIntensity).coerceAtLeast(1f)
                                        blur(blurRadius)
                                    },
                                    onDrawSurface = {
                                        drawRect(if (isDark) Color(0xEE1E1E22) else Color(0xEEFFFFFF))
                                        drawRect(colorPreviousButton, blendMode = BlendMode.Hue)
                                        drawRect(colorPreviousButton.copy(alpha = 0.95f))
                                    }
                                )
                        } else {
                            Modifier
                                .clip(prevShape)
                                .background(if (isLiquidGlass) colorPreviousButton.copy(alpha = 0.94f) else colorPreviousButton)
                                .then(if (glassRimBorder != null) Modifier.border(glassRimBorder, prevShape) else Modifier)
                        }
                    )
                    .clickable {
                        lastClicked = PlaybackButtonType.PREVIOUS
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPrevious()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Anterior",
                    tint = tintPreviousIcon,
                    modifier = Modifier
                        .size(iconSize)
                        .graphicsLayer {
                            val bw = prevBounds.size.width.toFloat()
                            val bh = prevBounds.size.height.toFloat()
                            if (morphTracker != null && expansionFractionProvider != null && bw > 0f && bh > 0f) {
                                val s = morphTracker.evaluate(expansionFractionProvider())
                                val parentScaleX = androidx.compose.ui.util.lerp(miniBtnSizePx, bw, s.sizeT) / bw
                                val parentScaleY = androidx.compose.ui.util.lerp(miniBtnSizePx, bh, s.sizeT) / bh
                                val fullIconPx = iconSize.toPx().coerceAtLeast(1f)
                                val desiredScale = androidx.compose.ui.util.lerp(miniIconSizePx / fullIconPx, 1f, s.sizeT)
                                scaleX = if (parentScaleX > 0.001f) desiredScale / parentScaleX else 1f
                                scaleY = if (parentScaleY > 0.001f) desiredScale / parentScaleY else 1f
                            }
                        }
                )
            }

            val playWeight by animateFloatAsState(
                targetValue = weightFor(PlaybackButtonType.PLAY_PAUSE),
                animationSpec = pressAnimationSpec,
                label = "playWeight"
            )
            val playCorner by animateDpAsState(
                targetValue = if (!playPauseVisualState) playPauseCornerPlaying else playPauseCornerPaused,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "playCorner"
            )
            val miniPlayCornerDp = if (playPauseVisualState) 11.dp else 18.dp
            val playShape = remember(morphTracker, expansionFractionProvider, miniBtnSizePx, miniPlayCornerDp) {
                MorphingTransportButtonShape(
                    miniCornerDp = miniPlayCornerDp,
                    fullCornerDpProvider = { playCorner },
                    miniBtnSizePx = miniBtnSizePx,
                    morphTracker = morphTracker,
                    expansionFractionProvider = expansionFractionProvider
                )
            }
            val playGlassCornerShape = playShape
            Box(
                modifier = Modifier
                    .weight(playWeight)
                    .fillMaxHeight()
                    .captureUntransformedBounds(rootCoordsHolder, playBounds)
                    .graphicsLayer {
                        if (morphTracker != null && expansionFractionProvider != null) {
                            val s = morphTracker.evaluate(expansionFractionProvider())
                            val cw = rootCoordsHolder?.widthPx?.takeIf { it > 0f }
                                ?: containerWidthPxProvider().takeIf { it > 0f }
                                ?: (size.width * 3f)
                            val miniNextLeftPx = (cw - miniEndPadPx - miniBtnSizePx).coerceAtLeast(0f)
                            val miniPlayLeftPx = (miniNextLeftPx - miniGapPx - miniBtnSizePx).coerceAtLeast(0f)
                            val destX = playBounds.offset.x
                            val destY = playBounds.offset.y
                            val curW = androidx.compose.ui.util.lerp(miniBtnSizePx, size.width, s.sizeT)
                            val curH = androidx.compose.ui.util.lerp(miniBtnSizePx, size.height, s.sizeT)
                            transformOrigin = TransformOrigin(0f, 0f)
                            scaleX = if (size.width > 0f) curW / size.width else 1f
                            scaleY = if (size.height > 0f) curH / size.height else 1f
                            translationX = (miniPlayLeftPx - destX) * (1f - s.posT)
                            translationY = (miniBtnTopPx - destY) * (1f - s.posT)
                        }
                    }
                    .then(
                        if (isGlassActive) {
                            Modifier
                                .clip(playGlassCornerShape)
                                .drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { playGlassCornerShape },
                                    effects = {
                                        vibrancy()
                                        val blurRadius = (12.dp.toPx() * glassBlurIntensity).coerceAtLeast(1f)
                                        blur(blurRadius)
                                    },
                                    onDrawSurface = {
                                        drawRect(if (isDark) Color(0xEE1E1E22) else Color(0xEEFFFFFF))
                                        drawRect(colorPlayPause, blendMode = BlendMode.Hue)
                                        drawRect(colorPlayPause.copy(alpha = 0.95f))
                                    }
                                )
                        } else {
                            Modifier
                                .clip(playShape)
                                .background(if (isLiquidGlass) colorPlayPause.copy(alpha = 0.95f) else colorPlayPause)
                                .then(if (glassRimBorder != null) Modifier.border(glassRimBorder, playShape) else Modifier)
                        }
                    )
                    .clickable {
                        lastClicked = PlaybackButtonType.PLAY_PAUSE
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPlayPause()
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        val bw = playBounds.size.width.toFloat()
                        val bh = playBounds.size.height.toFloat()
                        if (morphTracker != null && expansionFractionProvider != null && bw > 0f && bh > 0f) {
                            val s = morphTracker.evaluate(expansionFractionProvider())
                            val parentScaleX = androidx.compose.ui.util.lerp(miniBtnSizePx, bw, s.sizeT) / bw
                            val parentScaleY = androidx.compose.ui.util.lerp(miniBtnSizePx, bh, s.sizeT) / bh
                            val fullIconPx = playPauseIconSize.toPx().coerceAtLeast(1f)
                            val desiredScale = androidx.compose.ui.util.lerp(miniIconSizePx / fullIconPx, 1f, s.sizeT)
                            scaleX = if (parentScaleX > 0.001f) desiredScale / parentScaleX else 1f
                            scaleY = if (parentScaleY > 0.001f) desiredScale / parentScaleY else 1f
                        }
                    }
                ) {
                    MorphingPlayPauseIcon(
                        isPlaying = playPauseVisualState,
                        tint = tintPlayPauseIcon,
                        size = playPauseIconSize
                    )
                }
            }

            val nextShape = remember(morphTracker, expansionFractionProvider, miniBtnSizePx) {
                MorphingTransportButtonShape(
                    miniCornerDp = 18.dp,
                    fullCornerDpProvider = { 40.dp },
                    miniBtnSizePx = miniBtnSizePx,
                    morphTracker = morphTracker,
                    expansionFractionProvider = expansionFractionProvider
                )
            }
            val nextWeight by animateFloatAsState(
                targetValue = weightFor(PlaybackButtonType.NEXT),
                animationSpec = pressAnimationSpec,
                label = "nextWeight"
            )
            Box(
                modifier = Modifier
                    .weight(nextWeight)
                    .fillMaxHeight()
                    .captureUntransformedBounds(rootCoordsHolder, nextBounds)
                    .graphicsLayer {
                        if (morphTracker != null && expansionFractionProvider != null) {
                            val s = morphTracker.evaluate(expansionFractionProvider())
                            val cw = rootCoordsHolder?.widthPx?.takeIf { it > 0f }
                                ?: containerWidthPxProvider().takeIf { it > 0f }
                                ?: (size.width * 3f)
                            val miniNextLeftPx = (cw - miniEndPadPx - miniBtnSizePx).coerceAtLeast(0f)
                            val destX = nextBounds.offset.x
                            val destY = nextBounds.offset.y
                            val curW = androidx.compose.ui.util.lerp(miniBtnSizePx, size.width, s.sizeT)
                            val curH = androidx.compose.ui.util.lerp(miniBtnSizePx, size.height, s.sizeT)
                            transformOrigin = TransformOrigin(0f, 0f)
                            scaleX = if (size.width > 0f) curW / size.width else 1f
                            scaleY = if (size.height > 0f) curH / size.height else 1f
                            translationX = (miniNextLeftPx - destX) * (1f - s.posT)
                            translationY = (miniBtnTopPx - destY) * (1f - s.posT)
                        }
                    }
                    .then(
                        if (isGlassActive) {
                            Modifier
                                .clip(nextShape)
                                .drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { nextShape },
                                    effects = {
                                        vibrancy()
                                        val blurRadius = (12.dp.toPx() * glassBlurIntensity).coerceAtLeast(1f)
                                        blur(blurRadius)
                                    },
                                    onDrawSurface = {
                                        drawRect(if (isDark) Color(0xEE1E1E22) else Color(0xEEFFFFFF))
                                        drawRect(colorNextButton, blendMode = BlendMode.Hue)
                                        drawRect(colorNextButton.copy(alpha = 0.95f))
                                    }
                                )
                        } else {
                            Modifier
                                .clip(nextShape)
                                .background(if (isLiquidGlass) colorNextButton.copy(alpha = 0.94f) else colorNextButton)
                                .then(if (glassRimBorder != null) Modifier.border(glassRimBorder, nextShape) else Modifier)
                        }
                    )
                    .clickable {
                        lastClicked = PlaybackButtonType.NEXT
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNext()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Siguiente",
                    tint = tintNextIcon,
                    modifier = Modifier
                        .size(iconSize)
                        .graphicsLayer {
                            val bw = nextBounds.size.width.toFloat()
                            val bh = nextBounds.size.height.toFloat()
                            if (morphTracker != null && expansionFractionProvider != null && bw > 0f && bh > 0f) {
                                val s = morphTracker.evaluate(expansionFractionProvider())
                                val parentScaleX = androidx.compose.ui.util.lerp(miniBtnSizePx, bw, s.sizeT) / bw
                                val parentScaleY = androidx.compose.ui.util.lerp(miniBtnSizePx, bh, s.sizeT) / bh
                                val fullIconPx = iconSize.toPx().coerceAtLeast(1f)
                                val desiredScale = androidx.compose.ui.util.lerp(miniIconSizePx / fullIconPx, 1f, s.sizeT)
                                scaleX = if (parentScaleX > 0.001f) desiredScale / parentScaleX else 1f
                                scaleY = if (parentScaleY > 0.001f) desiredScale / parentScaleY else 1f
                            }
                        }
                )
            }
        }
    }
}

private class MorphingTransportButtonShape(
    private val miniCornerDp: Dp,
    private val fullCornerDpProvider: () -> Dp,
    private val miniBtnSizePx: Float,
    private val morphTracker: ArtworkMorphTracker?,
    private val expansionFractionProvider: (() -> Float)?
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width.coerceAtLeast(1f)
        val h = size.height.coerceAtLeast(1f)
        val fullCornerPx = with(density) { fullCornerDpProvider().toPx() }
        if (morphTracker == null || expansionFractionProvider == null) {
            val r = fullCornerPx.coerceAtMost(minOf(w, h) * 0.5f)
            return Outline.Rounded(RoundRect(Rect(0f, 0f, w, h), CornerRadius(r, r)))
        }
        val t = morphTracker.evaluate(expansionFractionProvider()).sizeT
        val miniCornerPx = with(density) { miniCornerDp.toPx() }
        val targetVisualR = androidx.compose.ui.util.lerp(miniCornerPx, fullCornerPx, t)
        val scaleX = (androidx.compose.ui.util.lerp(miniBtnSizePx, w, t) / w).coerceAtLeast(0.01f)
        val scaleY = (androidx.compose.ui.util.lerp(miniBtnSizePx, h, t) / h).coerceAtLeast(0.01f)
        val rx = (targetVisualR / scaleX).coerceIn(0f, w * 0.5f)
        val ry = (targetVisualR / scaleY).coerceIn(0f, h * 0.5f)
        return Outline.Rounded(RoundRect(Rect(0f, 0f, w, h), CornerRadius(rx, ry)))
    }
}

@Composable
private fun MorphingPlayPauseIcon(
    isPlaying: Boolean,
    tint: Color,
    size: Dp,
) {
    Crossfade(
        targetState = isPlaying,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "playPauseCrossfade"
    ) { playing ->
        Icon(
            imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (playing) "Pausar" else "Reproducir",
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}
