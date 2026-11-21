package com.saurav.pixelmusic.presentation.components.lyrics

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.CompositingStrategy as LayerCompositingStrategy
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.saurav.pixelmusic.data.model.SyncedWord
import com.saurav.pixelmusic.lyrics.MochyLyricsStyle
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

// ============================================================================
// 1. MochyLyricsStagger — Style 1 & Boring's critically damped line cascade
// ============================================================================

private const val StaggerLagPerRowMs = 55f
private const val StaggerMaxRows = 9

@Stable
internal class MochyLyricsStagger(rowCount: Int) {
    private val lags = FloatArray(rowCount.coerceAtLeast(1))
    private val velocities = FloatArray(rowCount.coerceAtLeast(1))

    private val version = mutableIntStateOf(0)
    private val moving = mutableStateOf(false)

    var anchorRow = -1

    fun lagOf(row: Int): Float {
        version.intValue
        return if (row in lags.indices) lags[row] else 0f
    }

    fun holdBack(row: Int, px: Float) {
        if (row !in lags.indices) return
        lags[row] += px
        version.intValue++
        moving.value = true
    }

    fun reset() {
        lags.fill(0f)
        velocities.fill(0f)
        version.intValue++
        moving.value = false
    }

    suspend fun run() {
        while (true) {
            snapshotFlow { moving.value }.first { it }
            var lastNanos = 0L
            var still: Boolean
            do {
                still = withFrameNanos { now ->
                    val dt = if (lastNanos == 0L) 0.016f
                    else ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.064f)
                    lastNanos = now
                    step(dt).also { version.intValue++ }
                }
            } while (still || anyLag())
            moving.value = false
        }
    }

    private fun step(dt: Float): Boolean {
        var any = false
        for (row in lags.indices) {
            val lag = lags[row]
            val v = velocities[row]
            if (lag == 0f && v == 0f) continue
            val k = (row - anchorRow).coerceIn(1, StaggerMaxRows)
            val omega = 1000f / (StaggerLagPerRowMs * k)
            val e = exp(-omega * dt)
            val tmp = (v + omega * lag) * dt
            val nextLag = (lag + tmp) * e
            val nextV = (v - omega * tmp) * e
            if (abs(nextLag) < 0.3f && abs(nextV) < 4f) {
                lags[row] = 0f
                velocities[row] = 0f
            } else {
                lags[row] = nextLag
                velocities[row] = nextV
                any = true
            }
        }
        return any
    }

    private fun anyLag(): Boolean = lags.any { it != 0f }
}

// ============================================================================
// 2. Mochy AGSL Shaders — Style 1 (BloomAgsl) & Style 2 (LegacyWaveAgsl)
// ============================================================================

private const val BloomAgsl = """
uniform shader content;
uniform float2 layerSize;
uniform float headX;
uniform float lineTop;
uniform float lineBottom;
uniform float lineStart;
uniform float prevTop;
uniform float prevBottom;
uniform float prevEnd;
uniform float hasPrev;
uniform float flowDir;
uniform float bloom;
uniform float bloomR;
uniform float bloomLag;
uniform float bloomSpan;

half4 sampleIn(float2 p) {
    if (p.x < 0.0 || p.y < 0.0 || p.x > layerSize.x || p.y > layerSize.y) return half4(0.0);
    return content.eval(p);
}

half4 main(float2 coord) {
    half4 c = sampleIn(coord);

    float onCur  = (coord.y < lineTop || coord.y > lineBottom) ? 0.0 : 1.0;
    float onPrev = (hasPrev < 0.5 || coord.y < prevTop || coord.y > prevBottom) ? 0.0 : 1.0;
    float onLine = max(onCur, onPrev);
    if (onLine <= 0.0) return c;

    float dxCur  = (coord.x - headX) * flowDir;
    float dxPrev = -((prevEnd - coord.x) + (headX - lineStart)) * flowDir;
    float dx = onPrev > 0.5 ? dxPrev : dxCur;

    float bdx = dx + bloomLag;
    float ba = min(1.0, abs(bdx) / max(bloomSpan, 1.0));
    float bw = 1.0 - ba;
    float bbell = bw * bw * (3.0 - 2.0 * bw);
    if (bbell <= 0.0) return c;

    half4 b = sampleIn(coord + float2(bloomR, bloomR))
            + sampleIn(coord + float2(bloomR, -bloomR))
            + sampleIn(coord + float2(-bloomR, bloomR))
            + sampleIn(coord + float2(-bloomR, -bloomR))
            + sampleIn(coord + float2(bloomR, 0.0))
            + sampleIn(coord - float2(bloomR, 0.0))
            + sampleIn(coord + float2(0.0, bloomR))
            + sampleIn(coord - float2(0.0, bloomR));
    return c + b * (bloom * bbell * 0.125);
}
"""

private const val LegacyWaveAgsl = """
uniform shader content;
uniform float2 layerSize;
uniform float headX;
uniform float spanBehind;
uniform float spanAhead;
uniform float liftPx;
uniform float zoomAmt;
uniform float baseY;
uniform float lineTop;
uniform float lineBottom;
uniform float bloom;
uniform float bloomR;
uniform float bloomLag;
uniform float bloomSpan;
uniform float prevTop;
uniform float prevBottom;
uniform float prevEnd;
uniform float prevBaseY;
uniform float hasPrev;
uniform float lineStart;
uniform float flowDir;

half4 sampleIn(float2 p) {
    if (p.x < 0.0 || p.y < 0.0 || p.x > layerSize.x || p.y > layerSize.y) return half4(0.0);
    return content.eval(p);
}

half4 main(float2 coord) {
    float onCur  = (coord.y < lineTop || coord.y > lineBottom) ? 0.0 : 1.0;
    float onPrev = (hasPrev < 0.5 || coord.y < prevTop || coord.y > prevBottom) ? 0.0 : 1.0;
    float onLine = max(onCur, onPrev);

    float dxCur  = (coord.x - headX) * flowDir;
    float dxPrev = -((prevEnd - coord.x) + (headX - lineStart)) * flowDir;
    float dx = onPrev > 0.5 ? dxPrev : dxCur;
    float bY = onPrev > 0.5 ? prevBaseY : baseY;

    float span = dx < 0.0 ? spanBehind : spanAhead;
    float a = min(1.0, abs(dx) / max(span, 1.0));
    float w = 1.0 - a;
    float bell = w * w * (3.0 - 2.0 * w) * onLine;

    float2 p = coord;
    p.y += liftPx * bell;
    p.y = bY + (p.y - bY) / (1.0 + zoomAmt * bell);

    half4 c = sampleIn(p);

    if (bloom > 0.0 && onLine > 0.0) {
        float bdx = dx + bloomLag;
        float ba = min(1.0, abs(bdx) / max(bloomSpan, 1.0));
        float bw = 1.0 - ba;
        float bbell = bw * bw * (3.0 - 2.0 * bw) * onLine;

        half4 b = sampleIn(p + float2(bloomR, bloomR))
                + sampleIn(p + float2(bloomR, -bloomR))
                + sampleIn(p + float2(-bloomR, bloomR))
                + sampleIn(p + float2(-bloomR, -bloomR))
                + sampleIn(p + float2(bloomR, 0.0))
                + sampleIn(p - float2(bloomR, 0.0))
                + sampleIn(p + float2(0.0, bloomR))
                + sampleIn(p - float2(0.0, bloomR));
        c += b * (bloom * bbell * 0.125);
    }
    return c;
}
"""

private val MotionBleed = 10.dp
private val WordWaitDrop = 4.dp
private val HeldLift = 2.dp
private const val HeldZoom = 0.08f
private const val LegacyWaveLiftPx = 5.5f
private const val LegacyWaveZoom = 0.10f
private const val WaveCharsBehind = 4f
private const val WaveCharsAhead = 3f
private const val BloomRadiusPx = 2.8f
private const val BloomStrength = 1.55f
private const val BloomLagChars = 1.6f
private const val BloomSpanChars = 3.5f
private const val EdgeSoftPx = 26f
private const val RiseFollowSec = 0.085f
private const val SwellFollowSec = 0.18f

private data class TimedSpan(
    val startChar: Int,
    val endChar: Int,
    val startMs: Long,
    val endMs: Long,
)

private fun buildWordRanges(text: String): List<IntRange> {
    if (text.isEmpty()) return emptyList()
    val out = ArrayList<IntRange>()
    var i = 0
    while (i < text.length) {
        if (text[i].isWhitespace()) {
            i++
            continue
        }
        val start = i
        while (i < text.length && !text[i].isWhitespace()) i++
        out.add(start until i)
    }
    return out
}

private fun buildTimedSpans(
    text: String,
    words: List<SyncedWord>?,
    lineStartMs: Long,
    lineEndMs: Long,
): List<TimedSpan> {
    if (text.isEmpty()) return emptyList()
    val safeEnd = lineEndMs.coerceAtLeast(lineStartMs + 400L)
    if (words.isNullOrEmpty()) {
        val wordRanges = buildWordRanges(text)
        if (wordRanges.isEmpty()) return listOf(TimedSpan(0, text.length, lineStartMs, safeEnd))
        val totalChars = wordRanges.sumOf { (it.last - it.first + 1) }.coerceAtLeast(1)
        val totalDur = (safeEnd - lineStartMs).coerceAtLeast(300L)
        var cursorMs = lineStartMs
        return wordRanges.mapIndexed { idx, range ->
            val len = range.last - range.first + 1
            val share = (totalDur * len / totalChars).coerceAtLeast(60L)
            val end = if (idx == wordRanges.lastIndex) safeEnd else (cursorMs + share).coerceAtMost(safeEnd)
            TimedSpan(range.first, range.last + 1, cursorMs, end).also { cursorMs = end }
        }
    }

    val spans = ArrayList<TimedSpan>(words.size)
    var searchFrom = 0
    for (i in words.indices) {
        val w = words[i]
        val token = w.word.trim()
        val wStartMs = w.time.toLong()
        val wEndMs = w.endTime?.toLong()
            ?: words.getOrNull(i + 1)?.time?.toLong()
            ?: safeEnd
        if (token.isEmpty()) continue
        val found = text.indexOf(token, startIndex = searchFrom, ignoreCase = true)
        if (found >= 0) {
            val endIdx = (found + token.length).coerceAtMost(text.length)
            spans.add(TimedSpan(found, endIdx, wStartMs, wEndMs.coerceAtLeast(wStartMs + 60L)))
            searchFrom = endIdx
        }
    }
    if (spans.isEmpty()) {
        return listOf(TimedSpan(0, text.length, lineStartMs, safeEnd))
    }
    return spans
}

@Stable
private class SweepState(wordCount: Int) {
    val headChar = mutableFloatStateOf(0f)
    val glow = mutableFloatStateOf(0f)
    val rise = FloatArray(wordCount.coerceAtLeast(1))
    val riseVel = FloatArray(wordCount.coerceAtLeast(1))
    val swell = FloatArray(wordCount.coerceAtLeast(1))
    val swellVel = FloatArray(wordCount.coerceAtLeast(1))
    val tick = mutableIntStateOf(0)
}

private fun smoothstep(t: Float): Float {
    val c = t.coerceIn(0f, 1f)
    return c * c * (3f - 2f * c)
}

private fun stepCriticallyDamped(
    current: Float,
    velocity: Float,
    target: Float,
    followSec: Float,
    dt: Float,
): Pair<Float, Float> {
    val omega = 1f / followSec
    val diff = current - target
    val e = exp(-omega * dt)
    val tmp = (velocity + omega * diff) * dt
    val next = target + (diff + tmp) * e
    val nextV = (velocity - omega * tmp) * e
    return if (abs(next - target) < 0.001f && abs(nextV) < 0.01f) {
        target to 0f
    } else {
        next to nextV
    }
}

private fun TextLayoutResult.sweepHeadX(headChar: Float, textLength: Int): Triple<Int, Float, Boolean> {
    val clamped = headChar.coerceIn(0f, textLength.toFloat())
    val idx = clamped.toInt().coerceIn(0, (textLength - 1).coerceAtLeast(0))
    val line = getLineForOffset(idx).coerceAtMost(lineCount - 1)
    val rtl = getBidiRunDirection(idx) == androidx.compose.ui.text.style.ResolvedTextDirection.Rtl
    val frac = clamped - clamped.toInt()
    val box = getBoundingBox(idx)
    val x = if (clamped >= textLength) {
        if (rtl) getLineLeft(line) else getLineRight(line)
    } else if (rtl) {
        box.right - (box.right - box.left) * frac
    } else {
        box.left + (box.right - box.left) * frac
    }
    return Triple(line, x, rtl)
}

private fun Modifier.layerBleed(bleedDp: androidx.compose.ui.unit.Dp): Modifier = layout { measurable, constraints ->
    val pad = bleedDp.roundToPx()
    val placeable = measurable.measure(constraints)
    layout(placeable.width + pad * 2, placeable.height + pad * 2) {
        placeable.place(pad, pad)
    }
}.layout { measurable, constraints ->
    val pad = bleedDp.roundToPx()
    val placeable = measurable.measure(constraints.offset(horizontal = pad * 2, vertical = pad * 2))
    layout((placeable.width - pad * 2).coerceAtLeast(0), (placeable.height - pad * 2).coerceAtLeast(0)) {
        placeable.place(-pad, -pad)
    }
}

/**
 * Authentic Mochy Karaoke & Motion Line Renderer supporting all 4 Mochy styles:
 * - [MochyLyricsStyle.MOTION] ("Style 1"): per-word vertical rise + held-note swell + AGSL bloom shader
 * - [MochyLyricsStyle.LEGACY] ("Style 2"): AGSL horizontal wave-lift/zoom + trailing bloom shader
 * - [MochyLyricsStyle.BORING] ("Boring"): Style 1's per-word vertical rise + swell, no glow shader
 * - [MochyLyricsStyle.PLAIN] ("Plain"): Style 2's smooth feathered karaoke sweep, no glow or wave
 */
@Composable
internal fun MochyKaraokeSweepLine(
    text: String,
    words: List<SyncedWord>?,
    lineStartMs: Long,
    lineEndMs: Long,
    positionProvider: () -> Long,
    style: MochyLyricsStyle,
    accentColor: Color,
    unsungColor: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val wordRanges = remember(text) { buildWordRanges(text) }
    val timedSpans = remember(text, words, lineStartMs, lineEndMs) {
        buildTimedSpans(text, words, lineStartMs, lineEndMs)
    }
    val state = remember(text, wordRanges.size) { SweepState(wordRanges.size) }
    val layoutHolder = remember(text) { arrayOfNulls<TextLayoutResult>(1) }

    val risesWords = style.risesWords
    val shaded = style.shaded

    // 60-120fps frame driver for sweep head, glow intensity, and per-word rise/swell springs
    LaunchedEffect(text, timedSpans, style) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f
                else ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNanos = now

                val posMs = positionProvider()
                var head = 0f
                var targetGlow = 0f

                if (timedSpans.isNotEmpty()) {
                    val firstStart = timedSpans.first().startMs
                    val lastEnd = timedSpans.last().endMs
                    if (posMs <= firstStart) {
                        head = 0f
                        targetGlow = 0f
                    } else if (posMs >= lastEnd) {
                        head = text.length.toFloat()
                        val fadeAfter = ((posMs - lastEnd) / 350f).coerceIn(0f, 1f)
                        targetGlow = (1f - fadeAfter) * 0.45f
                    } else {
                        for (s in timedSpans) {
                            if (posMs < s.startMs) break
                            if (posMs >= s.endMs) {
                                head = s.endChar.toFloat()
                            } else {
                                val spanDur = (s.endMs - s.startMs).coerceAtLeast(40L)
                                val frac = ((posMs - s.startMs).toFloat() / spanDur).coerceIn(0f, 1f)
                                head = s.startChar + (s.endChar - s.startChar) * frac
                                val charCount = (s.endChar - s.startChar).coerceAtLeast(1)
                                val secPerChar = (spanDur / 1000f) / charCount
                                val holdStrength = ((secPerChar - 0.09f) / 0.22f).coerceIn(0.28f, 1f)
                                val env = smoothstep(frac / 0.25f) * smoothstep((1f - frac) / 0.22f)
                                targetGlow = holdStrength * (0.35f + 0.65f * env)
                                break
                            }
                        }
                    }
                }

                state.headChar.floatValue = head
                state.glow.floatValue += (targetGlow - state.glow.floatValue) * (dt * 10f).coerceIn(0f, 1f)

                if (risesWords && wordRanges.isNotEmpty()) {
                    var changed = false
                    for (wi in wordRanges.indices) {
                        val range = wordRanges[wi]
                        val wStart = range.first.toFloat()
                        val wEnd = (range.last + 1).toFloat()
                        val wLen = (wEnd - wStart).coerceAtLeast(1f)
                        val progress = ((head - wStart + 0.5f) / (wLen * 0.85f)).coerceIn(0f, 1f)
                        val targetRise = smoothstep(progress)

                        // Swell held words while the sweep is inside them and moving slowly
                        val inWord = head >= wStart && head <= wEnd
                        val targetSwell = if (inWord && targetGlow > 0.42f) {
                            ((targetGlow - 0.35f) / 0.65f).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        val (nr, nrv) = stepCriticallyDamped(state.rise[wi], state.riseVel[wi], targetRise, RiseFollowSec, dt)
                        val (ns, nsv) = stepCriticallyDamped(state.swell[wi], state.swellVel[wi], targetSwell, SwellFollowSec, dt)
                        if (abs(nr - state.rise[wi]) > 0.0005f || abs(ns - state.swell[wi]) > 0.0005f) {
                            changed = true
                        }
                        state.rise[wi] = nr
                        state.riseVel[wi] = nrv
                        state.swell[wi] = ns
                        state.swellVel[wi] = nsv
                    }
                    if (changed) state.tick.intValue++
                }
            }
        }
    }

    // AGSL RuntimeShader (Android 13+) for Style 1 (Bloom) and Style 2 (LegacyWave)
    val agslModifier = if (shaded && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember(risesWords) {
            runCatching {
                RuntimeShader(if (risesWords) BloomAgsl else LegacyWaveAgsl)
            }.getOrNull()
        }
        if (shader != null) {
            Modifier
                .layerBleed(MotionBleed)
                .graphicsLayer {
                    val layout = layoutHolder[0]
                    val head = state.headChar.floatValue
                    val glowVal = state.glow.floatValue
                    if (layout == null || text.isEmpty() || head <= 0.01f) {
                        renderEffect = null
                        return@graphicsLayer
                    }
                    val bloom = BloomStrength * glowVal
                    if (risesWords && bloom <= 0.01f) {
                        renderEffect = null
                        return@graphicsLayer
                    }

                    val (headLine, headX, rtl) = layout.sweepHeadX(head, text.length)
                    val lineStart = layout.getLineStart(headLine)
                    val lineEnd = layout.getLineEnd(headLine, visibleEnd = true)
                    val charWidth = if (lineEnd > lineStart) {
                        ((layout.getLineRight(headLine) - layout.getLineLeft(headLine)) / (lineEnd - lineStart))
                            .coerceIn(10f, 42f)
                    } else {
                        18f
                    }
                    val pad = MotionBleed.toPx()
                    val lineStartX = if (rtl) layout.getLineRight(headLine) else layout.getLineLeft(headLine)

                    shader.setFloatUniform("layerSize", size.width, size.height)
                    shader.setFloatUniform("headX", headX + pad)
                    shader.setFloatUniform("lineTop", layout.getLineTop(headLine) + pad)
                    shader.setFloatUniform("lineBottom", layout.getLineBottom(headLine) + pad)
                    shader.setFloatUniform("lineStart", lineStartX + pad)
                    shader.setFloatUniform("flowDir", if (rtl) -1f else 1f)
                    shader.setFloatUniform("bloom", bloom)
                    shader.setFloatUniform("bloomR", BloomRadiusPx)
                    shader.setFloatUniform("bloomLag", charWidth * BloomLagChars)
                    shader.setFloatUniform("bloomSpan", charWidth * BloomSpanChars)

                    if (!risesWords) {
                        shader.setFloatUniform("spanBehind", charWidth * WaveCharsBehind)
                        shader.setFloatUniform("spanAhead", charWidth * WaveCharsAhead)
                        shader.setFloatUniform("liftPx", LegacyWaveLiftPx)
                        shader.setFloatUniform("zoomAmt", LegacyWaveZoom)
                        shader.setFloatUniform("baseY", layout.getLineBaseline(headLine) + pad)
                    }

                    if (headLine > 0) {
                        val prevEndX = if (rtl) layout.getLineLeft(headLine - 1) else layout.getLineRight(headLine - 1)
                        shader.setFloatUniform("hasPrev", 1f)
                        shader.setFloatUniform("prevTop", layout.getLineTop(headLine - 1) + pad)
                        shader.setFloatUniform("prevBottom", layout.getLineBottom(headLine - 1) + pad)
                        shader.setFloatUniform("prevEnd", prevEndX + pad)
                        if (!risesWords) {
                            shader.setFloatUniform("prevBaseY", layout.getLineBaseline(headLine - 1) + pad)
                        }
                    } else {
                        shader.setFloatUniform("hasPrev", 0f)
                        shader.setFloatUniform("prevTop", 0f)
                        shader.setFloatUniform("prevBottom", 0f)
                        shader.setFloatUniform("prevEnd", 0f)
                        if (!risesWords) {
                            shader.setFloatUniform("prevBaseY", 0f)
                        }
                    }

                    renderEffect = AndroidRenderEffect
                        .createRuntimeShaderEffect(shader, "content")
                        .asComposeRenderEffect()
                }
        } else {
            Modifier
        }
    } else {
        Modifier
    }

    // Per-word rise & held-word swell draw layer for Style 1 & Boring
    val wordMotionLayer = rememberGraphicsLayer()
    val wordMotionModifier = if (risesWords && wordRanges.isNotEmpty()) {
        Modifier.drawWithContent {
            @Suppress("UNUSED_VARIABLE") val tick = state.tick.intValue
            val layout = layoutHolder[0]
            if (layout == null) {
                drawContent()
                return@drawWithContent
            }
            val dropPx = WordWaitDrop.toPx()
            val liftPx = HeldLift.toPx()

            wordMotionLayer.record {
                this@drawWithContent.drawContent()
            }

            // Optional soft glow fallback on Android < 13 for Style 1
            val fallbackGlow = shaded && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

            for (wi in wordRanges.indices) {
                val range = wordRanges[wi]
                val firstIdx = range.first.coerceIn(0, (text.length - 1).coerceAtLeast(0))
                val lastIdx = range.last.coerceIn(firstIdx, (text.length - 1).coerceAtLeast(0))
                val lineIdx = layout.getLineForOffset(firstIdx).coerceAtMost(layout.lineCount - 1)
                val firstBox = layout.getBoundingBox(firstIdx)
                val lastBox = layout.getBoundingBox(lastIdx)
                val left = minOf(firstBox.left, lastBox.left) - 2f
                val right = maxOf(firstBox.right, lastBox.right) + 2f
                val top = layout.getLineTop(lineIdx) - 2f
                val bottom = layout.getLineBottom(lineIdx) + 2f
                val baseline = layout.getLineBaseline(lineIdx)

                val r = state.rise[wi]
                val sw = state.swell[wi]
                val dy = dropPx * (1f - r) - liftPx * sw
                val scale = 1f + HeldZoom * sw
                val pivotX = (left + right) * 0.5f

                if (fallbackGlow && sw > 0.05f) {
                    val w = (right - left).coerceAtLeast(16f)
                    val h = (bottom - top).coerceAtLeast(16f)
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.24f * sw), Color.Transparent),
                            center = Offset(pivotX, (top + bottom) * 0.5f + dy),
                            radius = maxOf(w, h) * 0.75f,
                        ),
                        topLeft = Offset(pivotX - w * 0.75f, top + dy - h * 0.2f),
                        size = Size(w * 1.5f, h * 1.4f),
                    )
                }

                withTransform({
                    translate(0f, dy)
                    if (scale > 1.001f) {
                        scale(scale, scale, Offset(pivotX, baseline))
                    }
                }) {
                    clipRect(left, top, right, bottom) {
                        drawLayer(wordMotionLayer)
                    }
                }
            }
        }
    } else {
        Modifier
    }

    // Smooth feathered karaoke sweep mask layer (shared across all 4 Mochy styles)
    val litLayer = rememberGraphicsLayer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(agslModifier)
            .then(wordMotionModifier),
    ) {
        // Base pass: unsung dimmed text
        BasicText(
            text = text,
            style = textStyle.copy(color = unsungColor),
            onTextLayout = { layoutHolder[0] = it },
            modifier = Modifier.fillMaxWidth(),
        )

        // Lit pass: sung accent text revealed pixel-by-pixel with a soft feathered sweep edge
        BasicText(
            text = text,
            style = textStyle.copy(color = accentColor),
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    val layout = layoutHolder[0] ?: return@drawWithContent
                    val head = state.headChar.floatValue
                    if (head <= 0.001f) return@drawWithContent
                    if (head >= text.length) {
                        drawContent()
                        return@drawWithContent
                    }

                    val (headLine, headX, rtl) = layout.sweepHeadX(head, text.length)

                    litLayer.compositingStrategy = LayerCompositingStrategy.Offscreen
                    litLayer.record {
                        this@drawWithContent.drawContent()
                        // Keep every visual line above headLine fully opaque
                        for (line in 0 until headLine) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(0f, layout.getLineTop(line)),
                                size = Size(size.width, layout.getLineBottom(line) - layout.getLineTop(line)),
                                blendMode = BlendMode.DstIn,
                            )
                        }
                        // Soft feathered horizontal mask across headLine
                        val lTop = layout.getLineTop(headLine)
                        val lBottom = layout.getLineBottom(headLine)
                        val lHeight = (lBottom - lTop).coerceAtLeast(1f)
                        val soft = EdgeSoftPx
                        val maskBrush = if (!rtl) {
                            Brush.horizontalGradient(
                                0f to Color.Black,
                                1f to Color.Transparent,
                                startX = (headX - soft * 0.5f).coerceAtLeast(0f),
                                endX = (headX + soft * 0.5f).coerceAtLeast(1f),
                            )
                        } else {
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                1f to Color.Black,
                                startX = (headX - soft * 0.5f).coerceAtLeast(0f),
                                endX = (headX + soft * 0.5f).coerceAtLeast(1f),
                            )
                        }
                        drawRect(
                            brush = maskBrush,
                            topLeft = Offset(0f, lTop),
                            size = Size(size.width, lHeight),
                            blendMode = BlendMode.DstIn,
                        )
                        // Hide lines below headLine completely
                        if (headLine + 1 < layout.lineCount) {
                            val nextTop = layout.getLineTop(headLine + 1)
                            drawRect(
                                color = Color.Transparent,
                                topLeft = Offset(0f, nextTop),
                                size = Size(size.width, (size.height - nextTop).coerceAtLeast(0f)),
                                blendMode = BlendMode.DstIn,
                            )
                        }
                    }
                    drawLayer(litLayer)
                },
        )
    }
}
