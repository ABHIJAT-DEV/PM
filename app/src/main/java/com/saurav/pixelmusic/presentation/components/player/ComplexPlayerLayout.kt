package com.saurav.pixelmusic.presentation.components.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.saurav.pixelmusic.data.preferences.AlbumArtQuality
import com.saurav.pixelmusic.presentation.components.AutoScrollingTextOnDemand
import com.saurav.pixelmusic.presentation.components.LocalMaterialTheme
import com.saurav.pixelmusic.presentation.components.SmartImage
import com.saurav.pixelmusic.presentation.components.sanitizeLyricLineText
import com.saurav.pixelmusic.presentation.components.scoped.rememberSmoothProgress
import com.saurav.pixelmusic.presentation.viewmodel.PlayerSheetState
import com.saurav.pixelmusic.ui.theme.GoogleSansRounded
import com.saurav.pixelmusic.ui.theme.LocalIsBlackAndWhiteTheme
import com.saurav.pixelmusic.ui.theme.LocalPixelMusicDarkTheme
import com.saurav.pixelmusic.utils.ThumbnailUrlUtils
import com.saurav.pixelmusic.utils.formatDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Custom shape that clips the bottom of the top-half artwork in a large circular/scooped arc.
 */
private class ScoopedArcBottomShape(
    private val sagittaPx: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val s = sagittaPx.coerceIn(12f, h * 0.45f)
        val halfW = w / 2f
        val radius = (s / 2f) + ((halfW * halfW) / (2f * s))
        val cx = halfW
        val cy = h - radius
        val sinAlpha = (halfW / radius).coerceIn(-1f, 1f)
        val alphaDeg = Math.toDegrees(asin(sinAlpha.toDouble())).toFloat()

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, h - s)
            arcTo(
                rect = Rect(
                    left = cx - radius,
                    top = cy - radius,
                    right = cx + radius,
                    bottom = cy + radius
                ),
                startAngleDegrees = 90f - alphaDeg,
                sweepAngleDegrees = 2f * alphaDeg,
                forceMoveTo = false
            )
            lineTo(0f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

private enum class VerticalControlButton {
    LIKE, PREVIOUS, PLAY_PAUSE, NEXT
}

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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(deepWashColor)
    ) {
        val screenWidthDp = maxWidth
        val screenHeightDp = maxHeight
        val artHeightDp = screenHeightDp * 0.52f
        val sagittaDp = (screenWidthDp * 0.145f).coerceIn(46.dp, 68.dp)
        val sagittaPx = with(density) { sagittaDp.toPx() }
        val scoopedShape = remember(sagittaPx) { ScoopedArcBottomShape(sagittaPx) }

        // =====================================================================
        // LAYER 1: Heavy Frosted-Glass / Blurred Cover Wash in Lower Half
        // =====================================================================
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

        // =====================================================================
        // LAYER 2: Edge-to-Edge Top Artwork with Curved, Scooped Cutoff
        // =====================================================================
        val shouldApplyPausedScale = !isPlayingProvider()
        val artScale by animateFloatAsState(
            targetValue = if (shouldApplyPausedScale) 1.0f else 1.03f,
            animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            label = "complexArtScale"
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(artHeightDp)
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
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                targetSize = coil.size.Size.ORIGINAL,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = artScale
                        scaleY = artScale
                    }
            )

            // Top scrim for status bar, drag handle, and collapse chevron legibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.44f),
                                Color.Black.copy(alpha = 0.14f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Soft vignette near the curved bottom edge of the artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                washColor.copy(alpha = 0.38f)
                            )
                        )
                    )
            )
        }

        // =====================================================================
        // LAYER 3: Curved Progress / Seek Bar Following the Arc
        // =====================================================================
        CurvedArcSeekBar(
            songId = song.id,
            artHeightDp = artHeightDp,
            sagittaDp = sagittaDp,
            currentPositionProvider = currentPositionProvider,
            totalDurationValue = totalDurationValue,
            songDurationHintMs = song.duration,
            isPlayingProvider = isPlayingProvider,
            expansionFractionProvider = expansionFractionProvider,
            currentSheetState = currentSheetState,
            allowRealtimeUpdates = allowRealtimeUpdates,
            activeColor = accentColor,
            inactiveColor = contentColor.copy(alpha = 0.26f),
            textColor = contentColor.copy(alpha = 0.80f),
            onSeek = onSeek,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // =====================================================================
        // LAYER 4: Lower Half Content (Left: Song + Karaoke Lyrics | Right: Vertical Control Stack)
        // =====================================================================
        val lowerContentTopPadding = artHeightDp - sagittaDp + 28.dp

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = lowerContentTopPadding,
                    bottom = navBarBottomPadding + 16.dp,
                    start = 24.dp,
                    end = 20.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // LEFT AREA: Song Title / Artist + Glowing Word-by-Word Karaoke Lyrics
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = (sagittaDp - 14.dp).coerceAtLeast(18.dp)),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Song Title + Artist + Subtle Options Button
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AutoScrollingTextOnDemand(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 23.sp,
                                color = contentColor
                            ),
                            gradientEdgeColor = Color.Transparent,
                            expansionFractionProvider = expansionFractionProvider,
                            modifier = Modifier.weight(1f)
                        )

                        // Minimal translucent more-options trigger
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(contentColor.copy(alpha = 0.12f))
                                .clickable(onClick = onMoreOptionsClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreHoriz,
                                contentDescription = stringResource(R.string.cd_player_more_options),
                                tint = contentColor.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = song.displayArtist,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = GoogleSansRounded,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = contentColor.copy(alpha = 0.72f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onArtistClick
                            )
                    )
                }

                // Glowing Word-by-Word Karaoke Lyrics Stage
                ComplexKaraokeLyricsStage(
                    lyrics = lyricsProvider(),
                    playbackPositionFlow = playbackPositionFlow,
                    syncOffsetMs = lyricsSyncOffset,
                    textColor = contentColor,
                    accentColor = accentColor,
                    onClick = onLyricsClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            // RIGHT AREA: Vertical Control Stack (Like, Previous, Play/Pause, Next)
            VerticalExpressiveControlStack(
                isPlayingProvider = isPlayingProvider,
                isFavoriteProvider = isFavoriteProvider,
                onFavoriteToggle = onFavoriteToggle,
                onPrevious = onPrevious,
                onPlayPause = onPlayPause,
                onNext = onNext,
                accentColor = accentColor,
                onAccentColor = baseTheme.onPrimary,
                secondaryContainerColor = baseTheme.secondaryContainer.copy(alpha = if (isDarkTheme) 0.88f else 0.92f),
                onSecondaryContainerColor = baseTheme.onSecondaryContainer,
                tertiaryColor = baseTheme.tertiary,
                onTertiaryColor = baseTheme.onTertiary,
                surfaceButtonColor = contentColor.copy(alpha = 0.16f),
                onSurfaceButtonColor = contentColor,
                modifier = Modifier
                    .width(68.dp)
                    .height(282.dp)
            )
        }

        // =====================================================================
        // LAYER 5: Minimal Chrome (Small Top Drag Handle + Top-Right Collapse Chevron)
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarTopPadding + 10.dp, start = 20.dp, end = 20.dp)
                .height(44.dp)
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
                    .background(Color.White.copy(alpha = 0.78f))
            )

            // Collapse chevron at top right
            val collapseOffsetY = remember { Animatable(0f) }
            val collapseScale = remember { Animatable(1f) }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.34f))
                    .border(
                        BorderStroke(0.75.dp, Color.White.copy(alpha = 0.24f)),
                        CircleShape
                    )
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
                    tint = Color.White,
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
 * Curved seekbar that hugs the concave/scooped circular arc at the bottom of the artwork.
 */
@Composable
private fun CurvedArcSeekBar(
    songId: String,
    artHeightDp: Dp,
    sagittaDp: Dp,
    currentPositionProvider: () -> Long,
    totalDurationValue: Long,
    songDurationHintMs: Long,
    isPlayingProvider: () -> Boolean,
    expansionFractionProvider: () -> Float,
    currentSheetState: PlayerSheetState,
    allowRealtimeUpdates: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    textColor: Color,
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
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.45f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "curvedThumbScale"
    )

    val effectivePositionMs = (currentProgress * durationForCalc).roundToLong().coerceIn(0L, displayDurationValue)
    val posStr = remember(effectivePositionMs / 1000L, isVisible) {
        if (isVisible) formatDuration((effectivePositionMs / 1000L) * 1000L) else "--:--"
    }
    val durStr = remember(displayDurationValue, isVisible) {
        if (isVisible) formatDuration(displayDurationValue) else "--:--"
    }

    val touchBandTopDp = artHeightDp - sagittaDp - 24.dp
    val touchBandHeightDp = sagittaDp + 52.dp
    val sagittaPx = with(density) { sagittaDp.toPx() }
    val topOffsetPx = with(density) { (artHeightDp - touchBandTopDp).toPx() }
    val edgeInsetPx = with(density) { 16.dp.toPx() }
    val hitTolerancePx = with(density) { 36.dp.toPx() }

    val latestOnSeek by rememberUpdatedState(onSeek)
    val latestDurationForCalc by rememberUpdatedState(durationForCalc)

    Box(
        modifier = modifier
            .padding(top = touchBandTopDp)
            .height(touchBandHeightDp)
            .pointerInput(songId, sagittaPx, topOffsetPx) {
                var lastQuantizedStep = -1
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val w = size.width.toFloat()
                    if (w <= 0f) return@awaitEachGesture

                    val s = sagittaPx
                    val halfW = w / 2f
                    val radius = (s / 2f) + ((halfW * halfW) / (2f * s))
                    val cx = halfW
                    val cy = topOffsetPx - radius

                    val dx = (down.position.x - cx).coerceIn(-radius * 0.99f, radius * 0.99f)
                    val curveYAtTouchX = cy + sqrt((radius * radius) - (dx * dx))

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

            val s = sagittaPx
            val halfW = w / 2f
            val radius = (s / 2f) + ((halfW * halfW) / (2f * s))
            val cx = halfW
            val cy = topOffsetPx - radius

            val effectiveHalfChord = (halfW - edgeInsetPx).coerceAtLeast(1f)
            val sinAlpha = (effectiveHalfChord / radius).coerceIn(-1f, 1f)
            val alphaDeg = Math.toDegrees(asin(sinAlpha.toDouble())).toFloat()

            // Start at left edge of arc (90 + alpha) and sweep to right edge (90 - alpha)
            val startAngleDeg = 90f + alphaDeg
            val totalSweepDeg = -2f * alphaDeg
            val activeSweepDeg = totalSweepDeg * currentProgress

            val arcRectTopLeft = Offset(cx - radius, cy - radius)
            val arcRectSize = Size(radius * 2f, radius * 2f)

            // 1. Subtle glass rim along the full arc edge
            val fullSinAlpha = (halfW / radius).coerceIn(-1f, 1f)
            val fullAlphaDeg = Math.toDegrees(asin(fullSinAlpha.toDouble())).toFloat()
            drawArc(
                color = Color.White.copy(alpha = 0.16f),
                startAngle = 90f + fullAlphaDeg,
                sweepAngle = -2f * fullAlphaDeg,
                useCenter = false,
                topLeft = arcRectTopLeft,
                size = arcRectSize,
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. Inactive progress track on the curve
            drawArc(
                color = inactiveColor,
                startAngle = startAngleDeg,
                sweepAngle = totalSweepDeg,
                useCenter = false,
                topLeft = arcRectTopLeft,
                size = arcRectSize,
                style = Stroke(width = 5.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Soft glow behind active progress arc
            if (currentProgress > 0.001f) {
                drawArc(
                    color = activeColor.copy(alpha = 0.34f),
                    startAngle = startAngleDeg,
                    sweepAngle = activeSweepDeg,
                    useCenter = false,
                    topLeft = arcRectTopLeft,
                    size = arcRectSize,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )

                // 4. Crisp active progress arc
                drawArc(
                    color = activeColor,
                    startAngle = startAngleDeg,
                    sweepAngle = activeSweepDeg,
                    useCenter = false,
                    topLeft = arcRectTopLeft,
                    size = arcRectSize,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 5. Glowing thumb riding along the curve
            val thumbAngleRad = Math.toRadians((startAngleDeg + activeSweepDeg).toDouble())
            val thumbX = cx + radius * cos(thumbAngleRad).toFloat()
            val thumbY = cy + radius * sin(thumbAngleRad).toFloat()
            val thumbCenter = Offset(thumbX, thumbY)

            drawCircle(
                color = activeColor.copy(alpha = if (isDragging) 0.42f else 0.26f),
                radius = 13.dp.toPx() * thumbScale,
                center = thumbCenter
            )
            drawCircle(
                color = activeColor,
                radius = 7.5.dp.toPx() * thumbScale,
                center = thumbCenter
            )
            drawCircle(
                color = Color.White,
                radius = 3.2.dp.toPx() * thumbScale,
                center = thumbCenter
            )
        }

        // Compact elapsed & total duration labels tucked just under the left and right ends of the curve
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp, start = 18.dp, end = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = posStr,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = textColor
            )
            Text(
                text = durStr,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = textColor
            )
        }
    }
}

/**
 * Karaoke word-by-word lyrics display where each active word glows and gets bigger.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComplexKaraokeLyricsStage(
    lyrics: Lyrics?,
    playbackPositionFlow: StateFlow<Long>,
    syncOffsetMs: Int,
    textColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPositionMs by playbackPositionFlow.collectAsStateWithLifecycle()
    val effectivePosition = currentPositionMs + syncOffsetMs

    val syncedLines = lyrics?.synced ?: emptyList()
    val plainLines = lyrics?.plain?.filter { it.isNotBlank() } ?: emptyList()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp, horizontal = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            syncedLines.isNotEmpty() -> {
                val currentLineIndex = remember(effectivePosition, syncedLines) {
                    syncedLines.indexOfLast { it.time <= effectivePosition }
                }
                val currentLine = if (currentLineIndex >= 0) syncedLines[currentLineIndex] else syncedLines.firstOrNull()
                val nextLine = if (currentLineIndex + 1 in syncedLines.indices) {
                    syncedLines[currentLineIndex + 1]
                } else {
                    null
                }
                val nextLineTimeMs = remember(currentLineIndex, syncedLines) {
                    if (currentLineIndex in 0 until syncedLines.lastIndex) {
                        syncedLines[currentLineIndex + 1].time.toLong()
                    } else if (currentLineIndex >= 0) {
                        syncedLines[currentLineIndex].time.toLong() + 7000L
                    } else {
                        7000L
                    }
                }

                val displayWords = remember(currentLine, nextLineTimeMs) {
                    resolveDisplayWords(currentLine, nextLineTimeMs)
                }
                val nextLineText = remember(nextLine) {
                    nextLine?.line?.let(::sanitizeLyricLineText).orEmpty()
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    AnimatedContent(
                        targetState = currentLine?.time ?: -1,
                        transitionSpec = {
                            (fadeIn(tween(260)) +
                                slideInVertically(
                                    initialOffsetY = { it / 3 },
                                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 220f)
                                )) togetherWith
                                (fadeOut(tween(180)) +
                                    slideOutVertically(
                                        targetOffsetY = { -it / 3 },
                                        animationSpec = tween(180)
                                    ))
                        },
                        label = "complexKaraokeLineTransition"
                    ) { lineKey ->
                        if (displayWords.isEmpty()) {
                            Text(
                                text = "•••",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = GoogleSansRounded,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp
                                ),
                                color = accentColor.copy(alpha = 0.75f)
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                maxLines = 4
                            ) {
                                displayWords.forEachIndexed { index, item ->
                                    val isCurrent = effectivePosition in item.startTimeMs until item.endTimeMs
                                    val isPast = effectivePosition >= item.endTimeMs

                                    key("${lineKey}_${item.startTimeMs}_$index") {
                                        val wordScale by animateFloatAsState(
                                            targetValue = when {
                                                isCurrent -> 1.24f
                                                isPast -> 1.0f
                                                else -> 0.94f
                                            },
                                            animationSpec = spring(
                                                dampingRatio = 0.56f,
                                                stiffness = Spring.StiffnessLow
                                            ),
                                            label = "karaokeWordScale"
                                        )
                                        val wordLiftPx by animateFloatAsState(
                                            targetValue = if (isCurrent) -6f else 0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.62f,
                                                stiffness = Spring.StiffnessMediumLow
                                            ),
                                            label = "karaokeWordLift"
                                        )
                                        val glowFraction by animateFloatAsState(
                                            targetValue = when {
                                                isCurrent -> 1f
                                                isPast -> 0.18f
                                                else -> 0f
                                            },
                                            animationSpec = tween(durationMillis = 160),
                                            label = "karaokeWordGlow"
                                        )
                                        val wordColor by animateColorAsState(
                                            targetValue = when {
                                                isCurrent -> Color.White
                                                isPast -> textColor.copy(alpha = 0.88f)
                                                else -> textColor.copy(alpha = 0.38f)
                                            },
                                            animationSpec = tween(durationMillis = 160),
                                            label = "karaokeWordColor"
                                        )

                                        Text(
                                            text = item.word,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontFamily = GoogleSansRounded,
                                                fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                                                fontSize = 22.sp,
                                                shadow = Shadow(
                                                    color = accentColor.copy(alpha = (0.95f * glowFraction).coerceIn(0f, 1f)),
                                                    offset = Offset.Zero,
                                                    blurRadius = if (isCurrent) 30f else 8f
                                                )
                                            ),
                                            color = wordColor,
                                            modifier = Modifier
                                                .drawBehind {
                                                    if (glowFraction > 0.05f) {
                                                        val radius = (size.maxDimension * 0.72f).coerceAtLeast(18f)
                                                        drawCircle(
                                                            brush = Brush.radialGradient(
                                                                colors = listOf(
                                                                    accentColor.copy(alpha = 0.34f * glowFraction),
                                                                    Color.Transparent
                                                                ),
                                                                center = center,
                                                                radius = radius
                                                            ),
                                                            radius = radius,
                                                            center = center
                                                        )
                                                    }
                                                }
                                                .graphicsLayer {
                                                    scaleX = wordScale
                                                    scaleY = wordScale
                                                    translationY = wordLiftPx
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Upcoming line preview for extra frosted depth
                    if (nextLineText.isNotBlank()) {
                        Text(
                            text = nextLineText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = textColor.copy(alpha = 0.34f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .blur(0.6.dp)
                        )
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
                    overflow = TextOverflow.Ellipsis
                )
            }

            else -> {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = textColor.copy(alpha = 0.10f),
                    border = BorderStroke(0.75.dp, textColor.copy(alpha = 0.16f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_lyrics_24),
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Tap for karaoke lyrics",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.Bold
                            ),
                            color = textColor.copy(alpha = 0.84f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Vertical control stack on the right: Like, Previous, Play/Pause, and Next
 * with Material 3 Expressive rounded pills, touch-responsive spring physics,
 * and shape morphing animations.
 */
@Composable
private fun VerticalExpressiveControlStack(
    isPlayingProvider: () -> Boolean,
    isFavoriteProvider: () -> Boolean,
    onFavoriteToggle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    accentColor: Color,
    onAccentColor: Color,
    secondaryContainerColor: Color,
    onSecondaryContainerColor: Color,
    tertiaryColor: Color,
    onTertiaryColor: Color,
    surfaceButtonColor: Color,
    onSurfaceButtonColor: Color,
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
            button -> baseWeight * 1.24f
            null -> baseWeight
            else -> baseWeight * 0.86f
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
        // 1. LIKE BUTTON
        val likeWeight by animateFloatAsState(
            targetValue = weightFor(VerticalControlButton.LIKE, 0.86f),
            animationSpec = weightSpec,
            label = "likeVerticalWeight"
        )
        val likeContainerColor by animateColorAsState(
            targetValue = if (isFavorite) tertiaryColor else surfaceButtonColor,
            animationSpec = tween(220),
            label = "likeContainerColor"
        )
        val likeIconColor by animateColorAsState(
            targetValue = if (isFavorite) onTertiaryColor else onSurfaceButtonColor,
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

        // 2. PREVIOUS BUTTON
        val prevWeight by animateFloatAsState(
            targetValue = weightFor(VerticalControlButton.PREVIOUS, 0.98f),
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
            containerColor = secondaryContainerColor,
            onClick = {
                activeButton = VerticalControlButton.PREVIOUS
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPrevious()
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint = onSecondaryContainerColor,
                modifier = Modifier.size(32.dp)
            )
        }

        // 3. PLAY / PAUSE BUTTON (Hero Expressive Morphing Button)
        val playWeight by animateFloatAsState(
            targetValue = weightFor(VerticalControlButton.PLAY_PAUSE, 1.34f),
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
            containerColor = accentColor,
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
                    tint = onAccentColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // 4. NEXT BUTTON
        val nextWeight by animateFloatAsState(
            targetValue = weightFor(VerticalControlButton.NEXT, 0.98f),
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
            containerColor = secondaryContainerColor,
            onClick = {
                activeButton = VerticalControlButton.NEXT
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onNext()
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                tint = onSecondaryContainerColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

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
            .border(
                BorderStroke(0.75.dp, Color.White.copy(alpha = 0.14f)),
                shape
            )
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
