package com.saurav.pixelmusic.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max

val LocalThemeTransitionController = staticCompositionLocalOf<ThemeTransitionController?> { null }

enum class ThemeTransitionType {
    CIRCLE_SPREAD,
    CIRCLE_SHRINK,
    WIPE_UP,
    WIPE_DOWN,
    WIPE_LEFT,
    WIPE_RIGHT,
    CORNER_TOP_LEFT,
    CORNER_TOP_RIGHT,
    CORNER_BOTTOM_LEFT,
    CORNER_BOTTOM_RIGHT
}

class CircularRevealShape(
    private val radius: Float,
    private val center: Offset
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (radius <= 0f) {
            return Outline.Generic(Path())
        }
        val path = Path().apply {
            addOval(
                Rect(
                    left = center.x - radius,
                    top = center.y - radius,
                    right = center.x + radius,
                    bottom = center.y + radius
                )
            )
        }
        return Outline.Generic(path)
    }
}

class DirectionalWipeShape(
    private val type: ThemeTransitionType,
    private val progress: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val p = progress.coerceIn(0f, 1f)
        if (p <= 0f) {
            return Outline.Generic(Path())
        }
        if (p >= 1f) {
            return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }
        val path = Path().apply {
            when (type) {
                ThemeTransitionType.WIPE_DOWN -> {
                    addRect(Rect(0f, 0f, size.width, size.height * p))
                }
                ThemeTransitionType.WIPE_UP -> {
                    addRect(Rect(0f, size.height * (1f - p), size.width, size.height))
                }
                ThemeTransitionType.WIPE_RIGHT -> {
                    addRect(Rect(0f, 0f, size.width * p, size.height))
                }
                ThemeTransitionType.WIPE_LEFT -> {
                    addRect(Rect(size.width * (1f - p), 0f, size.width, size.height))
                }
                else -> {
                    addRect(Rect(0f, 0f, size.width, size.height))
                }
            }
        }
        return Outline.Generic(path)
    }
}

class ThemeTransitionController {
    var previousBitmap by mutableStateOf<ImageBitmap?>(null)
        private set
    var isTransitioning by mutableStateOf(false)
        private set
    var transitionProgress by mutableFloatStateOf(0f)
        private set
    var currentTransitionType by mutableStateOf(ThemeTransitionType.CIRCLE_SPREAD)
        private set
    var revealRadius by mutableFloatStateOf(0f)
        private set
    var revealCenter by mutableStateOf(Offset.Zero)
        private set
    var maxRadius by mutableFloatStateOf(0f)
        private set
    var lastTouchOffset by mutableStateOf<Offset?>(null)

    private var lastChosenType: ThemeTransitionType? = null
    private var animJob: Job? = null

    private fun findWindow(view: View): Window? {
        var ctx: Context? = view.context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx.window
            ctx = ctx.baseContext
        }
        return null
    }

    private fun pickRandomTransition(): ThemeTransitionType {
        val allTypes = ThemeTransitionType.values()
        val candidates = allTypes.filter { it != lastChosenType }
        val chosen = candidates.randomOrNull() ?: allTypes.random()
        lastChosenType = chosen
        return chosen
    }

    private fun startReveal(
        resolvedOrigin: Offset,
        viewWidth: Float,
        viewHeight: Float,
        scope: CoroutineScope,
        change: () -> Unit
    ) {
        val chosenType = pickRandomTransition()
        currentTransitionType = chosenType

        val center = when (chosenType) {
            ThemeTransitionType.CORNER_TOP_LEFT -> Offset(0f, 0f)
            ThemeTransitionType.CORNER_TOP_RIGHT -> Offset(viewWidth, 0f)
            ThemeTransitionType.CORNER_BOTTOM_LEFT -> Offset(0f, viewHeight)
            ThemeTransitionType.CORNER_BOTTOM_RIGHT -> Offset(viewWidth, viewHeight)
            else -> resolvedOrigin
        }

        val maxDistX = max(center.x, viewWidth - center.x)
        val maxDistY = max(center.y, viewHeight - center.y)
        val calculatedMaxRadius = hypot(maxDistX.toDouble(), maxDistY.toDouble()).toFloat() + 32f

        revealCenter = center
        maxRadius = calculatedMaxRadius
        revealRadius = 0f
        transitionProgress = 0f
        isTransitioning = true

        change()

        animJob = scope.launch {
            val animatable = Animatable(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 440,
                    easing = FastOutSlowInEasing
                )
            ) {
                transitionProgress = value
                revealRadius = value * calculatedMaxRadius
            }
            isTransitioning = false
            previousBitmap = null
        }
    }

    fun transitionThemeChange(
        view: View,
        origin: Offset? = null,
        scope: CoroutineScope,
        change: () -> Unit
    ) {
        val root = view.rootView ?: view
        val w = root.width
        val h = root.height
        if (w <= 0 || h <= 0) {
            change()
            return
        }

        animJob?.cancel()

        val resolvedOrigin = origin
            ?: lastTouchOffset
            ?: Offset(w * 0.5f, h * 0.5f)

        val window = findWindow(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && window != null) {
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            try {
                PixelCopy.request(
                    window,
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            previousBitmap = bitmap.asImageBitmap()
                            startReveal(resolvedOrigin, w.toFloat(), h.toFloat(), scope, change)
                        } else {
                            Log.w("CircularReveal", "PixelCopy failed with result: $copyResult")
                            previousBitmap = null
                            change()
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
                return
            } catch (e: Throwable) {
                Log.e("CircularReveal", "PixelCopy invocation threw error", e)
            }
        }

        try {
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            root.draw(canvas)
            previousBitmap = bitmap.asImageBitmap()
            startReveal(resolvedOrigin, w.toFloat(), h.toFloat(), scope, change)
        } catch (e: Throwable) {
            Log.e("CircularReveal", "Software snapshot capture failed", e)
            previousBitmap = null
            change()
        }
    }
}

@Composable
fun rememberThemeTransitionController(): ThemeTransitionController {
    return remember { ThemeTransitionController() }
}

@Composable
fun CircularThemeRevealHost(
    controller: ThemeTransitionController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.firstOrNull()?.position?.let { pos ->
                            controller.lastTouchOffset = pos
                        }
                    }
                }
            }
    ) {
        if (!controller.isTransitioning || controller.previousBitmap == null) {
            content()
        } else {
            val type = controller.currentTransitionType
            val progress = controller.transitionProgress
            val center = controller.revealCenter
            val maxR = controller.maxRadius

            if (type == ThemeTransitionType.CIRCLE_SHRINK) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
                val currentRadius = (1f - progress) * maxR
                if (currentRadius > 0f) {
                    Image(
                        bitmap = controller.previousBitmap!!,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                shape = CircularRevealShape(radius = currentRadius, center = center)
                                clip = true
                            },
                        contentScale = ContentScale.FillBounds
                    )
                }
            } else {
                Image(
                    bitmap = controller.previousBitmap!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                val revealShape = when (type) {
                    ThemeTransitionType.WIPE_DOWN,
                    ThemeTransitionType.WIPE_UP,
                    ThemeTransitionType.WIPE_RIGHT,
                    ThemeTransitionType.WIPE_LEFT -> DirectionalWipeShape(type = type, progress = progress)
                    else -> CircularRevealShape(radius = progress * maxR, center = center)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            shape = revealShape
                            clip = true
                        }
                ) {
                    content()
                }
            }
        }
    }
}
