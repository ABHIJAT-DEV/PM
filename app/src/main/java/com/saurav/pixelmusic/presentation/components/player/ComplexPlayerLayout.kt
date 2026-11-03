package com.saurav.pixelmusic.presentation.components.player

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurav.pixelmusic.R
import com.saurav.pixelmusic.data.model.Artist
import com.saurav.pixelmusic.data.model.Lyrics
import com.saurav.pixelmusic.data.model.Song
import com.saurav.pixelmusic.data.model.SyncedLine
import com.saurav.pixelmusic.data.preferences.AlbumArtQuality
import com.saurav.pixelmusic.presentation.components.LocalMaterialTheme
import com.saurav.pixelmusic.presentation.components.SmartImage
import com.saurav.pixelmusic.presentation.components.clusterSyncedWords
import com.saurav.pixelmusic.presentation.components.sanitizeLyricLineText
import com.saurav.pixelmusic.presentation.components.sanitizeSyncedWords
import com.saurav.pixelmusic.presentation.components.scoped.rememberSmoothProgress
import com.saurav.pixelmusic.presentation.viewmodel.PlayerSheetState
import com.saurav.pixelmusic.ui.theme.GoogleSansRounded
import com.saurav.pixelmusic.ui.theme.LocalIsBlackAndWhiteTheme
import com.saurav.pixelmusic.ui.theme.LocalPixelMusicDarkTheme
import com.saurav.pixelmusic.utils.ThumbnailUrlUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

// Visual lead-in (ms) so the spring pop-in animation starts slightly before the vocal transient
// and lands right as the singer pronounces the word.
private const val VOCAL_ONSET_LEAD_IN_MS = 85L

// Pushes the lowest point (M) of the circular arch to the left side: (C--M--------C)
private const val ARCH_CENTER_X_FRACTION = 0.25f

private val FastFunctionWords = setOf(
    "a", "an", "the", "to", "in", "on", "of", "at", "by", "or", "is", "it", "as", "be",
    "ka", "ke", "ki", "ko", "se", "ye", "ya", "na", "ho", "to", "bhi", "aur", "ek", "ne", "pe"
)

private val VowelGroupRegex = Regex("[aeiouyAEIOUY]+")
private val SustainedVowelRegex = Regex("(?i)(aa|ee|oo|ai|au|ei|ou|aie|gae|gaye|hai|haan|jaan|sha|dil|chain)")

private data class AsymmetricArchCircle(
    val cx: Float,
    val cy: Float,
    val radius: Float,
    val rightEdgeY: Float,
    val leftEdgeY: Float,
    val alphaRightDeg: Float,
    val alphaLeftDeg: Float
)

/**
 * Computes the circle geometry where the lowest point (M) of the arch is shifted to the left
 * at `cx = widthPx * centerXFraction` (`C--M--------C`), and the right edge rises by `rightRisePx`.
 */
private fun computeAsymmetricArchCircle(
    widthPx: Float,
    bottomVertexYPx: Float,
    rightRisePx: Float,
    centerXFraction: Float = ARCH_CENTER_X_FRACTION
): AsymmetricArchCircle {
    val w = widthPx.coerceAtLeast(1f)
    val cx = w * centerXFraction
    val dxRight = (w - cx).coerceAtLeast(1f)
    val sRight = rightRisePx.coerceAtLeast(16f)

    // Circle passing through lowest vertex (cx, bottomVertexYPx) and right edge (w, bottomVertexYPx - sRight)
    val radius = (sRight / 2f) + ((dxRight * dxRight) / (2f * sRight))
    val cy = bottomVertexYPx - radius

    val sinRight = (dxRight / radius).coerceIn(-1f, 1f)
    val sinLeft = (cx / radius).coerceIn(-1f, 1f)
    val alphaRightDeg = Math.toDegrees(asin(sinRight.toDouble())).toFloat()
    val alphaLeftDeg = Math.toDegrees(asin(sinLeft.toDouble())).toFloat()

    val rightEdgeY = bottomVertexYPx - sRight
    val leftEdgeY = cy + sqrt((radius * radius - cx * cx).coerceAtLeast(0f))

    return AsymmetricArchCircle(
        cx = cx,
        cy = cy,
        radius = radius,
        rightEdgeY = rightEdgeY,
        leftEdgeY = leftEdgeY,
        alphaRightDeg = alphaRightDeg,
        alphaLeftDeg = alphaLeftDeg
    )
}

internal data class ArtworkMorphSample(
    val rawFraction: Float,
    val sizeT: Float,
    val posT: Float,
    val overshootScale: Float,
    val nonArtAlpha: Float
)

/**
 * Tracks drag direction to provide:
 * 1. When opening (dragging up): brisk expansion with a subtle overshoot scale ("getting bigger then sitting in place").
 * 2. When closing (dragging down): smooth deceleration curve ("getting slower and going to sit in the mini player").
 * Blended continuously by travel delta so reversing direction mid-gesture never jumps.
 */
internal class ArtworkMorphTracker {
    private var lastFraction: Float = -1f
    private var directionBlend: Float = 1f

    fun evaluate(rawFraction: Float): ArtworkMorphSample {
        val f = rawFraction.coerceIn(0f, 1f)
        if (lastFraction < 0f) {
            lastFraction = f
            directionBlend = if (f < 0.5f) 1f else 0f
        } else {
            val delta = f - lastFraction
            if (abs(delta) > 0.0004f) {
                directionBlend = (directionBlend + delta * 5.2f).coerceIn(0f, 1f)
                lastFraction = f
            }
        }

        val expandSizeT = 1f - (1f - f).pow(1.85f)
        val collapseSizeT = f.pow(1.75f)
        val sizeT = androidx.compose.ui.util.lerp(collapseSizeT, expandSizeT, directionBlend).coerceIn(0f, 1f)

        val expandPosT = 1f - (1f - f).pow(1.65f)
        val collapsePosT = f.pow(1.55f)
        val posT = androidx.compose.ui.util.lerp(collapsePosT, expandPosT, directionBlend).coerceIn(0f, 1f)

        val overshootScale = if (f in 0.02f..0.995f) {
            val bell = sin(f * Math.PI.toFloat()).coerceAtLeast(0f)
            1f + (0.055f * bell * directionBlend)
        } else {
            1f
        }

        val nonArtAlpha = ((f - 0.20f) / 0.80f).coerceIn(0f, 1f)

        return ArtworkMorphSample(
            rawFraction = f,
            sizeT = sizeT,
            posT = posT,
            overshootScale = overshootScale,
            nonArtAlpha = nonArtAlpha
        )
    }
}

/**
 * Custom shape that smoothly morphs from the 44.dp MiniPlayer Circle (at morphProgress = 0f)
 * into the left-shifted circular arch (C--M--------C) at morphProgress = 1f.
 */
private class MorphingScoopedArcBottomShape(
    private val rightRisePx: Float,
    private val morphProgressProvider: () -> Float,
    private val targetCenterXFraction: Float = ARCH_CENTER_X_FRACTION
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width.coerceAtLeast(1f)
        val h = size.height.coerceAtLeast(1f)
        val t = morphProgressProvider().coerceIn(0f, 1f)

        val currentCenterXFraction = androidx.compose.ui.util.lerp(0.5f, targetCenterXFraction, t)
        val currentRightRisePx = androidx.compose.ui.util.lerp(h * 0.5f, rightRisePx, t)
            .coerceIn(2f, (h - 1f).coerceAtLeast(2f))

        val circle = computeAsymmetricArchCircle(
            widthPx = w,
            bottomVertexYPx = h,
            rightRisePx = currentRightRisePx,
            centerXFraction = currentCenterXFraction
        )

        val maxTopR = minOf(w * 0.5f, circle.leftEdgeY, circle.rightEdgeY).coerceAtLeast(0f)
        val topCornerR = (maxTopR * (1f - t).pow(1.15f)).coerceIn(0f, maxTopR)

        val path = Path().apply {
            if (topCornerR > 0.5f) {
                moveTo(0f, topCornerR)
                arcTo(
                    rect = Rect(0f, 0f, topCornerR * 2f, topCornerR * 2f),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(w - topCornerR, 0f)
                arcTo(
                    rect = Rect(w - topCornerR * 2f, 0f, w, topCornerR * 2f),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            } else {
                moveTo(0f, 0f)
                lineTo(w, 0f)
            }

            lineTo(w, circle.rightEdgeY)
            arcTo(
                rect = Rect(
                    left = circle.cx - circle.radius,
                    top = circle.cy - circle.radius,
                    right = circle.cx + circle.radius,
                    bottom = circle.cy + circle.radius
                ),
                startAngleDegrees = 90f - circle.alphaRightDeg,
                sweepAngleDegrees = circle.alphaRightDeg + circle.alphaLeftDeg,
                forceMoveTo = false
            )
            if (topCornerR > 0.5f) {
                lineTo(0f, topCornerR)
            } else {
                lineTo(0f, 0f)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

private enum class VerticalControlButton {
    MORE, LIKE, PREVIOUS, PLAY_PAUSE, NEXT
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComplexPlayerLayout(
    song: Song,
    currentSongArtists: List<Artist>,
    albumArtQuality: AlbumArtQuality,
    aodScreenEnabled: Boolean,
    onEnterAodScreen: () -> Unit,
    expansionFractionProvider: () -> Float,
    currentSheetState: PlayerSheetState,
    allowRealtimeUpdates: Boolean,
    currentPositionProvider: () -> Long,
    totalDurationValue: Long,
    isPlayingProvider: () -> Boolean,
    isFavoriteProvider: () -> Boolean,
    lyricsProvider: () -> Lyrics?,
    playbackPositionFlow: StateFlow<Long>,
    lyricsSyncOffset: Int,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onCollapse: () -> Unit,
    onLyricsClick: () -> Unit,
    onArtistClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val baseTheme = LocalMaterialTheme.current
    val isDarkTheme = LocalPixelMusicDarkTheme.current
    val isBwTheme = LocalIsBlackAndWhiteTheme.current
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val optimizedAlbumArtUri = remember(song.id, albumArtQuality) {
        ThumbnailUrlUtils.optimizeArtworkUrl(song.albumArtUriString, albumArtQuality)
            ?: song.albumArtUriString.orEmpty()
    }

    // Material You dominant color extraction from cover art
    val fallbackBg = baseTheme.surface
    var extractedColor by remember { mutableStateOf(fallbackBg) }
    LaunchedEffect(optimizedAlbumArtUri, isDarkTheme) {
        extractedColor = extractDominantColor(context, optimizedAlbumArtUri, fallbackBg, isDarkTheme)
    }

    val targetWash = if (isBwTheme) {
        fallbackBg
    } else {
        lerp(
            baseTheme.primaryContainer,
            extractedColor,
            if (isDarkTheme) 0.62f else 0.48f
        )
    }
    val washColor by animateColorAsState(
        targetValue = targetWash,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "complexWashColor"
    )
    val deepWashColor = remember(washColor, isDarkTheme) {
        if (isDarkTheme) {
            lerp(washColor, Color(0xFF0A0A0E), 0.48f)
        } else {
            lerp(washColor, Color.White, 0.28f)
        }
    }

    val contentColor = if (isDarkTheme) Color.White else baseTheme.onPrimaryContainer
    val accentColor = baseTheme.primary
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Reliable non-empty song title & artist resolution
    val resolvedTitle = remember(song.id, song.title) {
        song.title.takeIf { it.isNotBlank() } ?: "Unknown Title"
    }
    val resolvedArtist = remember(song.id, song.displayArtist, song.artist, currentSongArtists) {
        song.displayArtist.takeIf { it.isNotBlank() }
            ?: currentSongArtists
                .mapNotNull { it.name.takeIf(String::isNotBlank) }
                .joinToString(", ")
                .takeIf { it.isNotBlank() }
            ?: song.artist.takeIf { it.isNotBlank() }
            ?: "Unknown Artist"
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(deepWashColor)
    ) {
        val screenWidthDp = maxWidth
        val screenHeightDp = maxHeight
        // Lowest point M of the arch sits at ~52% height and 25% from the left (C--M--------C)
        val artHeightDp = screenHeightDp * 0.52f
        val rightRiseDp = (screenWidthDp * 0.23f).coerceIn(74.dp, 108.dp)
        val rightRisePx = with(density) { rightRiseDp.toPx() }
        val screenWidthPx = with(density) { screenWidthDp.toPx() }
        val artHeightPx = with(density) { artHeightDp.toPx() }
        val miniArtSizePx = with(density) { 44.dp.toPx() }
        val miniArtOffsetPx = with(density) { 10.dp.toPx() }
        val slideOffsetPx = with(density) { 24.dp.toPx() }

        val morphTracker = remember { ArtworkMorphTracker() }
        val scoopedShape = remember(rightRisePx, morphTracker, expansionFractionProvider) {
            MorphingScoopedArcBottomShape(
                rightRisePx = rightRisePx,
                morphProgressProvider = {
                    morphTracker.evaluate(expansionFractionProvider()).sizeT
                },
                targetCenterXFraction = ARCH_CENTER_X_FRACTION
            )
        }

        // =====================================================================
        // LAYER 1: Heavy Frosted-Glass / Blurred Cover Wash in Lower Half
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = morphTracker.evaluate(expansionFractionProvider()).nonArtAlpha
                }
        ) {
            SmartImage(
                model = optimizedAlbumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                targetSize = coil.size.Size.ORIGINAL,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.28f
                        scaleY = 1.28f
                        alpha = if (isDarkTheme) 0.62f else 0.48f
                    }
                    .blur(radius = 64.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            )

            // Translucent frosted-glass gradient wash over the blurred background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to washColor.copy(alpha = 0.25f),
                                0.44f to washColor.copy(alpha = if (isDarkTheme) 0.58f else 0.64f),
                                0.72f to deepWashColor.copy(alpha = if (isDarkTheme) 0.80f else 0.84f),
                                1.0f to deepWashColor.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            // Subtle frosted glass sheen overlay in the bottom half
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isDarkTheme) Color.White.copy(alpha = 0.04f)
                        else Color.White.copy(alpha = 0.16f)
                    )
            )
        }

        // =====================================================================
        // LAYER 2: Morphing Edge-to-Edge Top Artwork (MiniPlayer 44.dp Circle <-> Left-Shifted Arch)
        // =====================================================================
        val shouldApplyPausedScale = !isPlayingProvider()
        val artScale by animateFloatAsState(
            targetValue = if (shouldApplyPausedScale) 1.0f else 1.03f,
            animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            label = "complexArtScale"
        )

        Box(
            modifier = Modifier
                .layout { measurable, _ ->
                    val sample = morphTracker.evaluate(expansionFractionProvider())
                    val baseW = androidx.compose.ui.util.lerp(miniArtSizePx, screenWidthPx, sample.sizeT)
                    val baseH = androidx.compose.ui.util.lerp(miniArtSizePx, artHeightPx, sample.sizeT)
                    val targetW = (baseW * sample.overshootScale).roundToInt().coerceAtLeast(1)
                    val targetH = (baseH * sample.overshootScale).roundToInt().coerceAtLeast(1)
                    val baseX = androidx.compose.ui.util.lerp(miniArtOffsetPx, 0f, sample.posT)
                    val baseY = androidx.compose.ui.util.lerp(miniArtOffsetPx, 0f, sample.posT)
                    val placeX = (baseX - (targetW - baseW) * 0.5f).roundToInt()
                    val placeY = (baseY - (targetH - baseH) * 0.5f).roundToInt()

                    val placeable = measurable.measure(Constraints.fixed(targetW, targetH))
                    layout(targetW, targetH) {
                        placeable.place(placeX, placeY)
                    }
                }
                .clip(scoopedShape)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                    onLongClick = { if (aodScreenEnabled) onEnterAodScreen() }
                )
                .pointerInput(song.id) {
                    var horizontalDrag = 0f
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        horizontalDrag = 0f
                        drag(down.id) { change ->
                            val dx = change.positionChange().x
                            val dy = change.positionChange().y
                            if (abs(dx) > abs(dy) * 1.3f) {
                                horizontalDrag += dx
                            }
                        }
                        if (horizontalDrag > 72.dp.toPx()) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onPrevious()
                        } else if (horizontalDrag < -72.dp.toPx()) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNext()
                        }
                    }
                }
        ) {
            SmartImage(
                model = optimizedAlbumArtUri,
                contentDescription = resolvedTitle,
                contentScale = ContentScale.Crop,
                targetSize = coil.size.Size.ORIGINAL,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = artScale
                        scaleY = artScale
                    }
            )

            // Top scrim for status bar and drag handle legibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        alpha = morphTracker.evaluate(expansionFractionProvider()).nonArtAlpha
                    }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.38f),
                                Color.Black.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // =====================================================================
        // LAYER 3: Bold Material You Expressive Curved Progress Bar on the Arch
        // =====================================================================
        CurvedArcSeekBar(
            songId = song.id,
            artHeightDp = artHeightDp,
            rightRiseDp = rightRiseDp,
            currentPositionProvider = currentPositionProvider,
            totalDurationValue = totalDurationValue,
            songDurationHintMs = song.duration,
            isPlayingProvider = isPlayingProvider,
            expansionFractionProvider = expansionFractionProvider,
            currentSheetState = currentSheetState,
            allowRealtimeUpdates = allowRealtimeUpdates,
            activeTrackColor = baseTheme.primary,
            inactiveTrackColor = baseTheme.secondaryContainer,
            thumbColor = baseTheme.primary,
            onSeek = onSeek,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    val a = morphTracker.evaluate(expansionFractionProvider()).nonArtAlpha
                    alpha = a
                    translationY = (1f - a) * slideOffsetPx
                }
        )

        // =====================================================================
        // LAYER 4: Lower Half Content
        // Left: Reliable Song Title/Artist + Glowing Karaoke Lyrics
        // Right: Solid-Filled Vertical Stack (3-Dot Menu, Like, Previous, Play/Pause, Next)
        // =====================================================================
        val rightColumnTopDp = (artHeightDp - rightRiseDp + 24.dp)
            .coerceAtLeast(statusBarTopPadding + 64.dp)
            .coerceAtLeast(0.dp)
        val leftColumnTopDp = (artHeightDp + 18.dp).coerceAtLeast(0.dp)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val a = morphTracker.evaluate(expansionFractionProvider()).nonArtAlpha
                    alpha = a
                    translationY = (1f - a) * slideOffsetPx
                }
                .padding(
                    bottom = (navBarBottomPadding + 16.dp).coerceAtLeast(0.dp),
                    start = 22.dp,
                    end = 20.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // LEFT AREA: Song Title / Artist + Glowing Word-by-Word Karaoke Lyrics
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = leftColumnTopDp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Song Title & Artist — direct marquee Text so it never blanks out or flickers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    key(song.id, resolvedTitle) {
                        Text(
                            text = resolvedTitle,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                lineHeight = 30.sp
                            ),
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    initialDelayMillis = 1600,
                                    velocity = 28.dp
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    key(song.id, resolvedArtist) {
                        Text(
                            text = resolvedArtist,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            ),
                            color = contentColor.copy(alpha = 0.78f),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    initialDelayMillis = 2000,
                                    velocity = 24.dp
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onArtistClick
                                )
                        )
                    }
                }

                // Glowing Word-by-Word Karaoke Lyrics Stage (UNCLIPPED so glow radiates freely)
                ComplexKaraokeLyricsStage(
                    lyrics = lyricsProvider(),
                    playbackPositionFlow = playbackPositionFlow,
                    isPlayingProvider = isPlayingProvider,
                    syncOffsetMs = lyricsSyncOffset,
                    textColor = contentColor,
                    accentColor = accentColor,
                    onClick = onLyricsClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            // RIGHT AREA: Solid-Filled Material You Expressive Vertical Control Stack
            // Order: 3-Dot Menu -> Like -> Previous -> Play/Pause -> Next
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = rightColumnTopDp),
                contentAlignment = Alignment.BottomCenter
            ) {
                VerticalExpressiveControlStack(
                    isPlayingProvider = isPlayingProvider,
                    isFavoriteProvider = isFavoriteProvider,
                    onMoreOptionsClick = onMoreOptionsClick,
                    onFavoriteToggle = onFavoriteToggle,
                    onPrevious = onPrevious,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    playPauseContainerColor = baseTheme.primary,
                    onPlayPauseContainerColor = baseTheme.onPrimary,
                    skipContainerColor = baseTheme.secondaryContainer,
                    onSkipContainerColor = baseTheme.onSecondaryContainer,
                    likedContainerColor = baseTheme.tertiary,
                    onLikedContainerColor = baseTheme.onTertiary,
                    neutralContainerColor = baseTheme.surfaceContainerHighest,
                    onNeutralContainerColor = baseTheme.onSurfaceVariant,
                    modifier = Modifier
                        .width(68.dp)
                        .height(332.dp)
                )
            }
        }

        // =====================================================================
        // LAYER 5: Minimal Chrome (Small Top Drag Handle + Solid Filled Top-Right Collapse Button)
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = morphTracker.evaluate(expansionFractionProvider()).nonArtAlpha
                }
                .padding(
                    top = (statusBarTopPadding + 10.dp).coerceAtLeast(0.dp),
                    start = 20.dp,
                    end = 20.dp
                )
                .height(46.dp)
                .align(Alignment.TopCenter)
        ) {
            // Small drag handle at top center
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .width(38.dp)
                    .height(4.5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.82f))
            )

            // Solid filled collapse chevron button at top right (NO border, NO transparent fill)
            val collapseOffsetY = remember { Animatable(0f) }
            val collapseScale = remember { Animatable(1f) }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(baseTheme.secondaryContainer, CircleShape)
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        coroutineScope.launch {
                            collapseOffsetY.snapTo(6f)
                            collapseScale.snapTo(1.16f)
                            launch {
                                collapseOffsetY.animateTo(
                                    0f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                collapseScale.animateTo(
                                    1f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                        onCollapse()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_keyboard_arrow_down_24),
                    contentDescription = stringResource(R.string.presentation_batch_g_player_cd_collapse),
                    tint = baseTheme.onSecondaryContainer,
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            translationY = collapseOffsetY.value
                            scaleX = collapseScale.value
                            scaleY = collapseScale.value
                        }
                )
            }
        }
    }
}

/**
 * Bold Material You Expressive curved progress bar that follows the asymmetric (C--M--------C) arch.
 * Features a thick split track with a clean gap around a bold perpendicular pill thumb, and no time labels.
 */
@Composable
private fun CurvedArcSeekBar(
    songId: String,
    artHeightDp: Dp,
    rightRiseDp: Dp,
    currentPositionProvider: () -> Long,
    totalDurationValue: Long,
    songDurationHintMs: Long,
    isPlayingProvider: () -> Boolean,
    expansionFractionProvider: () -> Float,
    currentSheetState: PlayerSheetState,
    allowRealtimeUpdates: Boolean,
    activeTrackColor: Color,
    inactiveTrackColor: Color,
    thumbColor: Color,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current

    val isVisible by remember(expansionFractionProvider) {
        derivedStateOf { expansionFractionProvider() > 0.01f }
    }
    val isExpanded by remember(currentSheetState, expansionFractionProvider) {
        derivedStateOf {
            currentSheetState == PlayerSheetState.EXPANDED && expansionFractionProvider() >= 0.995f
        }
    }
    val shouldRunRealtimeUpdates = allowRealtimeUpdates && isVisible

    val reportedDuration = totalDurationValue.coerceAtLeast(0L)
    val hintDuration = songDurationHintMs.coerceAtLeast(0L)
    val displayDurationValue = when {
        reportedDuration <= 0L && hintDuration <= 0L -> 0L
        reportedDuration <= 0L -> hintDuration
        hintDuration <= 0L -> reportedDuration
        abs(reportedDuration - hintDuration) <= 1500L -> reportedDuration
        else -> minOf(reportedDuration, hintDuration)
    }
    val durationForCalc = displayDurationValue.coerceAtLeast(1L)

    val (smoothProgressState, _) = rememberSmoothProgress(
        isPlayingProvider = isPlayingProvider,
        currentPositionProvider = currentPositionProvider,
        totalDuration = displayDurationValue,
        songId = songId,
        sampleWhilePlayingMs = if (isExpanded) 180L else 320L,
        sampleWhilePausedMs = 800L,
        isVisible = shouldRunRealtimeUpdates
    )

    var sliderDragValue by remember(songId) { mutableStateOf<Float?>(null) }
    var targetSeekFraction by remember(songId) { mutableFloatStateOf(-1f) }
    var lastSeekFinishedTime by remember(songId) { mutableLongStateOf(0L) }

    LaunchedEffect(songId) {
        sliderDragValue = null
        targetSeekFraction = -1f
        lastSeekFinishedTime = 0L
    }

    LaunchedEffect(songId) {
        snapshotFlow { smoothProgressState.value }.collect { progress ->
            if (sliderDragValue != null) return@collect
            val target = targetSeekFraction
            if (target < 0f) return@collect
            val timeSinceSeek = System.currentTimeMillis() - lastSeekFinishedTime
            val diff = abs(progress - target)
            if (timeSinceSeek > 1500L || diff < 0.04f) {
                targetSeekFraction = -1f
            }
        }
    }

    val currentProgress = when {
        sliderDragValue != null -> sliderDragValue!!
        targetSeekFraction >= 0f -> targetSeekFraction
        else -> smoothProgressState.value
    }.coerceIn(0f, 1f)

    val isDragging = sliderDragValue != null
    val thumbLengthScale by animateFloatAsState(
        targetValue = if (isDragging) 1.32f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "expressiveThumbScale"
    )

    val touchBandTopDp = (artHeightDp - rightRiseDp - 28.dp).coerceAtLeast(0.dp)
    val touchBandHeightDp = rightRiseDp + 56.dp
    val rightRisePx = with(density) { rightRiseDp.toPx() }
    val artHeightPx = with(density) { artHeightDp.toPx() }
    val touchBandTopPx = with(density) { touchBandTopDp.toPx() }
    val bottomVertexOffsetPx = artHeightPx - touchBandTopPx
    val edgeInsetPx = with(density) { 18.dp.toPx() }
    val hitTolerancePx = with(density) { 38.dp.toPx() }

    val latestOnSeek by rememberUpdatedState(onSeek)
    val latestDurationForCalc by rememberUpdatedState(durationForCalc)

    Box(
        modifier = modifier
            .padding(top = touchBandTopDp)
            .height(touchBandHeightDp)
            .pointerInput(songId, rightRisePx, bottomVertexOffsetPx) {
                var lastQuantizedStep = -1
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val w = size.width.toFloat()
                    if (w <= 0f) return@awaitEachGesture

                    val circle = computeAsymmetricArchCircle(
                        widthPx = w,
                        bottomVertexYPx = bottomVertexOffsetPx,
                        rightRisePx = rightRisePx,
                        centerXFraction = ARCH_CENTER_X_FRACTION
                    )

                    val dx = (down.position.x - circle.cx).coerceIn(-circle.radius * 0.99f, circle.radius * 0.99f)
                    val curveYAtTouchX = circle.cy + sqrt((circle.radius * circle.radius) - (dx * dx))

                    if (abs(down.position.y - curveYAtTouchX) > hitTolerancePx) {
                        return@awaitEachGesture
                    }

                    down.consume()
                    val usableWidth = (w - 2f * edgeInsetPx).coerceAtLeast(1f)
                    val initialFraction = ((down.position.x - edgeInsetPx) / usableWidth).coerceIn(0f, 1f)
                    sliderDragValue = initialFraction
                    lastQuantizedStep = (initialFraction * 24f).toInt()
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                    drag(down.id) { change ->
                        change.consume()
                        val newFraction = ((change.position.x - edgeInsetPx) / usableWidth).coerceIn(0f, 1f)
                        sliderDragValue = newFraction
                        val step = (newFraction * 24f).toInt()
                        if (step != lastQuantizedStep) {
                            lastQuantizedStep = step
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }

                    val committed = sliderDragValue ?: initialFraction
                    targetSeekFraction = committed
                    lastSeekFinishedTime = System.currentTimeMillis()
                    sliderDragValue = null
                    latestOnSeek((committed * latestDurationForCalc).roundToLong())
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            if (w <= 0f) return@Canvas

            val circle = computeAsymmetricArchCircle(
                widthPx = w,
                bottomVertexYPx = bottomVertexOffsetPx,
                rightRisePx = rightRisePx,
                centerXFraction = ARCH_CENTER_X_FRACTION
            )

            val leftChord = (circle.cx - edgeInsetPx).coerceAtLeast(0f)
            val rightChord = ((w - edgeInsetPx) - circle.cx).coerceAtLeast(0f)

            val alphaStartDeg = Math.toDegrees(asin((leftChord / circle.radius).coerceIn(-1f, 1f).toDouble())).toFloat()
            val alphaEndDeg = Math.toDegrees(asin((rightChord / circle.radius).coerceIn(-1f, 1f).toDouble())).toFloat()

            // Start at left edge (90 + alphaStartDeg) and sweep counter-clockwise to right edge (90 - alphaEndDeg)
            val startAngleDeg = 90f + alphaStartDeg
            val totalSweepDeg = -(alphaStartDeg + alphaEndDeg)
            val thumbAngleDeg = startAngleDeg + (totalSweepDeg * currentProgress)

            val trackStrokePx = 10.dp.toPx()
            // Angular gap around the expressive pill thumb (Material 3 Expressive split track)
            val gapArcLengthPx = 13.dp.toPx()
            val gapDeg = Math.toDegrees((gapArcLengthPx / circle.radius).toDouble()).toFloat()

            val arcRectTopLeft = Offset(circle.cx - circle.radius, circle.cy - circle.radius)
            val arcRectSize = Size(circle.radius * 2f, circle.radius * 2f)

            // 1. Bold Active Track (left start -> thumb - gap)
            val activeSweepDeg = (thumbAngleDeg - startAngleDeg) + gapDeg
            if (activeSweepDeg < -0.15f) {
                drawArc(
                    color = activeTrackColor,
                    startAngle = startAngleDeg,
                    sweepAngle = activeSweepDeg.coerceAtLeast(totalSweepDeg),
                    useCenter = false,
                    topLeft = arcRectTopLeft,
                    size = arcRectSize,
                    style = Stroke(width = trackStrokePx, cap = StrokeCap.Round)
                )
            }

            // 2. Bold Inactive Track (thumb + gap -> right end)
            val inactiveStartDeg = thumbAngleDeg - gapDeg
            val endAngleDeg = startAngleDeg + totalSweepDeg
            val inactiveSweepDeg = endAngleDeg - inactiveStartDeg
            if (inactiveSweepDeg < -0.15f) {
                drawArc(
                    color = inactiveTrackColor,
                    startAngle = inactiveStartDeg,
                    sweepAngle = inactiveSweepDeg,
                    useCenter = false,
                    topLeft = arcRectTopLeft,
                    size = arcRectSize,
                    style = Stroke(width = trackStrokePx, cap = StrokeCap.Round)
                )
            }

            // 3. Bold Material You Expressive Pill Thumb oriented perpendicular (normal) to the curve
            val thumbAngleRad = Math.toRadians(thumbAngleDeg.toDouble())
            val cosA = cos(thumbAngleRad).toFloat()
            val sinA = sin(thumbAngleRad).toFloat()
            val halfThumbLenPx = 12.dp.toPx() * thumbLengthScale
            val innerR = circle.radius - halfThumbLenPx
            val outerR = circle.radius + halfThumbLenPx

            drawLine(
                color = thumbColor,
                start = Offset(circle.cx + innerR * cosA, circle.cy + innerR * sinA),
                end = Offset(circle.cx + outerR * cosA, circle.cy + outerR * sinA),
                strokeWidth = 7.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * High-precision 60fps interpolated playback clock.
 * Because PlaybackStateHolder emits position ticks every 250ms, waiting for raw StateFlow
 * updates can delay word appearance by up to a quarter second. This interpolates smoothly
 * between ticks using SystemClock.elapsedRealtime() while playing.
 */
@Composable
private fun rememberInterpolatedPlaybackPositionMs(
    playbackPositionFlow: StateFlow<Long>,
    isPlayingProvider: () -> Boolean
): State<Long> {
    val rawPositionMs by playbackPositionFlow.collectAsStateWithLifecycle()
    val isPlaying = isPlayingProvider()
    val interpolatedState = remember { mutableLongStateOf(rawPositionMs) }
    val anchorPosRef = remember { longArrayOf(rawPositionMs) }
    val anchorElapsedRef = remember { longArrayOf(SystemClock.elapsedRealtime()) }

    LaunchedEffect(rawPositionMs) {
        anchorPosRef[0] = rawPositionMs
        anchorElapsedRef[0] = SystemClock.elapsedRealtime()
        interpolatedState.longValue = rawPositionMs
    }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            interpolatedState.longValue = anchorPosRef[0]
            return@LaunchedEffect
        }
        anchorElapsedRef[0] = SystemClock.elapsedRealtime()
        while (isActive) {
            withFrameNanos {
                val elapsedSinceTick = (SystemClock.elapsedRealtime() - anchorElapsedRef[0]).coerceIn(0L, 320L)
                interpolatedState.longValue = anchorPosRef[0] + elapsedSinceTick
            }
        }
    }

    return interpolatedState
}

/**
 * Smart vocal-cadence word timing resolver:
 * 1. If explicit word-level timestamps exist, clusters multi-syllable fragments into whole words
 *    so words don't split awkwardly, preserving exact vocal start/end timestamps.
 * 2. If only line-level timestamps exist, estimates each word's vocal duration using phonetic
 *    syllable nuclei, sustained vowel digraphs (aa, ee, oo, ai, haan, hai, etc.), fast function-word
 *    compression, mid-line comma/punctuation breath pauses, and instrumental tail capping so words
 *    appear right as the singer pronounces them.
 */
private fun resolveSmartVocalWords(
    syncedLine: SyncedLine?,
    nextLineTimeMs: Long
): List<DisplayWord> {
    if (syncedLine == null) return emptyList()

    val sanitizedExplicit = syncedLine.words?.let(::sanitizeSyncedWords)
    if (!sanitizedExplicit.isNullOrEmpty()) {
        val clusters = clusterSyncedWords(sanitizedExplicit)
        if (clusters.isNotEmpty()) {
            return clusters.mapIndexed { idx, cluster ->
                val startMs = cluster.words.first().time.toLong()
                val nextClusterStartMs = if (idx < clusters.lastIndex) {
                    clusters[idx + 1].words.first().time.toLong()
                } else {
                    maxOf(cluster.words.last().time.toLong() + 420L, nextLineTimeMs)
                }
                val mergedWord = cluster.words.joinToString("") { it.word }
                DisplayWord(
                    word = mergedWord,
                    startTimeMs = startMs,
                    endTimeMs = nextClusterStartMs.coerceAtLeast(startMs + 80L)
                )
            }
        }
    }

    val sanitizedLine = sanitizeLyricLineText(syncedLine.line)
    val rawTokens = sanitizedLine.split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (rawTokens.isEmpty()) return emptyList()

    val lineStart = syncedLine.time.toLong()
    val rawLineGapMs = (nextLineTimeMs - lineStart).coerceAtLeast(900L)

    data class TokenTimingProfile(
        val word: String,
        val vocalWeight: Float,
        val breathPauseAfterMs: Long
    )

    val profiles = rawTokens.mapIndexed { idx, token ->
        val cleanLetters = token.trim { !it.isLetterOrDigit() }.lowercase()
        val vowelMatches = VowelGroupRegex.findAll(cleanLetters).count().coerceAtLeast(1)
        val hasSustainedVowel = SustainedVowelRegex.containsMatchIn(cleanLetters)
        val isFastParticle = cleanLetters in FastFunctionWords && idx != rawTokens.lastIndex

        var weight = vowelMatches.toFloat() * 1.05f + (cleanLetters.length.coerceAtMost(8) * 0.12f)
        if (hasSustainedVowel) weight *= 1.26f
        if (isFastParticle) weight *= 0.62f
        if (idx == rawTokens.lastIndex) weight *= 1.32f

        val trimmedEnd = token.trimEnd()
        val lastChar = trimmedEnd.lastOrNull()
        val hasMidLineBreath = idx < rawTokens.lastIndex &&
            (lastChar == ',' || lastChar == ';' || lastChar == ':' || lastChar == '-' ||
                lastChar == '—' || lastChar == ')' || lastChar == '!' || lastChar == '?')

        if (hasMidLineBreath) {
            weight *= 1.18f
        }

        val pauseAfterMs = when {
            idx >= rawTokens.lastIndex -> 0L
            trimmedEnd.endsWith("...") || lastChar == '?' || lastChar == '!' || lastChar == ')' -> 280L
            hasMidLineBreath -> 230L
            else -> 0L
        }

        TokenTimingProfile(
            word = token,
            vocalWeight = weight.coerceAtLeast(0.45f),
            breathPauseAfterMs = pauseAfterMs
        )
    }

    val totalBreathPausesMs = profiles.sumOf { it.breathPauseAfterMs }
    val totalWeight = profiles.sumOf { it.vocalWeight.toDouble() }.toFloat().coerceAtLeast(1f)

    val naturalMaxVocalMs = (totalWeight * 295f).toLong() + totalBreathPausesMs + 320L
    val effectiveLineSpanMs = minOf((rawLineGapMs * 0.86f).toLong(), naturalMaxVocalMs)
        .coerceAtLeast(minOf(rawLineGapMs, 1050L))
    val availableSingingMs = (effectiveLineSpanMs - totalBreathPausesMs).coerceAtLeast(600L)

    var cursorMs = lineStart
    return profiles.mapIndexed { idx, profile ->
        val rawWordDuration = ((profile.vocalWeight / totalWeight) * availableSingingMs)
            .toLong()
            .coerceAtLeast(110L)
        val wordStart = cursorMs
        val wordEnd = if (idx == profiles.lastIndex) {
            maxOf(wordStart + rawWordDuration, minOf(nextLineTimeMs - 120L, wordStart + rawWordDuration + 650L))
        } else {
            wordStart + rawWordDuration
        }
        cursorMs = wordStart + rawWordDuration + profile.breathPauseAfterMs
        DisplayWord(
            word = profile.word,
            startTimeMs = wordStart,
            endTimeMs = wordEnd
        )
    }
}

private data class KaraokeLineState(
    val lineIndex: Int,
    val lineTimeMs: Int,
    val syncedLine: SyncedLine?,
    val nextLineTimeMs: Long,
    val nextLineText: String
)

/**
 * Karaoke word-by-word lyrics display where:
 * - Each line transition uses the iOS Dynamic Island spring expansion + motion blur animation
 *   (matching MusicRecognitionOverlay's card appear animation).
 * - Words appear at the exact time the song speaks them and glow without any rectangular box clipping.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComplexKaraokeLyricsStage(
    lyrics: Lyrics?,
    playbackPositionFlow: StateFlow<Long>,
    isPlayingProvider: () -> Boolean,
    syncOffsetMs: Int,
    textColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interpolatedPositionMs by rememberInterpolatedPlaybackPositionMs(
        playbackPositionFlow = playbackPositionFlow,
        isPlayingProvider = isPlayingProvider
    )
    val effectivePosition = interpolatedPositionMs + syncOffsetMs + VOCAL_ONSET_LEAD_IN_MS

    val syncedLines = lyrics?.synced ?: emptyList()
    val plainLines = lyrics?.plain?.filter { it.isNotBlank() } ?: emptyList()

    // IMPORTANT: No .clip() here so word glow and scale animations radiate freely without box edges!
    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            syncedLines.isNotEmpty() -> {
                val currentLineIndex = remember(effectivePosition, syncedLines) {
                    syncedLines.indexOfLast { it.time <= effectivePosition }
                }

                val activeLineState = remember(currentLineIndex, syncedLines) {
                    val resolvedIdx = if (currentLineIndex >= 0) currentLineIndex else 0
                    val line = syncedLines.getOrNull(resolvedIdx)
                    val nextLine = syncedLines.getOrNull(resolvedIdx + 1)
                    val nextTimeMs = when {
                        nextLine != null -> nextLine.time.toLong()
                        line != null -> line.time.toLong() + 6500L
                        else -> 6500L
                    }
                    KaraokeLineState(
                        lineIndex = resolvedIdx,
                        lineTimeMs = line?.time ?: -1,
                        syncedLine = line,
                        nextLineTimeMs = nextTimeMs,
                        nextLineText = nextLine?.line?.let(::sanitizeLyricLineText).orEmpty()
                    )
                }

                // iOS Dynamic Island / Music Recognition Card appear animation on every line change!
                // SizeTransform(clip = false) ensures glow and scaling words are NEVER clipped in a box.
                AnimatedContent(
                    targetState = activeLineState,
                    contentKey = { it.lineTimeMs to it.lineIndex },
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(durationMillis = 360)) +
                            scaleIn(
                                initialScale = 0.44f,
                                transformOrigin = TransformOrigin(0.12f, 0f),
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = 100f)
                            ) +
                            slideInVertically(
                                initialOffsetY = { -it / 2 - 32 },
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = 100f)
                            ))
                            .togetherWith(
                                fadeOut(animationSpec = tween(durationMillis = 220)) +
                                    scaleOut(
                                        targetScale = 0.76f,
                                        transformOrigin = TransformOrigin(0.12f, 1f),
                                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 120f)
                                    ) +
                                    slideOutVertically(
                                        targetOffsetY = { it / 2 + 16 },
                                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 120f)
                                    )
                            )
                            .using(SizeTransform(clip = false))
                    },
                    label = "complexDynamicIslandLineTransition",
                    modifier = Modifier.fillMaxWidth()
                ) { lineState ->
                    val lineWords = remember(lineState.syncedLine, lineState.nextLineTimeMs) {
                        resolveSmartVocalWords(lineState.syncedLine, lineState.nextLineTimeMs)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
                    ) {
                        if (lineWords.isEmpty()) {
                            Text(
                                text = "•••",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = GoogleSansRounded,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp
                                ),
                                color = accentColor.copy(alpha = 0.80f)
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(11.dp, Alignment.Start),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                lineWords.forEachIndexed { index, item ->
                                    val hasAppeared = effectivePosition >= item.startTimeMs
                                    val isCurrent = effectivePosition in item.startTimeMs until item.endTimeMs

                                    key("${lineState.lineTimeMs}_${item.startTimeMs}_$index") {
                                        val appearScale by animateFloatAsState(
                                            targetValue = when {
                                                !hasAppeared -> 0.42f
                                                isCurrent -> 1.14f
                                                else -> 1.0f
                                            },
                                            animationSpec = spring(
                                                dampingRatio = if (isCurrent) 0.62f else 0.78f,
                                                stiffness = if (hasAppeared) 150f else 220f
                                            ),
                                            label = "wordAppearScale"
                                        )
                                        val appearAlpha by animateFloatAsState(
                                            targetValue = when {
                                                !hasAppeared -> 0f
                                                isCurrent -> 1f
                                                else -> 0.82f
                                            },
                                            animationSpec = tween(durationMillis = if (hasAppeared) 170 else 90),
                                            label = "wordAppearAlpha"
                                        )
                                        val wordOffsetY by animateFloatAsState(
                                            targetValue = when {
                                                !hasAppeared -> 18f
                                                isCurrent -> -3f
                                                else -> 0f
                                            },
                                            animationSpec = spring(
                                                dampingRatio = 0.74f,
                                                stiffness = 140f
                                            ),
                                            label = "wordOffsetY"
                                        )
                                        val glowStrength by animateFloatAsState(
                                            targetValue = if (isCurrent) 1f else 0f,
                                            animationSpec = tween(durationMillis = 160),
                                            label = "wordGlowStrength"
                                        )
                                        val baseWordColor by animateColorAsState(
                                            targetValue = if (isCurrent) accentColor else textColor,
                                            animationSpec = tween(durationMillis = 150),
                                            label = "wordColor"
                                        )

                                        val effectiveWordColor = baseWordColor.copy(
                                            alpha = (baseWordColor.alpha * appearAlpha).coerceIn(0f, 1f)
                                        )
                                        val effectiveShadowColor = accentColor.copy(
                                            alpha = (0.55f * glowStrength * appearAlpha).coerceIn(0f, 1f)
                                        )

                                        // Direct Text rendering with NO Modifier.blur and NO graphicsLayer alpha < 1f
                                        // so Android's hardware RenderNode never allocates a rectangular clipping box!
                                        Text(
                                            text = item.word,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontFamily = GoogleSansRounded,
                                                fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                                                fontSize = 22.sp,
                                                shadow = if (glowStrength > 0.02f) {
                                                    Shadow(
                                                        color = effectiveShadowColor,
                                                        offset = Offset.Zero,
                                                        blurRadius = 14f
                                                    )
                                                } else {
                                                    null
                                                }
                                            ),
                                            color = effectiveWordColor,
                                            modifier = Modifier.graphicsLayer {
                                                clip = false
                                                scaleX = appearScale
                                                scaleY = appearScale
                                                translationY = wordOffsetY
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Upcoming line preview for frosted depth
                        if (lineState.nextLineText.isNotBlank()) {
                            Text(
                                text = lineState.nextLineText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = GoogleSansRounded,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = textColor.copy(alpha = 0.36f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            plainLines.isNotEmpty() -> {
                Text(
                    text = plainLines.take(4).joinToString("\n"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = GoogleSansRounded,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 25.sp
                    ),
                    color = textColor.copy(alpha = 0.82f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }

            else -> {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = LocalMaterialTheme.current.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_lyrics_24),
                            contentDescription = null,
                            tint = LocalMaterialTheme.current.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Tap for karaoke lyrics",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.Bold
                            ),
                            color = LocalMaterialTheme.current.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Solid-filled Material 3 Expressive vertical control stack on the right:
 * 3-Dot Menu -> Like -> Previous -> Play/Pause -> Next
 */
@Composable
private fun VerticalExpressiveControlStack(
    isPlayingProvider: () -> Boolean,
    isFavoriteProvider: () -> Boolean,
    onMoreOptionsClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    playPauseContainerColor: Color,
    onPlayPauseContainerColor: Color,
    skipContainerColor: Color,
    onSkipContainerColor: Color,
    likedContainerColor: Color,
    onLikedContainerColor: Color,
    neutralContainerColor: Color,
    onNeutralContainerColor: Color,
    modifier: Modifier = Modifier
) {
    val isPlaying = isPlayingProvider()
    val isFavorite = isFavoriteProvider()
    val haptics = LocalHapticFeedback.current

    var activeButton by remember { mutableStateOf<VerticalControlButton?>(null) }
    var playPauseVisualState by remember { mutableStateOf(isPlaying) }
    val latestIsPlayingProvider by rememberUpdatedState(isPlayingProvider)

    LaunchedEffect(activeButton) {
        if (activeButton != null) {
            delay(230L)
            activeButton = null
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            playPauseVisualState = true
        } else {
            if (activeButton == VerticalControlButton.NEXT || activeButton == VerticalControlButton.PREVIOUS) {
                delay(220L)
            }
            if (!latestIsPlayingProvider()) {
                playPauseVisualState = false
            }
        }
    }

    fun weightFor(button: VerticalControlButton, baseWeight: Float): Float {
        return when (activeButton) {
            button -> baseWeight * 1.22f
            null -> baseWeight
            else -> baseWeight * 0.88f
        }
    }

    val weightSpec = spring<Float>(
        dampingRatio = 0.62f,
        stiffness = Spring.StiffnessMediumLow
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 3-DOT MORE OPTIONS BUTTON (Over the Like button)
        val moreWeight by animateFloatAsState(
            targetValue = weightFor(VerticalControlButton.MORE, 0.74f),
            animationSpec = weightSpec,
            label = "moreVerticalWeight"
        )
        ExpressiveMorphingStackButton(
            modifier = Modifier
                .fillMaxWidth()
                .weight(moreWeight),
            defaultCornerDp = 34.dp,
            pressedCornerDp = 14.dp,
            isActive = activeButton == VerticalControlButton.MORE,
            containerColor = neutralContainerColor,
            onClick = {
                activeButton = VerticalControlButton.MORE
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onMoreOptionsClick()
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = stringResource(R.string.cd_player_more_options),
                tint = onNeutralContainerColor,
                modifier = Modifier.size(26.dp)
            )
        }

        // 2. LIKE BUTTON
        val likeWeight by animateFloatAsState(
            targetValue = weightFor(VerticalControlButton.LIKE, 0.88f),
            animationSpec = weightSpec,
            label = "likeVerticalWeight"
        )
        val likeContainerColor by animateColorAsState(
            targetValue = if (isFavorite) likedContainerColor else neutralContainerColor,
            animationSpec = tween(220),
            label = "likeContainerColor"
        )
        val likeIconColor by animateColorAsState(
            targetValue = if (isFavorite) onLikedContainerColor else onNeutralContainerColor,
            animationSpec = tween(220),
            label = "likeIconColor"
        )

        ExpressiveMorphingStackButton(
            modifier = Modifier
                .fillMaxWidth()
                .weight(likeWeight),
            defaultCornerDp = if (isFavorite) 20.dp else 34.dp,
            pressedCornerDp = 14.dp,
            isActive = activeButton == VerticalControlButton.LIKE,
            containerColor = likeContainerColor,
            onClick = {
                activeButton = VerticalControlButton.LIKE
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onFavoriteToggle()
            }
        ) {
            val heartScale by animateFloatAsState(
                targetValue = if (isFavorite) 1.14f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "heartPopScale"
            )
            Icon(
                painter = painterResource(R.drawable.round_favorite_24),
                contentDescription = "Favorite",
                tint = likeIconColor,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        scaleX = heartScale
                        scaleY = heartScale
                    }
            )
        }

        // 3. PREVIOUS BUTTON
        val prevWeight by animateFloatAsState(
            targetValue = weightFor(VerticalControlButton.PREVIOUS, 0.96f),
            animationSpec = weightSpec,
            label = "prevVerticalWeight"
        )
        ExpressiveMorphingStackButton(
            modifier = Modifier
                .fillMaxWidth()
                .weight(prevWeight),
            defaultCornerDp = 34.dp,
            pressedCornerDp = 16.dp,
            isActive = activeButton == VerticalControlButton.PREVIOUS,
            containerColor = skipContainerColor,
            onClick = {
                activeButton = VerticalControlButton.PREVIOUS
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPrevious()
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint = onSkipContainerColor,
                modifier = Modifier.size(32.dp)
            )
        }

        // 4. PLAY / PAUSE BUTTON (Hero Expressive Morphing Button)
        val playWeight by animateFloatAsState(
            targetValue = weightFor(VerticalControlButton.PLAY_PAUSE, 1.32f),
            animationSpec = weightSpec,
            label = "playVerticalWeight"
        )
        ExpressiveMorphingStackButton(
            modifier = Modifier
                .fillMaxWidth()
                .weight(playWeight),
            defaultCornerDp = if (playPauseVisualState) 20.dp else 34.dp,
            pressedCornerDp = 14.dp,
            isActive = activeButton == VerticalControlButton.PLAY_PAUSE,
            containerColor = playPauseContainerColor,
            onClick = {
                activeButton = VerticalControlButton.PLAY_PAUSE
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPlayPause()
            }
        ) {
            Crossfade(
                targetState = playPauseVisualState,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "complexPlayPauseIcon"
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = onPlayPauseContainerColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // 5. NEXT BUTTON
        val nextWeight by animateFloatAsState(
            targetValue = weightFor(VerticalControlButton.NEXT, 0.96f),
            animationSpec = weightSpec,
            label = "nextVerticalWeight"
        )
        ExpressiveMorphingStackButton(
            modifier = Modifier
                .fillMaxWidth()
                .weight(nextWeight),
            defaultCornerDp = 34.dp,
            pressedCornerDp = 16.dp,
            isActive = activeButton == VerticalControlButton.NEXT,
            containerColor = skipContainerColor,
            onClick = {
                activeButton = VerticalControlButton.NEXT
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onNext()
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                tint = onSkipContainerColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Solid-filled Material You Expressive button with smooth corner morphing and spring press scale.
 * No outlines/borders or transparent fills.
 */
@Composable
private fun ExpressiveMorphingStackButton(
    defaultCornerDp: Dp,
    pressedCornerDp: Dp,
    isActive: Boolean,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val latestOnClick by rememberUpdatedState(onClick)

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed || isActive) pressedCornerDp else defaultCornerDp,
        animationSpec = spring(
            dampingRatio = 0.64f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "stackButtonCorner"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isActive) 1.04f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "stackButtonScale"
    )

    val shape = AbsoluteSmoothCornerShape(
        cornerRadiusTL = cornerRadius,
        smoothnessAsPercentTL = 60,
        cornerRadiusTR = cornerRadius,
        smoothnessAsPercentTR = 60,
        cornerRadiusBL = cornerRadius,
        smoothnessAsPercentBL = 60,
        cornerRadiusBR = cornerRadius,
        smoothnessAsPercentBR = 60
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(shape)
            .background(containerColor, shape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        up.consume()
                        latestOnClick()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
