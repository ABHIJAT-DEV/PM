package com.saurav.pixelmusic.utils.shapes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Biological Asymmetry & Material You Expressive Collage Shapes.
 * Provides:
 * - Rounded Triangle Blobs for 3 corners (Top-Left, Top-Right, Bottom-Left)
 * - 7-lobed Scalloped Cookie for the 4th corner (Bottom-Right)
 * - Material 3 Expressive 4-Lobe Clover Pill & 10-Sided Sunny badge for the middle
 */
enum class FluidBlobType {
    CORNER_TRIANGLE_TL,
    CORNER_TRIANGLE_TR,
    CORNER_TRIANGLE_BL,
    CORNER_COOKIE_BR,
    CENTER_EXPRESSIVE_CLOVER_PILL,
    MIDDLE_EXPRESSIVE_SUNNY
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

        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val rx = size.width * 0.485f
        val ry = size.height * 0.485f

        val path = Path().apply {
            when (type) {
                FluidBlobType.CORNER_TRIANGLE_TL -> {
                    buildRoundedTriangle(
                        cx = cx,
                        cy = cy,
                        rx = rx,
                        ry = ry,
                        rotDeg = 10f,
                        cornerRatio = 0.44f
                    )
                }
                FluidBlobType.CORNER_TRIANGLE_TR -> {
                    buildRoundedTriangle(
                        cx = cx,
                        cy = cy,
                        rx = rx,
                        ry = ry,
                        rotDeg = 46f,
                        cornerRatio = 0.44f
                    )
                }
                FluidBlobType.CORNER_TRIANGLE_BL -> {
                    buildRoundedTriangle(
                        cx = cx,
                        cy = cy,
                        rx = rx,
                        ry = ry,
                        rotDeg = 5f,
                        cornerRatio = 0.44f
                    )
                }
                FluidBlobType.CORNER_COOKIE_BR -> {
                    buildScallopedPolygon(
                        cx = cx,
                        cy = cy,
                        rx = rx,
                        ry = ry,
                        sides = 7,
                        curve = 0.135f,
                        rotDeg = 18f
                    )
                }
                FluidBlobType.CENTER_EXPRESSIVE_CLOVER_PILL -> {
                    buildExpressiveCloverPill(
                        cx = cx,
                        cy = cy,
                        rx = rx,
                        ry = ry * 0.76f,
                        rotDeg = -28f
                    )
                }
                FluidBlobType.MIDDLE_EXPRESSIVE_SUNNY -> {
                    buildScallopedPolygon(
                        cx = cx,
                        cy = cy,
                        rx = rx,
                        ry = ry,
                        sides = 10,
                        curve = 0.055f,
                        rotDeg = 15f
                    )
                }
            }
        }

        val outline = Outline.Generic(path)
        cachedWidth = size.width
        cachedHeight = size.height
        cachedOutline = outline
        return outline
    }

    private fun Path.buildRoundedTriangle(
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        rotDeg: Float,
        cornerRatio: Float
    ) {
        val rot = (rotDeg * PI / 180.0)
        val cosRot = cos(rot).toFloat()
        val sinRot = sin(rot).toFloat()
        val d = 1f - cornerRatio
        val vertexAngles = doubleArrayOf(0.0, 2.0 * PI / 3.0, 4.0 * PI / 3.0)
        val stepsPerCorner = 36

        var isFirst = true
        for (aMid in vertexAngles) {
            val vxBase = (d * cos(aMid)).toFloat()
            val vyBase = (d * sin(aMid)).toFloat()
            val startAngle = aMid - PI / 3.0
            val sweep = 2.0 * PI / 3.0

            for (i in 0..stepsPerCorner) {
                val t = startAngle + sweep * (i.toDouble() / stepsPerCorner)
                val x0 = (vxBase + cornerRatio * cos(t).toFloat()) * rx
                val y0 = (vyBase + cornerRatio * sin(t).toFloat()) * ry
                val x = cx + x0 * cosRot - y0 * sinRot
                val y = cy + x0 * sinRot + y0 * cosRot
                if (isFirst) {
                    moveTo(x, y)
                    isFirst = false
                } else {
                    lineTo(x, y)
                }
            }
        }
        close()
    }

    private fun Path.buildExpressiveCloverPill(
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        rotDeg: Float
    ) {
        val rot = (rotDeg * PI / 180.0)
        val cosRot = cos(rot).toFloat()
        val sinRot = sin(rot).toFloat()
        val steps = 120
        val maxFactor = 1.11f

        for (i in 0 until steps) {
            val t = 2.0 * PI * i / steps
            val factor = ((1.0 + 0.11 * cos(4.0 * t - PI)) / maxFactor).toFloat()
            val x0 = rx * factor * cos(t).toFloat()
            val y0 = ry * factor * sin(t).toFloat()
            val x = cx + x0 * cosRot - y0 * sinRot
            val y = cy + x0 * sinRot + y0 * cosRot
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    private fun Path.buildScallopedPolygon(
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        sides: Int,
        curve: Float,
        rotDeg: Float
    ) {
        val rot = (rotDeg * PI / 180.0)
        val cosRot = cos(rot).toFloat()
        val sinRot = sin(rot).toFloat()
        val steps = 144
        val maxFactor = 1f + curve

        for (i in 0 until steps) {
            val t = 2.0 * PI * i / steps
            val factor = ((1.0 + curve * cos(sides * t)) / maxFactor).toFloat()
            val x0 = rx * factor * cos(t).toFloat()
            val y0 = ry * factor * sin(t).toFloat()
            val x = cx + x0 * cosRot - y0 * sinRot
            val y = cy + x0 * sinRot + y0 * cosRot
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}
