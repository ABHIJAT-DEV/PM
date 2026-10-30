package com.saurav.pixelmusic.utils.shapes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Biological Asymmetry Fluid Blob Shapes.
 * Each shape defines a smooth, non-self-intersecting organic contour using cubic Bezier curves.
 */
enum class FluidBlobType {
    EGG_DROPLET,
    HORIZONTAL_BEAN,
    SLANTED_OVAL,
    PEBBLE_CIRCLE,
    BULB_TEARDROP,
    AMOEBA_WAVE
}

class FluidBlobShape(private val type: FluidBlobType) : Shape {
    private var cachedWidth: Float = -1f
    private var cachedHeight: Float = -1f
    private var cachedOutline: Outline? = null

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val currentOutline = cachedOutline
        if (size.width == cachedWidth && size.height == cachedHeight && currentOutline != null) {
            return currentOutline
        }

        val w = size.width
        val h = size.height
        val path = Path().apply {
            when (type) {
                FluidBlobType.EGG_DROPLET -> {
                    moveTo(0.48f * w, 0.04f * h)
                    cubicTo(0.78f * w, 0.04f * h, 0.98f * w, 0.22f * h, 0.94f * w, 0.52f * h)
                    cubicTo(0.92f * w, 0.76f * h, 0.76f * w, 0.98f * h, 0.48f * w, 0.98f * h)
                    cubicTo(0.20f * w, 0.98f * h, 0.04f * w, 0.76f * h, 0.04f * w, 0.48f * h)
                    cubicTo(0.04f * w, 0.20f * h, 0.18f * w, 0.04f * h, 0.48f * w, 0.04f * h)
                    close()
                }
                FluidBlobType.HORIZONTAL_BEAN -> {
                    moveTo(0.25f * w, 0.08f * h)
                    cubicTo(0.55f * w, 0.02f * h, 0.85f * w, 0.06f * h, 0.96f * w, 0.35f * h)
                    cubicTo(1.02f * w, 0.65f * h, 0.82f * w, 0.95f * h, 0.55f * w, 0.92f * h)
                    cubicTo(0.35f * w, 0.88f * h, 0.15f * w, 0.96f * h, 0.04f * w, 0.70f * h)
                    cubicTo(-0.02f * w, 0.45f * h, 0.05f * w, 0.16f * h, 0.25f * w, 0.08f * h)
                    close()
                }
                FluidBlobType.SLANTED_OVAL -> {
                    moveTo(0.35f * w, 0.05f * h)
                    cubicTo(0.70f * w, 0.02f * h, 0.96f * w, 0.18f * h, 0.92f * w, 0.50f * h)
                    cubicTo(0.90f * w, 0.78f * h, 0.70f * w, 0.98f * h, 0.42f * w, 0.95f * h)
                    cubicTo(0.18f * w, 0.92f * h, 0.04f * w, 0.72f * h, 0.05f * w, 0.42f * h)
                    cubicTo(0.06f * w, 0.15f * h, 0.18f * w, 0.07f * h, 0.35f * w, 0.05f * h)
                    close()
                }
                FluidBlobType.PEBBLE_CIRCLE -> {
                    moveTo(0.50f * w, 0.02f * h)
                    cubicTo(0.80f * w, 0.02f * h, 0.98f * w, 0.20f * h, 0.98f * w, 0.50f * h)
                    cubicTo(0.98f * w, 0.80f * h, 0.80f * w, 0.98f * h, 0.50f * w, 0.98f * h)
                    cubicTo(0.20f * w, 0.98f * h, 0.02f * w, 0.80f * h, 0.02f * w, 0.50f * h)
                    cubicTo(0.02f * w, 0.20f * h, 0.20f * w, 0.02f * h, 0.50f * w, 0.02f * h)
                    close()
                }
                FluidBlobType.BULB_TEARDROP -> {
                    moveTo(0.32f * w, 0.06f * h)
                    cubicTo(0.62f * w, 0.02f * h, 0.90f * w, 0.18f * h, 0.95f * w, 0.48f * h)
                    cubicTo(0.98f * w, 0.78f * h, 0.80f * w, 0.98f * h, 0.50f * w, 0.98f * h)
                    cubicTo(0.20f * w, 0.98f * h, 0.02f * w, 0.78f * h, 0.04f * w, 0.46f * h)
                    cubicTo(0.05f * w, 0.20f * h, 0.15f * w, 0.10f * h, 0.32f * w, 0.06f * h)
                    close()
                }
                FluidBlobType.AMOEBA_WAVE -> {
                    moveTo(0.40f * w, 0.04f * h)
                    cubicTo(0.72f * w, 0.02f * h, 0.98f * w, 0.20f * h, 0.96f * w, 0.52f * h)
                    cubicTo(0.94f * w, 0.80f * h, 0.78f * w, 0.98f * h, 0.46f * w, 0.96f * h)
                    cubicTo(0.22f * w, 0.94f * h, 0.02f * w, 0.80f * h, 0.04f * w, 0.48f * h)
                    cubicTo(0.05f * w, 0.22f * h, 0.18f * w, 0.06f * h, 0.40f * w, 0.04f * h)
                    close()
                }
            }
        }

        val outline = Outline.Generic(path)
        cachedWidth = size.width
        cachedHeight = size.height
        cachedOutline = outline
        return outline
    }
}
