package com.saurav.pixelmusic.presentation.components.lyrics

import android.os.Build
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurav.pixelmusic.R
import com.saurav.pixelmusic.data.model.SyncedLine
import com.saurav.pixelmusic.lyrics.GoogleTranslateEngine
import com.saurav.pixelmusic.lyrics.MochyLyricsPreferences
import com.saurav.pixelmusic.lyrics.MochyLyricsStyle
import com.saurav.pixelmusic.lyrics.MochyRomanizer
import com.saurav.pixelmusic.ui.theme.GoogleSansRounded
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

private val MochySmoothRecenterEasing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
private val MochyLegacyRecenterEasing = CubicBezierEasing(0.32f, 0.72f, 0.0f, 1.0f)

/**
 * Mochy-powered synced lyrics view rendered inside PixelMusic's LyricsSheet viewport
 * (preserving PixelMusic's floating top song pill and bottom playback/Synced-Static controls).
 *
 * Authentically renders Mochy's 4 styles:
 * - Style 1 (MOTION): word rise + held-note swell + AGSL bloom + staggered line cascade
 * - Style 2 (LEGACY): AGSL wave-lift/zoom + trailing bloom + unified scroll
 * - Boring (BORING): Style 1's word rise & staggered cascade without glow
 * - Plain (PLAIN): Style 2's smooth sweep without glow or wave
 */
@Composable
fun MochySyncedLyricsView(
    lines: List<SyncedLine>,
    listState: LazyListState,
    playbackPositionFlow: StateFlow<Long>,
    lyricsSyncOffset: Int,
    positionOverrideMs: Long?,
    accentColor: Color,
    contentColor: Color,
    providerName: String?,
    lyricsAlignment: String,
    showTranslationPref: Boolean,
    showRomanizationPref: Boolean,
    performanceModeEnabled: Boolean,
    immersiveMode: Boolean,
    onLineClick: (SyncedLine) -> Unit,
    onOpenStyleStudio: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(top = 128.dp, bottom = 110.dp),
) {
    val context = LocalContext.current
    val style by remember(context) { MochyLyricsPreferences.styleFlow(context) }
        .collectAsStateWithLifecycle(initialValue = MochyLyricsStyle.MOTION)
    val textSizeSp by remember(context) { MochyLyricsPreferences.textSizeFlow(context) }
        .collectAsStateWithLifecycle(initialValue = 26f)
    val lineSpacing by remember(context) { MochyLyricsPreferences.lineSpacingFlow(context) }
        .collectAsStateWithLifecycle(initialValue = 1.25f)
    val softLightEnabled by remember(context) { MochyLyricsPreferences.softLightFlow(context) }
        .collectAsStateWithLifecycle(initialValue = true)
    val autoTranslate by remember(context) { MochyLyricsPreferences.autoTranslateFlow(context) }
        .collectAsStateWithLifecycle(initialValue = false)
    val translateLang by remember(context) { MochyLyricsPreferences.translateLanguageFlow(context) }
        .collectAsStateWithLifecycle(initialValue = "en")
    val autoRomanize by remember(context) { MochyLyricsPreferences.autoRomanizeFlow(context) }
        .collectAsStateWithLifecycle(initialValue = false)
    val romanizationAsMain by remember(context) { MochyLyricsPreferences.romanizationAsMainFlow(context) }
        .collectAsStateWithLifecycle(initialValue = false)

    val rawPositionMs by playbackPositionFlow.collectAsStateWithLifecycle()
    val effectivePosMs = ((positionOverrideMs ?: rawPositionMs) + lyricsSyncOffset).coerceAtLeast(0L)
    val currentPosProvider by rememberUpdatedState { effectivePosMs }

    // Live Google Translate cache for this song + language
    var liveTranslations by remember(lines, translateLang, autoTranslate) {
        mutableStateOf<List<String>?>(null)
    }
    LaunchedEffect(lines, translateLang, autoTranslate) {
        if (autoTranslate && lines.isNotEmpty()) {
            val sourceLines = lines.map { it.line }
            liveTranslations = GoogleTranslateEngine.translate(sourceLines, targetLanguage = translateLang)
        } else {
            liveTranslations = null
        }
    }

    // Precompute offline romanization when enabled
    val romanizedLines = remember(lines, autoRomanize, showRomanizationPref) {
        if (!autoRomanize && !showRomanizationPref) emptyList()
        else lines.map { line ->
            line.romanization?.takeIf { it.isNotBlank() }
                ?: if (autoRomanize) MochyRomanizer.romanize(line.line) else null
        }
    }

    val currentIndex by remember(lines, effectivePosMs) {
        derivedStateOf {
            if (lines.isEmpty()) -1
            else {
                var idx = -1
                for (i in lines.indices) {
                    if (lines[i].time <= effectivePosMs) idx = i else break
                }
                idx
            }
        }
    }

    // Mochy's Style 1 / Boring staggered line cascade
    val cascade = style.risesWords
    val stagger = remember(lines.size) { MochyLyricsStagger(lines.size + 2) }
    LaunchedEffect(stagger) { stagger.run() }
    LaunchedEffect(cascade) { if (!cascade) stagger.reset() }

    // Detect intro interlude before the first lyric line
    val firstLineTime = lines.firstOrNull()?.time?.toLong() ?: 0L
    val showIntroDots = firstLineTime >= 4_000L && effectivePosMs < firstLineTime - 300L
    val introOffset = if (showIntroDots) 1 else 0

    // Smooth auto-scroll keeping the active line in the upper-center focus zone,
    // driving MochyLyricsStagger on Style 1 & Boring so lines below cascade into place.
    LaunchedEffect(currentIndex, style) {
        if (currentIndex < 0 || listState.isScrollInProgress) return@LaunchedEffect
        val targetItemIndex = (currentIndex + introOffset).coerceIn(0, (lines.size + introOffset).coerceAtLeast(0))
        val visible = listState.layoutInfo.visibleItemsInfo
        val targetInfo = visible.firstOrNull { it.index == targetItemIndex }
        val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
        val desiredOffset = (viewportHeight * 0.24f).toInt()

        if (targetInfo != null) {
            val distance = (targetInfo.offset - desiredOffset).toFloat()
            if (abs(distance) > 2f) {
                stagger.anchorRow = targetItemIndex
                val easing = if (cascade) MochySmoothRecenterEasing else MochyLegacyRecenterEasing
                runCatching {
                    listState.scroll {
                        var previous = 0f
                        animate(
                            initialValue = 0f,
                            targetValue = distance,
                            animationSpec = tween(durationMillis = 520, easing = easing),
                        ) { value, _ ->
                            val consumed = scrollBy(value - previous)
                            previous += consumed
                            if (cascade && consumed != 0f) {
                                for (item in listState.layoutInfo.visibleItemsInfo) {
                                    if (item.index > targetItemIndex) {
                                        stagger.holdBack(item.index, consumed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            runCatching {
                listState.animateScrollToItem(index = targetItemIndex, scrollOffset = -desiredOffset)
            }
        }
    }

    val baseTextAlign = when (lyricsAlignment) {
        "center" -> TextAlign.Center
        "right" -> TextAlign.End
        else -> TextAlign.Start
    }
    val effectiveScale = if (immersiveMode) 1.12f else 1f
    val allowBlur = softLightEnabled && !performanceModeEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy((14f * lineSpacing).dp),
    ) {
        if (showIntroDots) {
            item(key = "mochy_intro_dots") {
                val progress = (effectivePosMs.toFloat() / firstLineTime.coerceAtLeast(1L)).coerceIn(0f, 1f)
                MochyInterludeDots(
                    progress = progress,
                    color = accentColor,
                    alignment = baseTextAlign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
            }
        }

        itemsIndexed(
            items = lines,
            key = { index, line -> "${index}_${line.time}" },
        ) { index, line ->
            val listItemIndex = index + introOffset
            val isActive = index == currentIndex
            val isPast = currentIndex >= 0 && index < currentIndex
            val distance = if (currentIndex < 0) 0 else abs(index - currentIndex)
            val nextLineTime = lines.getOrNull(index + 1)?.time?.toLong()
            val lineEndMs = line.words?.lastOrNull()?.endTime?.toLong()
                ?: nextLineTime
                ?: (line.time.toLong() + 3_500L)

            val translationText = line.translation?.takeIf { it.isNotBlank() && showTranslationPref }
                ?: liveTranslations?.getOrNull(index)?.takeIf { it.isNotBlank() && it != line.line }
            val romanizationText = romanizedLines.getOrNull(index)?.takeIf { it.isNotBlank() && it != line.line }

            val swapRom = romanizationAsMain && !romanizationText.isNullOrBlank()
            val primaryText = if (swapRom) romanizationText!! else line.line
            val secondaryRomText = if (swapRom) line.line else romanizationText
            val effectiveWords = if (swapRom) null else line.words

            val lineAlign = when {
                line.isBackground -> TextAlign.Center
                line.agent?.equals("v2", ignoreCase = true) == true -> TextAlign.End
                else -> baseTextAlign
            }

            MochyLyricLineItem(
                primaryText = primaryText,
                words = effectiveWords,
                lineStartMs = line.time.toLong(),
                lineEndMs = lineEndMs,
                isBackground = line.isBackground,
                isActive = isActive,
                isPast = isPast,
                distance = distance,
                positionProvider = currentPosProvider,
                style = style,
                stagger = stagger,
                staggerRowIndex = listItemIndex,
                textSizeSp = textSizeSp * effectiveScale,
                lineSpacing = lineSpacing,
                allowBlur = allowBlur,
                accentColor = accentColor,
                contentColor = contentColor,
                textAlign = lineAlign,
                translationText = translationText,
                romanizationText = secondaryRomText,
                onClick = { onLineClick(line) },
            )

            // Instrumental interlude dots between widely spaced lines (>= 5s gap)
            if (isActive && nextLineTime != null) {
                val sungEndMs = line.words?.lastOrNull()?.endTime?.toLong() ?: (line.time + 3_500L)
                val gapMs = nextLineTime - sungEndMs
                if (gapMs >= 5_000L && effectivePosMs > sungEndMs + 400L && effectivePosMs < nextLineTime - 300L) {
                    val gapProgress = ((effectivePosMs - sungEndMs).toFloat() / gapMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
                    Spacer(Modifier.height(10.dp))
                    MochyInterludeDots(
                        progress = gapProgress,
                        color = accentColor,
                        alignment = baseTextAlign,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item(key = "mochy_source_footer") {
            MochySourceFooter(
                providerName = providerName,
                styleName = style.displayName,
                accentColor = accentColor,
                contentColor = contentColor,
                onOpenStudio = onOpenStyleStudio,
                modifier = Modifier.graphicsLayer {
                    if (cascade) {
                        translationY = stagger.lagOf(lines.size + introOffset)
                    }
                },
            )
        }
    }
}

@Composable
private fun MochyLyricLineItem(
    primaryText: String,
    words: List<com.saurav.pixelmusic.data.model.SyncedWord>?,
    lineStartMs: Long,
    lineEndMs: Long,
    isBackground: Boolean,
    isActive: Boolean,
    isPast: Boolean,
    distance: Int,
    positionProvider: () -> Long,
    style: MochyLyricsStyle,
    stagger: MochyLyricsStagger,
    staggerRowIndex: Int,
    textSizeSp: Float,
    lineSpacing: Float,
    allowBlur: Boolean,
    accentColor: Color,
    contentColor: Color,
    textAlign: TextAlign,
    translationText: String?,
    romanizationText: String?,
    onClick: () -> Unit,
) {
    val targetAlpha = when {
        isActive -> 1f
        isPast -> 0.28f
        else -> when (distance) {
            1 -> 0.52f
            2 -> 0.38f
            else -> 0.26f
        }
    }
    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 280),
        label = "lyric_alpha",
    )

    val targetScale = if (isActive) 1.04f else 0.96f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
        label = "lyric_scale",
    )

    val blurDp = if (allowBlur && !isActive) {
        when (distance) {
            1 -> 1.2f
            2 -> 2.4f
            else -> 3.6f
        }
    } else {
        0f
    }

    val fontSize = (if (isBackground) textSizeSp * 0.78f else textSizeSp).sp
    val lineHeight = (fontSize.value * lineSpacing).sp
    val fontWeight = when {
        isBackground -> FontWeight.Medium
        isActive -> FontWeight.ExtraBold
        else -> FontWeight.Bold
    }

    val pivotX = when (textAlign) {
        TextAlign.Center -> 0.5f
        TextAlign.End -> 1f
        else -> 0f
    }

    val lineTextStyle = TextStyle(
        fontFamily = GoogleSansRounded,
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontWeight = fontWeight,
        fontStyle = if (isBackground) FontStyle.Italic else FontStyle.Normal,
        textAlign = textAlign,
        color = if (isActive) accentColor else contentColor,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animatedAlpha
                scaleX = animatedScale
                scaleY = animatedScale
                if (style.risesWords) {
                    translationY = stagger.lagOf(staggerRowIndex)
                }
                transformOrigin = TransformOrigin(pivotX, 0.5f)
            }
            .then(if (blurDp > 0.2f) Modifier.blur(blurDp.dp) else Modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
    ) {
        if (isActive) {
            MochyKaraokeSweepLine(
                text = primaryText,
                words = words,
                lineStartMs = lineStartMs,
                lineEndMs = lineEndMs,
                positionProvider = positionProvider,
                style = style,
                accentColor = accentColor,
                unsungColor = contentColor.copy(alpha = 0.36f),
                textStyle = lineTextStyle,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = primaryText,
                style = lineTextStyle,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (!romanizationText.isNullOrBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = romanizationText,
                style = TextStyle(
                    fontFamily = GoogleSansRounded,
                    fontSize = (fontSize.value * 0.56f).coerceIn(12f, 18f).sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    textAlign = textAlign,
                    color = (if (isActive) accentColor else contentColor).copy(alpha = 0.72f),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (!translationText.isNullOrBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = translationText,
                style = TextStyle(
                    fontFamily = GoogleSansRounded,
                    fontSize = (fontSize.value * 0.60f).coerceIn(13f, 19f).sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = textAlign,
                    color = (if (isActive) contentColor else contentColor.copy(alpha = 0.78f)).copy(alpha = 0.80f),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Mochy's 3-dot animated instrumental interlude indicator.
 */
@Composable
fun MochyInterludeDots(
    progress: Float,
    color: Color,
    alignment: TextAlign,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "interlude_dots")
    val pulse by infinite.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "interlude_pulse",
    )

    val arrangement = when (alignment) {
        TextAlign.Center -> Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        TextAlign.End -> Arrangement.spacedBy(10.dp, Alignment.End)
        else -> Arrangement.spacedBy(10.dp, Alignment.Start)
    }

    Row(
        modifier = modifier.padding(vertical = 6.dp),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { idx ->
            val threshold = (idx + 1) / 3f
            val dotAlpha = when {
                progress >= threshold -> 0.95f
                progress >= idx / 3f -> 0.45f + 0.5f * ((progress - idx / 3f) * 3f)
                else -> 0.25f
            }
            val dotScale = if (progress >= idx / 3f && progress < threshold) pulse else 1f
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .graphicsLayer {
                        alpha = dotAlpha
                        scaleX = dotScale
                        scaleY = dotScale
                    }
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
fun MochySourceFooter(
    providerName: String?,
    styleName: String,
    accentColor: Color,
    contentColor: Color,
    onOpenStudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!providerName.isNullOrBlank()) {
            Text(
                text = "Lyrics provided by $providerName",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = GoogleSansRounded,
                color = contentColor.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
            )
        }
        Surface(
            onClick = onOpenStudio,
            shape = RoundedCornerShape(50),
            color = accentColor.copy(alpha = 0.14f),
            contentColor = accentColor,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_auto_awesome_24),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Style: $styleName · Customize",
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
