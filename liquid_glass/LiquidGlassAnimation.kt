package com.saurav.pixelmusic.presentation.components.liquidglass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * LIQUID GLASS ANIMATION & INTERACTION CODE
 * ============================================================================
 *
 * ARCHITECTURE OVERVIEW:
 * 1. DUAL INDEPENDENT SPRING PHYSICS:
 *    - `press` Animatable: Tracks touch hold duration.
 *      * Spring: dampingRatio = 0.5f, stiffness = 450f (firm compression)
 *    - `jiggle` Animatable: Triggers instant transient elastic bounce upon contact.
 *      * Spring: dampingRatio = 0.32f, stiffness = 280f (rapid underdamped wobble)
 *
 * 2. NON-UNIFORM SQUASH & STRETCH TRANSFORMS:
 *    Simulates physical liquid displacement (volume conservation):
 *    - Horizontal Pills / Capsules (e.g., PixelMusic header pill):
 *        scaleX = 1f + (press * 0.08f) + (jiggle * 0.12f)
 *        scaleY = 1f - (press * 0.06f) - (jiggle * 0.10f)
 *    - Circular Buttons (e.g., settings icon button):
 *        scaleX = 1f + (press * 0.10f) + (jiggle * 0.15f)
 *        scaleY = 1f - (press * 0.08f) - (jiggle * 0.13f)
 *
 * 3. GESTURE SEQUENCE (awaitEachGesture):
 *    - `awaitFirstDown(requireUnconsumed = false)`: Captures contact immediately without
 *      interfering with scrolling lists underneath.
 *    - Jiggle snaps to 1f and animates to 0f with jiggleSpec.
 *    - Press animates to 1f with pressSpec.
 *    - `waitForUpOrCancellation()`: Waits for user release or scroll cancel.
 *    - Press animates back to 0f with pressSpec.
 *    - If released (`up != null`), triggers haptic feedback (`TextHandleMove`) and invokes onClick().
 *
 * 4. BACKDROP COMPATIBILITY:
 *    - The scale transform MUST be passed into `drawBackdrop`'s `layerBlock` so that only the glass
 *      layer deforms, preventing the sampled background content from sliding around.
 *
 * ----------------------------------------------------------------------------
 * QUICK COPY-PASTE USAGE:
 *
 *   val animState = rememberLiquidGlassAnimationState()
 *
 *   Box(
 *       modifier = Modifier
 *           .liquidGlassTouch(
 *               state = animState,
 *               onClick = { /* Handle Click */ }
 *           )
 *           .liquidGlassSurface(
 *               backdrop = backdrop,
 *               shape = CircleShape,
 *               layerBlock = {
 *                   animState.applyPillTransform(this) // or applyCircleTransform(this)
 *               }
 *           )
 *   ) {
 *       // Child content
 *   }
 * ============================================================================
 */

/**
 * Spring physics parameters for Liquid Glass interaction.
 */
object LiquidGlassSpringDefaults {
    /**
     * Firm press spring: fast down-stroke with moderate damping (bouncy yet controlled).
     */
    val PressSpec: SpringSpec<Float> = spring(
        dampingRatio = 0.5f,
        stiffness = 450f
    )

    /**
     * High-frequency jiggle spring: rapid transient oscillation upon initial contact.
     */
    val JiggleSpec: SpringSpec<Float> = spring(
        dampingRatio = 0.32f,
        stiffness = 280f
    )
}

/**
 * Encapsulates the dual-spring animation state for Liquid Glass elements.
 */
@Stable
class LiquidGlassAnimationState(
    val press: Animatable<Float, AnimationVector1D>,
    val jiggle: Animatable<Float, AnimationVector1D>
) {
    /**
     * Snaps jiggle to full deflection and lets it oscillate back to 0.
     */
    fun triggerJiggle(scope: CoroutineScope) {
        scope.launch {
            jiggle.snapTo(1f)
            jiggle.animateTo(0f, LiquidGlassSpringDefaults.JiggleSpec)
        }
    }

    /**
     * Drives the press state down into compressed position.
     */
    fun pressDown(scope: CoroutineScope) {
        scope.launch {
            press.animateTo(1f, LiquidGlassSpringDefaults.PressSpec)
        }
    }

    /**
     * Releases the press state back to resting position.
     */
    fun release(scope: CoroutineScope) {
        scope.launch {
            press.animateTo(0f, LiquidGlassSpringDefaults.PressSpec)
        }
    }

    /**
     * Applies non-uniform squash-and-stretch for elongated pill/capsule shapes
     * (used by the PixelMusic logo pill).
     *
     * Expands width by up to +20% and compresses height by up to -16% during touch.
     */
    fun applyPillTransform(scope: GraphicsLayerScope) {
        val p = press.value
        val j = jiggle.value
        scope.scaleX = 1f + (p * 0.08f) + (j * 0.12f)
        scope.scaleY = 1f - (p * 0.06f) - (j * 0.10f)
    }

    /**
     * Applies non-uniform squash-and-stretch for circular controls
     * (used by the top-right settings gear button).
     *
     * Higher sensitivity (+25% width, -21% height) for prominent bounce on small circular hit targets.
     */
    fun applyCircleTransform(scope: GraphicsLayerScope) {
        val p = press.value
        val j = jiggle.value
        scope.scaleX = 1f + (p * 0.10f) + (j * 0.15f)
        scope.scaleY = 1f - (p * 0.08f) - (j * 0.13f)
    }
}

/**
 * Creates and remembers a [LiquidGlassAnimationState] instance across recompositions.
 */
@Composable
fun rememberLiquidGlassAnimationState(): LiquidGlassAnimationState {
    val press = remember { Animatable(0f) }
    val jiggle = remember { Animatable(0f) }
    return remember(press, jiggle) {
        LiquidGlassAnimationState(press, jiggle)
    }
}

/**
 * Interactive touch gesture modifier for Liquid Glass components.
 *
 * Implements the full gesture loop:
 * - Immediate touch-down detection (requireUnconsumed = false).
 * - Simultaneous trigger of jiggle oscillation and press spring.
 * - Release spring on touch-up or touch cancellation.
 * - Haptic tick (TextHandleMove) upon confirmed tap.
 *
 * @param state The animation state managing the animatables.
 * @param enabled Whether interaction is active.
 * @param coroutineScope Coroutine scope to launch animation jobs.
 * @param onClick Action invoked when tap is released within target bounds.
 */
@Composable
fun Modifier.liquidGlassTouch(
    state: LiquidGlassAnimationState,
    enabled: Boolean = true,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onClick: () -> Unit
): Modifier {
    val haptic = LocalHapticFeedback.current

    if (!enabled) return this

    return this.pointerInput(enabled) {
        awaitEachGesture {
            // 1. Detect initial touch down immediately without consuming scroll gestures
            awaitFirstDown(requireUnconsumed = false)

            // 2. Fire high-frequency transient jiggle + smooth press compression concurrently
            state.triggerJiggle(coroutineScope)
            state.pressDown(coroutineScope)

            // 3. Await finger lift or gesture cancellation
            val up = waitForUpOrCancellation()

            // 4. Spring back to resting state
            state.release(coroutineScope)

            // 5. If released cleanly, trigger haptic feedback and click callback
            if (up != null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        }
    }
}
