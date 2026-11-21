// MochyShims.kt
// The few small helpers Mochy's lyric renderer leans on that live outside its lyric files.

package com.saurav.pixelmusic.presentation.components.lyrics.mochy

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Animatable as FloatAnimatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.saurav.pixelmusic.lyrics.MochyLyricsStyle

/** Mochy's name for the four animated lyric styles; PixelMusic's own enum carries them. */
typealias LyricsMotionStyle = MochyLyricsStyle

/** Left edge of the lyric text, level with the player's content edge. */
val LyricsHeaderArtX = 24.dp

object AppMotion {
    /** Element blur (lyric line depth blur) on or off. */
    @Volatile
    var elementBlur: Boolean = true
}

/** Eases to [target] only when it changes, so a mounted line costs no animation until then. */
@Composable
fun animateFloatOnChange(target: Float, spec: AnimationSpec<Float>): State<Float> {
    val anim = remember { FloatAnimatable(target) }
    LaunchedEffect(target) {
        if (anim.targetValue != target) anim.animateTo(target, spec)
    }
    return anim.asState()
}

@Composable
fun animateColorOnChange(target: Color, spec: AnimationSpec<Color>): State<Color> {
    val anim = remember { Animatable(target) }
    LaunchedEffect(target) {
        if (anim.targetValue != target) anim.animateTo(target, spec)
    }
    return anim.asState()
}

/** A row repaints on its own layer, so one moving line doesn't redraw its neighbours. */
fun Modifier.damageBoundary(): Modifier = this.graphicsLayer { }

// AGSL helpers. The shader travels as Any so older phones never load RuntimeShader.
@SuppressLint("NewApi")
fun createAgslShader(source: String): Any? =
    if (Build.VERSION.SDK_INT >= 33) runCatching { RuntimeShader(source) }.getOrNull() else null

@SuppressLint("NewApi")
fun agslRenderEffect(shader: Any, inputName: String): RenderEffect =
    RenderEffect.createRuntimeShaderEffect(shader as RuntimeShader, inputName)

@SuppressLint("NewApi")
fun Any.setAgslUniform(name: String, value: Float) {
    (this as RuntimeShader).setFloatUniform(name, value)
}

@SuppressLint("NewApi")
fun Any.setAgslUniform(name: String, v1: Float, v2: Float) {
    (this as RuntimeShader).setFloatUniform(name, v1, v2)
}

@SuppressLint("NewApi")
fun Any.setAgslUniform(name: String, v1: Float, v2: Float, v3: Float) {
    (this as RuntimeShader).setFloatUniform(name, v1, v2, v3)
}

/** The colour of the sung words. Mochy draws them white; PixelMusic hands in its own content colour. */
val LocalLyricsHighlight = compositionLocalOf { Color.White }

// the lyrics page's recenter scroll; the instrumental dots collapse on the same curve
internal const val LyricsRecenterDurationMs = 950
internal val LyricsRecenterEasing = androidx.compose.animation.core.CubicBezierEasing(0.5f, 0.45f, 0f, 1f)
internal val LyricsSmoothRecenterEasing = androidx.compose.animation.core.CubicBezierEasing(0.42f, 0f, 0.16f, 1f)
