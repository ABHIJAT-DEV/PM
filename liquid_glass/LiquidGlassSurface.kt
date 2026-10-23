package com.saurav.pixelmusic.presentation.components.liquidglass

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.saurav.pixelmusic.ui.theme.LocalLiquidGlassBlurIntensity
import com.saurav.pixelmusic.ui.theme.LocalLiquidGlassRefractionIntensity

/**
 * ============================================================================
 * LIQUID GLASS LOGIC CODE
 * ============================================================================
 *
 * DEPENDENCY:
 *   implementation("io.github.kyant0:backdrop:2.0.1")
 *
 * ============================================================================
 * DESIGN PRINCIPLE: SHADOW POLICY
 *   { Material You Expressive (default style) x Shadow }
 *   { Liquid Glass effect x NO Shadow }
 *
 * RULE: Whenever applying Liquid Glass effect to components:
 * 1. REMOVE SHADOW when in liquid glass mode:
 *    - elevation = 0.dp
 *    - shadowElevation = 0.dp
 *    - tonalElevation = 0.dp
 *    - If using Modifier.shadow(), set elevation to 0.dp.
 *    Liquid Glass surfaces rely on optical refraction, translucency, specular rim
 *    gradients, and top highlight sheens. External drop shadows create muddy halos
 *    and ruin realistic glass refraction.
 * 2. PRESERVE SHADOW when liquid glass is disabled:
 *    - Keep default Material You Expressive shadows / elevations (e.g. 4.dp - 8.dp)
 *      so non-glass mode retains its tactile elevation hierarchy.
 * ============================================================================
 *
 * ARCHITECTURE OVERVIEW:
 * 1. LIVE BACKDROP SHADER PIPELINE (via Kyant0 Backdrop):
 *    - vibrancy(): Boosts color saturation ~1.5x beneath the glass for vibrant iOS look.
 *    - blur(radius): Gaussian blur scaled by user preference (LocalLiquidGlassBlurIntensity).
 *    - lens(refractionHeight, refractionAmount): Real-time optical refraction shader.
 *      * NOTE: Requires Android 13+ (API level 33, Tiramisu).
 * 2. SPECULAR RIM GRADIENT:
 *    - Vertical gradient border (0.75dp) with subtle white highlights (top-bright, bottom-subtle).
 * 3. SURFACE TINT & LEGIBILITY:
 *    - onDrawSurface renders an adaptive translucent tint over refraction:
 *      * Dark theme: Color(0x351E1E22) (~21% opacity dark charcoal)
 *      * Light theme: Color(0x40FFFFFF) (~25% opacity pure white)
 * 4. SPECULAR SHEEN OVERLAY:
 *    - Soft vertical white highlight gradient fading downwards to transparent.
 * 5. ZERO SHADOW ELEVATION:
 *    - Liquid glass surfaces must never cast drop shadows. Always set elevation to 0.dp.
 * 6. NO GREY RECTANGULAR HALOS:
 *    - Never use unclipped drawRect() inside drawWithContent on rounded or pill-shaped
 *      components. On dark/AMOLED backgrounds, drawRect paints into the 4 corner areas
 *      outside the pill shape, creating sharp grey box artifacts around buttons.
 *      Always use .border(stroke, shape) and .clip(shape) directly.
 * 7. NO FLOATING TOUCH-TARGET HALOS (MINIMUM INTERACTIVE COMPONENT SIZE):
 *    - When applying Liquid Glass to components whose visual height is less than 48.dp
 *      (such as FilterChips (32dp), custom buttons (42dp), or range selectors (44dp)),
 *      Material 3's minimumInteractiveComponentSize expands the outer modifier bounds
 *      to 48dp while centering the smaller visual component inside. This causes the glass
 *      border and animation to be drawn as a larger transparent bubble around the smaller
 *      inner component.
 *      Always wrap these components with:
 *      CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) { ... }
 *      so the glass surface bounds align 1:1 with the component bounds.
 * 8. SEPARATION OF TRANSFORMS (layerBlock):
 *    - Scale / squashing transforms MUST be applied inside layerBlock rather than on the outer Composable,
 *      so that the sampled backdrop underneath remains anchored and does not slide.
 * 8. GRACEFUL FALLBACKS:
 *    - When backdrop is null or disabled, gracefully falls back to frosted surface or surfaceContainerHighest.
 *
 * ----------------------------------------------------------------------------
 * QUICK COPY-PASTE USAGE:
 *
 *   Box(
 *       modifier = Modifier
 *           .height(48.dp)
 *           .liquidGlassSurface(
 *               backdrop = backdrop,
 *               shape = CircleShape,
 *               layerBlock = {
 *                   scaleX = currentScaleX
 *                   scaleY = currentScaleY
 *               }
 *           )
 *   ) {
 *       LiquidGlassSheenOverlay()
 *       // Composable content here (Text, Icon, etc.)
 *   }
 * ============================================================================
 */

/**
 * Produces the iOS-style specular rim gradient brush.
 *
 * Highlights the upper edge with slightly higher opacity and softly fades toward the bottom.
 */
@Composable
fun rememberGlassBorderBrush(
    isDark: Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f
): Brush {
    return remember(isDark) {
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
}

/**
 * Fallback translucent background color used when live backdrop capture is unavailable.
 */
@Composable
fun rememberGlassFallbackColor(
    isDark: Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f
): Color {
    return remember(isDark) {
        if (isDark) Color(0x421E1E22) else Color(0x66FFFFFF)
    }
}

/**
 * Top specular highlight sheen overlay.
 * Placed inside the glass Box to simulate ambient light reflection from above.
 */
@Composable
fun LiquidGlassSheenOverlay(
    modifier: Modifier = Modifier,
    isDark: Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.04f else 0.08f),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Comprehensive Liquid Glass surface modifier extracted directly from the header pill.
 *
 * Enforces the zero-shadow rule: eliminates muddy drop shadows in liquid glass mode.
 *
 * @param backdrop The [Backdrop] instance captured from the parent screen (e.g. rememberLayerBackdrop).
 * @param shape The shape of the glass element (default: [CircleShape] for pills and circular buttons).
 * @param isLiquidGlass Toggle flag (e.g. from user settings) to enable or disable glass rendering.
 * @param borderWidth Thickness of the specular rim border (default: 0.75.dp).
 * @param borderBrush Specular rim gradient brush (default: [rememberGlassBorderBrush]).
 * @param layerBlock Lambda applied to the Backdrop's internal layer (keeps the background sample stationary while flexing).
 */
@Composable
fun Modifier.liquidGlassSurface(
    backdrop: Backdrop?,
    shape: Shape = CircleShape,
    isLiquidGlass: Boolean = true,
    borderWidth: Dp = 0.75.dp,
    borderBrush: Brush = rememberGlassBorderBrush(),
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null
): Modifier {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val blurIntensity = LocalLiquidGlassBlurIntensity.current
    val refractionIntensity = LocalLiquidGlassRefractionIntensity.current
    val fallbackColor = rememberGlassFallbackColor(isDark)

    return when {
        // 1. Full Live Backdrop Shader Mode (Active glass + provided backdrop)
        isLiquidGlass && backdrop != null -> {
            this
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        // Color boost underneath glass
                        vibrancy()

                        // Gaussian blur with user-configured intensity
                        val blurRadiusPx = (10.dp.toPx() * blurIntensity).coerceAtLeast(1f)
                        blur(blurRadiusPx)

                        // Android 13+ (API 33) optical refraction lens shader
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && refractionIntensity > 0.01f) {
                            lens(
                                refractionHeight = (12.dp.toPx() * refractionIntensity).coerceAtMost(16.dp.toPx()),
                                refractionAmount = (28.dp.toPx() * refractionIntensity).coerceAtMost(32.dp.toPx())
                            )
                        }
                    },
                    layerBlock = {
                        shadowElevation = 0f
                        layerBlock?.invoke(this)
                    },
                    onDrawSurface = {
                        // Adaptive translucent tint over refraction for legibility
                        drawRect(if (isDark) Color(0x351E1E22) else Color(0x40FFFFFF))
                    }
                )
                .border(BorderStroke(borderWidth, borderBrush), shape)
        }

        // 2. Translucent Glass Fallback (Glass active, but no backdrop in scope)
        isLiquidGlass -> {
            this
                .graphicsLayer {
                    shadowElevation = 0f
                    layerBlock?.invoke(this)
                }
                .background(fallbackColor, shape)
                .border(BorderStroke(borderWidth, borderBrush), shape)
                .clip(shape)
        }

        // 3. Glass Disabled Fallback (Standard Material 3 container)
        else -> {
            this
                .graphicsLayer {
                    layerBlock?.invoke(this)
                }
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                    shape
                )
                .clip(shape)
        }
    }
}
