package com.saurav.pixelmusic.presentation.components.lyrics

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.res.painterResource
import com.saurav.pixelmusic.R
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurav.pixelmusic.data.model.SyncedLine
import com.saurav.pixelmusic.lyrics.GoogleTranslateEngine
import com.saurav.pixelmusic.lyrics.MochyLyricsPreferences
import com.saurav.pixelmusic.lyrics.MochyLyricsStyle
import com.saurav.pixelmusic.lyrics.MochyRomanizer
import com.saurav.pixelmusic.ui.theme.GoogleSansRounded
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

/**
 * Mochy-powered lyrics view rendered inside PixelMusic's LyricsSheet viewport
 * (preserving PixelMusic's floating top song pill and bottom playback/Synced-Static controls).
 *
 * Supports all 11 Mochy showing styles, word-by-word rise/sweep/glow, duet alignment,
 * background vocals, 3-dot instrumental interlude indicator, on-the-fly Google Translate,
 * offline romanization, and provider attribution footer.
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
        .collectAsStateWithLifecycle(initialValue = MochyLyricsStyle.APPLE)
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

    val rawPositionMs by playbackPositionFlow.collectAsStateWithLifecycle()
    val effectivePosMs = ((positionOverrideMs ?: rawPositionMs) + lyricsSyncOffset).coerceAtLeast(0L)

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

    // Smooth auto-scroll keeping the active line near the upper-center focus zone
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0 && !listState.isScrollInProgress) {
            val targetIndex = (currentIndex).coerceIn(0, (lines.size).coerceAtLeast(0))
            runCatching {
                listState.animateScrollToItem(index = targetIndex, scrollOffset = -180)
            }
        }
    }

    val baseTextAlign = when (lyricsAlignment) {
        "center" -> TextAlign.Center
        "right" -> TextAlign.End
        else -> TextAlign.Start
    }
    val effectiveScale = if (immersiveMode) 1.15f else 1f
    val allowBlur = softLightEnabled && !performanceModeEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // Detect intro interlude before the first lyric line
    val firstLineTime = lines.firstOrNull()?.time?.toLong() ?: 0L
    val showIntroDots = firstLineTime >= 4_000L && effectivePosMs < firstLineTime - 300L

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
            val isActive = index == currentIndex
            val isPast = currentIndex >= 0 && index < currentIndex
            val distance = if (currentIndex < 0) 0 else abs(index - currentIndex)
            val nextLineTime = lines.getOrNull(index + 1)?.time?.toLong()
            val translationText = line.translation?.takeIf { it.isNotBlank() && showTranslationPref }
                ?: liveTranslations?.getOrNull(index)?.takeIf { it.isNotBlank() && it != line.line }
            val romanizationText = romanizedLines.getOrNull(index)

            val lineAlign = when {
                line.isBackground -> TextAlign.Center
                line.agent?.equals("v2", ignoreCase = true) == true -> TextAlign.End
                else -> baseTextAlign
            }

            MochyLyricLineItem(
                line = line,
                isActive = isActive,
                isPast = isPast,
                distance = distance,
                effectivePosMs = effectivePosMs,
                nextLineTimeMs = nextLineTime,
                style = style,
                textSizeSp = textSizeSp * effectiveScale,
                lineSpacing = lineSpacing,
                allowBlur = allowBlur,
                accentColor = accentColor,
                contentColor = contentColor,
                textAlign = lineAlign,
                translationText = translationText,
                romanizationText = romanizationText,
                onClick = { onLineClick(line) },
            )

            // Instrumental interlude dots between widely spaced lines (>= 6s gap)
            if (isActive && nextLineTime != null) {
                val lineEndMs = line.words?.lastOrNull()?.endTime?.toLong() ?: (line.time + 3_500L)
                val gapMs = nextLineTime - lineEndMs
                if (gapMs >= 5_000L && effectivePosMs > lineEndMs + 400L && effectivePosMs < nextLineTime - 300L) {
                    val gapProgress = ((effectivePosMs - lineEndMs).toFloat() / gapMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
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
            )
        }
    }
}

@Composable
private fun MochyLyricLineItem(
    line: SyncedLine,
    isActive: Boolean,
    isPast: Boolean,
    distance: Int,
    effectivePosMs: Long,
    nextLineTimeMs: Long?,
    style: MochyLyricsStyle,
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
        style == MochyLyricsStyle.NONE -> if (isPast) 0.42f else 0.55f
        style == MochyLyricsStyle.METRO_LYRICS -> if (isPast) 0.28f else 0.40f
        else -> when (distance) {
            1 -> if (isPast) 0.36f else 0.52f
            2 -> 0.34f
            else -> 0.22f
        }
    }
    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = if (style == MochyLyricsStyle.NONE) 80 else 280),
        label = "lyric_alpha",
    )

    val targetScale = when {
        !isActive -> if (style == MochyLyricsStyle.METRO_LYRICS) 0.94f else 0.96f
        style == MochyLyricsStyle.METRO_LYRICS -> 1.07f
        style == MochyLyricsStyle.APPLE || style == MochyLyricsStyle.APPLE_V2 || style == MochyLyricsStyle.MOCHY_1 -> 1.04f
        style == MochyLyricsStyle.NONE || style == MochyLyricsStyle.FADE -> 1.0f
        else -> 1.02f
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
        label = "lyric_scale",
    )

    val targetOffsetY = when {
        isActive && (style == MochyLyricsStyle.APPLE || style == MochyLyricsStyle.APPLE_V2 || style == MochyLyricsStyle.SLIDE) -> -4f
        else -> 0f
    }
    val animatedOffsetY by animateFloatAsState(
        targetValue = targetOffsetY,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "lyric_rise",
    )

    val targetOffsetX = when {
        isActive && style == MochyLyricsStyle.SLIDE -> 6f
        else -> 0f
    }
    val animatedOffsetX by animateFloatAsState(
        targetValue = targetOffsetX,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "lyric_slide",
    )

    val blurDp = if (allowBlur && !isActive && style != MochyLyricsStyle.NONE && style != MochyLyricsStyle.METRO_LYRICS) {
        when (distance) {
            1 -> 1.2f
            2 -> 2.4f
            else -> 3.6f
        }
    } else {
        0f
    }

    val fontSize = (if (line.isBackground) textSizeSp * 0.78f else textSizeSp).sp
    val lineHeight = (fontSize.value * lineSpacing).sp
    val fontWeight = when {
        line.isBackground -> FontWeight.Medium
        style == MochyLyricsStyle.METRO_LYRICS && isActive -> FontWeight.Black
        isActive -> FontWeight.ExtraBold
        else -> FontWeight.Bold
    }

    val pivotX = when (textAlign) {
        TextAlign.Center -> 0.5f
        TextAlign.End -> 1f
        else -> 0f
    }

    val glowShadow = if (isActive && (style == MochyLyricsStyle.GLOW || style == MochyLyricsStyle.APPLE || style == MochyLyricsStyle.MOCHY_1)) {
        Shadow(
            color = accentColor.copy(alpha = if (style == MochyLyricsStyle.GLOW) 0.55f else 0.30f),
            blurRadius = if (style == MochyLyricsStyle.GLOW) 22f else 12f,
        )
    } else {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animatedAlpha
                scaleX = animatedScale
                scaleY = animatedScale
                translationY = animatedOffsetY
                translationX = animatedOffsetX
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
        val words = line.words
        val useWordSync = isActive && !words.isNullOrEmpty() && style in WordSyncStyles

        if (useWordSync && words != null) {
            val annotated = buildAnnotatedString {
                words.forEachIndexed { idx, w ->
                    val wordStart = w.time.toLong()
                    val wordEnd = w.endTime?.toLong()
                        ?: words.getOrNull(idx + 1)?.time?.toLong()
                        ?: nextLineTimeMs
                        ?: (wordStart + 450L)
                    val spanMs = (wordEnd - wordStart).coerceAtLeast(80L)
                    val progress = ((effectivePosMs - wordStart).toFloat() / spanMs).coerceIn(0f, 1f)

                    val wordColor = when {
                        progress >= 0.99f -> accentColor
                        progress > 0f -> {
                            val blend = if (style == MochyLyricsStyle.KARAOKE) 1f else progress
                            androidx.compose.ui.graphics.lerp(
                                contentColor.copy(alpha = 0.42f),
                                accentColor,
                                blend,
                            )
                        }
                        else -> contentColor.copy(alpha = 0.36f)
                    }
                    val wordShadow = if (progress in 0.05f..0.99f && (style == MochyLyricsStyle.APPLE || style == MochyLyricsStyle.MOCHY_1 || style == MochyLyricsStyle.GLOW)) {
                        Shadow(color = accentColor.copy(alpha = 0.45f), blurRadius = 14f)
                    } else {
                        glowShadow
                    }

                    withStyle(
                        SpanStyle(
                            color = wordColor,
                            fontWeight = if (progress > 0f) FontWeight.ExtraBold else FontWeight.Bold,
                            shadow = wordShadow,
                        )
                    ) {
                        append(w.word)
                    }
                    if (idx < words.lastIndex) {
                        val nextStartsNew = words[idx + 1].startsNewWord
                        if (nextStartsNew) append(" ")
                    }
                }
            }
            Text(
                text = annotated,
                style = TextStyle(
                    fontFamily = GoogleSansRounded,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontWeight = fontWeight,
                    fontStyle = if (line.isBackground) FontStyle.Italic else FontStyle.Normal,
                    textAlign = textAlign,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            val activeBrush = if (isActive && style == MochyLyricsStyle.MOCHY_1) {
                Brush.horizontalGradient(
                    colors = listOf(accentColor, MaterialTheme.colorScheme.tertiary, accentColor)
                )
            } else {
                null
            }
            val textStyle = if (activeBrush != null) {
                TextStyle(
                    brush = activeBrush,
                    fontFamily = GoogleSansRounded,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontWeight = fontWeight,
                    fontStyle = if (line.isBackground) FontStyle.Italic else FontStyle.Normal,
                    textAlign = textAlign,
                    shadow = glowShadow,
                )
            } else {
                TextStyle(
                    color = if (isActive) accentColor else contentColor,
                    fontFamily = GoogleSansRounded,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontWeight = fontWeight,
                    fontStyle = if (line.isBackground) FontStyle.Italic else FontStyle.Normal,
                    textAlign = textAlign,
                    shadow = glowShadow,
                )
            }
            Text(
                text = line.line,
                style = textStyle,
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

private val WordSyncStyles = setOf(
    MochyLyricsStyle.APPLE,
    MochyLyricsStyle.APPLE_V2,
    MochyLyricsStyle.MOCHY_1,
    MochyLyricsStyle.KARAOKE,
    MochyLyricsStyle.LEGACY,
    MochyLyricsStyle.GLOW,
    MochyLyricsStyle.METRO_LYRICS,
)
