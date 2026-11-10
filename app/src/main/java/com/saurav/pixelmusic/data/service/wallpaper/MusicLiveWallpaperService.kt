package com.saurav.pixelmusic.data.service.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.text.TextPaint
import android.text.TextUtils
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.compose.ui.util.lerp
import androidx.core.content.res.ResourcesCompat
import com.saurav.pixelmusic.R
import com.saurav.pixelmusic.data.preferences.MusicLiveWallpaperSettings
import com.saurav.pixelmusic.data.preferences.MusicWallpaperShapePosition
import com.saurav.pixelmusic.data.preferences.MusicWallpaperStyleMode
import com.saurav.pixelmusic.data.preferences.UserPreferencesRepository
import com.saurav.pixelmusic.presentation.viewmodel.PlaybackStateHolder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MusicLiveWallpaperServiceEntryPoint {
    fun userPreferencesRepository(): UserPreferencesRepository
    fun playbackStateHolder(): PlaybackStateHolder
}

/**
 * Android Live Wallpaper service (`WallpaperService`) for PixelMusic.
 *
 * Features:
 * - Uses already-cached album art from Now Playing / Notification without re-downloading.
 * - Falls back smoothly to the user-selected gallery wallpaper when no music is playing.
 * - Performs slow, silky crossfade transitions (`800ms..4000ms`) between tracks and states,
 *   using offscreen snapshot compositing so mid-transition track skips never jump or flash.
 * - Supports Full Screen mode and Material You Expressive shapes (Cookies, Rounded Triangle,
 *   Rounded Square, Rounded Rectangle, Pill, Diamond, Oval, Pentagon) at Top, Middle, or Bottom
 *   with adjustable background blur and single-tap shape morphing.
 * - Optional top-left song title and artist overlay with smooth crossfading.
 */
class MusicLiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return MusicWallpaperEngine()
    }

    private inner class MusicWallpaperEngine : Engine(), Choreographer.FrameCallback {

        private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private val choreographer = Choreographer.getInstance()

        private lateinit var userPreferencesRepository: UserPreferencesRepository
        private lateinit var playbackStateHolder: PlaybackStateHolder

        private var isEngineVisible = false
        private var isFrameCallbackPosted = false
        private var surfaceWidth = 0
        private var surfaceHeight = 0

        // Current settings & playback state
        private var settings = MusicLiveWallpaperSettings()
        private var currentPlayback = WallpaperPlaybackSnapshot()

        // Loaded bitmaps
        private var activeArtKey: String = ""
        private var activeSharpBitmap: Bitmap? = null
        private var activeBlurredBitmap: Bitmap? = null
        private var fallbackBitmap: Bitmap? = null
        private var imageLoadJob: Job? = null

        // Smooth scene crossfade state (captures exact outgoing rendered frame so rapid skips never jump)
        private var outgoingFrameSnapshot: Bitmap? = null
        private var currentFrameBuffer: Bitmap? = null
        private var crossfadeStartMs = 0L
        private var crossfadeDurationMs = 1800L
        private var crossfadeProgress = 1f

        // Smooth Material You shape morph + rotation state
        private var fromShapeKind = WallpaperExpressiveShapeKind.COOKIE_8
        private var toShapeKind = WallpaperExpressiveShapeKind.COOKIE_8
        private var shapeMorphStartMs = 0L
        private var shapeMorphDurationMs = 820L
        private var shapeMorphProgress = 1f

        private var spinStartDeg = 0f
        private var spinTargetDeg = 0f
        private var currentSpinDeg = 0f
        private var continuousSpinDeg = 0f
        private var lastFrameRealtimeMs = 0L

        // Smooth vertical position & mode morph state
        private var posStartY = MusicWallpaperShapePosition.MIDDLE.verticalCenterFraction()
        private var posTargetY = MusicWallpaperShapePosition.MIDDLE.verticalCenterFraction()
        private var currentPosY = MusicWallpaperShapePosition.MIDDLE.verticalCenterFraction()
        private var posAnimStartMs = 0L
        private var posAnimProgress = 1f

        private var modeStartT = 1f
        private var modeTargetT = 1f
        private var currentModeT = 1f
        private var modeAnimStartMs = 0L
        private var modeAnimProgress = 1f

        // Smooth top-left song info alpha & text transition
        private var displayedTitle: String = ""
        private var displayedArtist: String = ""
        private var previousTitle: String = ""
        private var previousArtist: String = ""
        private var textCrossfadeStartMs = 0L
        private var textCrossfadeProgress = 1f
        private var songInfoVisibilityAlpha = 0f

        // Tap detection for changing Material You shape on home/lock screen
        private var touchDownX = 0f
        private var touchDownY = 0f
        private var touchDownTimeMs = 0L

        // Pre-allocated drawing objects (zero per-frame allocations)
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(46, 255, 255, 255)
        }
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(38, 255, 255, 255)
        }
        private val topScrimPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val titleTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            setShadowLayer(12f, 0f, 3f, Color.argb(140, 0, 0, 0))
        }
        private val artistTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(215, 255, 255, 255)
            setShadowLayer(10f, 0f, 2f, Color.argb(130, 0, 0, 0))
        }

        private val shapePath = Path()
        private val haloPath = Path()
        private val drawMatrix = Matrix()
        private val tempRect = RectF()

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)

            val appContext = applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(
                appContext,
                MusicLiveWallpaperServiceEntryPoint::class.java
            )
            userPreferencesRepository = entryPoint.userPreferencesRepository()
            playbackStateHolder = entryPoint.playbackStateHolder()

            val roundedFont = runCatching {
                ResourcesCompat.getFont(appContext, R.font.gflex_variable)
            }.getOrNull()
            val boldTypeface = if (roundedFont != null) {
                Typeface.create(roundedFont, Typeface.BOLD)
            } else {
                Typeface.create("sans-serif", Typeface.BOLD)
            }
            val mediumTypeface = roundedFont ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)
            titleTextPaint.typeface = boldTypeface
            artistTextPaint.typeface = mediumTypeface

            // Observe settings changes
            engineScope.launch {
                var isFirstSettings = true
                userPreferencesRepository.musicLiveWallpaperSettingsFlow.collectLatest { newSettings ->
                    val oldSettings = settings
                    settings = newSettings

                    if (isFirstSettings) {
                        isFirstSettings = false
                        val initialKind = WallpaperExpressiveShapeKind.fromIndex(newSettings.shapeIndex)
                        fromShapeKind = initialKind
                        toShapeKind = initialKind
                        shapeMorphProgress = 1f
                        currentPosY = newSettings.shapePosition.verticalCenterFraction()
                        posStartY = currentPosY
                        posTargetY = currentPosY
                        currentModeT = if (newSettings.styleMode == MusicWallpaperStyleMode.MATERIAL_YOU_SHAPE) 1f else 0f
                        modeStartT = currentModeT
                        modeTargetT = currentModeT
                    } else {
                        // Animate shape change if shapeIndex changed
                        val nextKind = WallpaperExpressiveShapeKind.fromIndex(newSettings.shapeIndex)
                        if (nextKind != toShapeKind) {
                            triggerShapeMorphTo(nextKind)
                        }
                        // Animate vertical position change
                        val nextPosY = newSettings.shapePosition.verticalCenterFraction()
                        if (kotlin.math.abs(nextPosY - posTargetY) > 0.001f) {
                            posStartY = currentPosY
                            posTargetY = nextPosY
                            posAnimStartMs = SystemClock.uptimeMillis()
                            posAnimProgress = 0f
                        }
                        // Animate style mode change (Full Screen <-> Material You Shape)
                        val nextModeT = if (newSettings.styleMode == MusicWallpaperStyleMode.MATERIAL_YOU_SHAPE) 1f else 0f
                        if (kotlin.math.abs(nextModeT - modeTargetT) > 0.001f) {
                            modeStartT = currentModeT
                            modeTargetT = nextModeT
                            modeAnimStartMs = SystemClock.uptimeMillis()
                            modeAnimProgress = 0f
                        }
                    }

                    // Reload fallback or blur if changed
                    if (oldSettings.fallbackWallpaperPath != newSettings.fallbackWallpaperPath ||
                        oldSettings.fallbackWallpaperUpdatedAt != newSettings.fallbackWallpaperUpdatedAt ||
                        kotlin.math.abs(oldSettings.blurAmount - newSettings.blurAmount) > 0.005f
                    ) {
                        refreshBitmaps(animateCrossfade = oldSettings.fallbackWallpaperPath != newSettings.fallbackWallpaperPath && !currentPlayback.isPlaying)
                    } else {
                        scheduleNextFrame()
                    }
                }
            }

            // Observe PlaybackStateHolder for immediate in-app state sync
            engineScope.launch {
                playbackStateHolder.stablePlayerState.collectLatest { stable ->
                    val song = stable.currentSong
                    if (song != null) {
                        MusicLiveWallpaperStateHolder.updatePlaybackQuickState(
                            songTitle = song.title,
                            artistName = song.displayArtist.takeIf { it.isNotBlank() } ?: song.artist,
                            albumArtUriString = song.albumArtUriString,
                            isPlaying = stable.isPlaying
                        )
                    } else if (!stable.isPlaying && currentPlayback.isPlaying) {
                        MusicLiveWallpaperStateHolder.updatePlaybackQuickState(
                            songTitle = currentPlayback.songTitle,
                            artistName = currentPlayback.artistName,
                            albumArtUriString = currentPlayback.albumArtUriString,
                            isPlaying = false
                        )
                    }
                }
            }

            // Observe unified wallpaper playback snapshot
            engineScope.launch {
                var isFirstPlayback = true
                MusicLiveWallpaperStateHolder.playbackState.collectLatest { snapshot ->
                    val prev = currentPlayback
                    currentPlayback = snapshot

                    val newTitle = snapshot.songTitle.trim()
                    val newArtist = snapshot.artistName.trim()
                    if (newTitle != displayedTitle || newArtist != displayedArtist) {
                        if (isFirstPlayback || displayedTitle.isEmpty()) {
                            displayedTitle = newTitle
                            displayedArtist = newArtist
                            previousTitle = newTitle
                            previousArtist = newArtist
                            textCrossfadeProgress = 1f
                        } else if (newTitle.isNotEmpty()) {
                            previousTitle = displayedTitle
                            previousArtist = displayedArtist
                            displayedTitle = newTitle
                            displayedArtist = newArtist
                            textCrossfadeStartMs = SystemClock.uptimeMillis()
                            textCrossfadeProgress = 0f
                        }
                    }

                    val trackChanged = prev.albumArtUriString != snapshot.albumArtUriString &&
                        !snapshot.albumArtUriString.isNullOrBlank()
                    val playStateChanged = prev.isPlaying != snapshot.isPlaying

                    if (!isFirstPlayback && trackChanged && snapshot.isPlaying && settings.animateShapeOnTrackChange) {
                        val nextIdx = WallpaperExpressiveShapeKind.nextIndex(settings.shapeIndex)
                        val nextKind = WallpaperExpressiveShapeKind.fromIndex(nextIdx)
                        triggerShapeMorphTo(nextKind)
                    }

                    isFirstPlayback = false
                    if (trackChanged || playStateChanged || activeSharpBitmap == null) {
                        refreshBitmaps(animateCrossfade = true)
                    } else {
                        scheduleNextFrame()
                    }
                }
            }
        }

        private fun triggerShapeMorphTo(targetKind: WallpaperExpressiveShapeKind) {
            fromShapeKind = if (shapeMorphProgress < 0.5f) fromShapeKind else toShapeKind
            toShapeKind = targetKind
            shapeMorphStartMs = SystemClock.uptimeMillis()
            shapeMorphDurationMs = 820L
            shapeMorphProgress = 0f
            spinStartDeg = currentSpinDeg
            spinTargetDeg = currentSpinDeg + 95f
            scheduleNextFrame()
        }

        private fun refreshBitmaps(animateCrossfade: Boolean) {
            imageLoadJob?.cancel()
            imageLoadJob = engineScope.launch {
                val appContext = applicationContext
                val s = settings
                val p = currentPlayback

                // Ensure fallback bitmap is loaded if configured
                val fb = if (s.fallbackWallpaperPath.isNotBlank()) {
                    MusicLiveWallpaperStateHolder.loadFallbackWallpaperBitmap(
                        appContext,
                        s.fallbackWallpaperPath,
                        s.fallbackWallpaperUpdatedAt
                    )
                } else {
                    null
                }
                fallbackBitmap = fb

                // Determine target image based on whether music is playing
                val shouldShowMusicArt = p.isPlaying && (!p.albumArtUriString.isNullOrBlank() || p.albumArtBytes != null)
                val targetKey = if (shouldShowMusicArt) {
                    "music_${p.albumArtUriString ?: "bytes_${p.albumArtBytes?.size ?: 0}"}"
                } else {
                    "fallback_${s.fallbackWallpaperPath}_${s.fallbackWallpaperUpdatedAt}"
                }

                val targetSharp: Bitmap? = if (shouldShowMusicArt) {
                    MusicLiveWallpaperStateHolder.loadCachedAlbumArtBitmap(
                        appContext,
                        p.albumArtUriString,
                        p.albumArtBytes
                    ) ?: fb
                } else {
                    fb
                }

                val targetBlurred: Bitmap? = if (targetSharp != null) {
                    withContext(Dispatchers.Default) {
                        MusicLiveWallpaperStateHolder.getOrCreateBlurredBitmap(
                            source = targetSharp,
                            sourceKey = targetKey,
                            blurAmount = s.blurAmount
                        )
                    }
                } else {
                    null
                }

                val keyChanged = targetKey != activeArtKey && activeArtKey.isNotEmpty()
                if (animateCrossfade && keyChanged && surfaceWidth > 0 && surfaceHeight > 0 && currentFrameBuffer != null) {
                    // Capture the exact current visual frame into outgoingFrameSnapshot so the crossfade
                    // starts seamlessly from whatever is currently on screen (even mid-transition!).
                    val snap = ensureSnapshotBitmap( outgoingFrameSnapshot, surfaceWidth, surfaceHeight )
                    val snapCanvas = Canvas(snap)
                    currentFrameBuffer?.let { snapCanvas.drawBitmap(it, 0f, 0f, null) }
                    outgoingFrameSnapshot = snap
                    crossfadeStartMs = SystemClock.uptimeMillis()
                    crossfadeDurationMs = s.crossfadeDurationMs.toLong().coerceIn(800L, 4000L)
                    crossfadeProgress = 0f
                }

                activeArtKey = targetKey
                activeSharpBitmap = targetSharp
                activeBlurredBitmap = targetBlurred
                scheduleNextFrame()
            }
        }

        private fun ensureSnapshotBitmap(existing: Bitmap?, w: Int, h: Int): Bitmap {
            if (existing != null && !existing.isRecycled && existing.width == w && existing.height == h) {
                return existing
            }
            return Bitmap.createBitmap(w.coerceAtLeast(1), h.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isEngineVisible = visible
            if (visible) {
                lastFrameRealtimeMs = SystemClock.uptimeMillis()
                scheduleNextFrame()
            } else {
                stopFrameCallback()
            }
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width
            surfaceHeight = height
            val density = applicationContext.resources.displayMetrics.density
            titleTextPaint.textSize = 20f * density
            artistTextPaint.textSize = 14f * density
            borderPaint.strokeWidth = 1.5f * density
            topScrimPaint.shader = LinearGradient(
                0f,
                0f,
                0f,
                height * 0.24f,
                intArrayOf(
                    Color.argb(145, 0, 0, 0),
                    Color.argb(65, 0, 0, 0),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
            scheduleNextFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            isEngineVisible = false
            stopFrameCallback()
        }

        override fun onDestroy() {
            stopFrameCallback()
            engineScope.cancel()
            super.onDestroy()
        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            if (!settings.tapWallpaperToChangeShape) return
            if (settings.styleMode != MusicWallpaperStyleMode.MATERIAL_YOU_SHAPE) return
            if (!currentPlayback.isPlaying && fallbackBitmap != null) return

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownX = event.x
                    touchDownY = event.y
                    touchDownTimeMs = SystemClock.uptimeMillis()
                }
                MotionEvent.ACTION_UP -> {
                    val dt = SystemClock.uptimeMillis() - touchDownTimeMs
                    val dist = hypot((event.x - touchDownX).toDouble(), (event.y - touchDownY).toDouble())
                    val density = applicationContext.resources.displayMetrics.density
                    if (dt < 320L && dist < 24f * density && surfaceWidth > 0 && surfaceHeight > 0) {
                        val cx = surfaceWidth * 0.5f
                        val cy = surfaceHeight * currentPosY
                        val radius = min(surfaceWidth, surfaceHeight) * 0.36f * settings.shapeScale
                        val tapDistFromCenter = hypot((event.x - cx).toDouble(), (event.y - cy).toDouble())
                        if (tapDistFromCenter <= radius * 1.12f) {
                            val nextIdx = WallpaperExpressiveShapeKind.nextIndex(settings.shapeIndex)
                            val nextKind = WallpaperExpressiveShapeKind.fromIndex(nextIdx)
                            triggerShapeMorphTo(nextKind)
                            engineScope.launch {
                                userPreferencesRepository.setMusicWallpaperShapeIndex(nextIdx)
                            }
                        }
                    }
                }
            }
        }

        private fun scheduleNextFrame() {
            if (!isEngineVisible) return
            if (!isFrameCallbackPosted) {
                isFrameCallbackPosted = true
                choreographer.postFrameCallback(this)
            }
        }

        private fun stopFrameCallback() {
            if (isFrameCallbackPosted) {
                choreographer.removeFrameCallback(this)
                isFrameCallbackPosted = false
            }
        }

        override fun doFrame(frameTimeNanos: Long) {
            isFrameCallbackPosted = false
            if (!isEngineVisible || surfaceWidth <= 0 || surfaceHeight <= 0) return

            val nowMs = SystemClock.uptimeMillis()
            val dtMs = if (lastFrameRealtimeMs > 0L) (nowMs - lastFrameRealtimeMs).coerceIn(0L, 64L) else 16L
            lastFrameRealtimeMs = nowMs

            var keepAnimating = false

            // 1. Update scene crossfade progress
            if (crossfadeProgress < 1f) {
                val elapsed = (nowMs - crossfadeStartMs).coerceAtLeast(0L)
                val rawT = (elapsed.toFloat() / crossfadeDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
                crossfadeProgress = evaluateSmoothWallpaperEasing(rawT)
                if (rawT < 1f) keepAnimating = true else crossfadeProgress = 1f
            }

            // 2. Update Material You shape morph + tap spin
            if (shapeMorphProgress < 1f) {
                val elapsed = (nowMs - shapeMorphStartMs).coerceAtLeast(0L)
                val rawT = (elapsed.toFloat() / shapeMorphDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
                val eased = evaluateSmoothWallpaperEasing(rawT)
                shapeMorphProgress = eased
                currentSpinDeg = lerp(spinStartDeg, spinTargetDeg, eased)
                if (rawT < 1f) keepAnimating = true else shapeMorphProgress = 1f
            }

            // 3. Update slow ambient spin while playing in Material You Shape mode
            val shouldAmbientSpin = currentPlayback.isPlaying &&
                settings.slowRotateWhilePlaying &&
                currentModeT > 0.02f
            if (shouldAmbientSpin) {
                continuousSpinDeg = (continuousSpinDeg + (dtMs * 0.010f)) % 360f
                keepAnimating = true
            }

            // 4. Update vertical position animation
            if (posAnimProgress < 1f) {
                val elapsed = (nowMs - posAnimStartMs).coerceAtLeast(0L)
                val rawT = (elapsed.toFloat() / 680f).coerceIn(0f, 1f)
                val eased = evaluateSmoothWallpaperEasing(rawT)
                posAnimProgress = eased
                currentPosY = lerp(posStartY, posTargetY, eased)
                if (rawT < 1f) keepAnimating = true else posAnimProgress = 1f
            }

            // 5. Update style mode morph (Full Screen <-> Material You Shape)
            if (modeAnimProgress < 1f) {
                val elapsed = (nowMs - modeAnimStartMs).coerceAtLeast(0L)
                val rawT = (elapsed.toFloat() / 720f).coerceIn(0f, 1f)
                val eased = evaluateSmoothWallpaperEasing(rawT)
                modeAnimProgress = eased
                currentModeT = lerp(modeStartT, modeTargetT, eased)
                if (rawT < 1f) keepAnimating = true else modeAnimProgress = 1f
            }

            // 6. Update top-left song info visibility and text crossfade
            val targetSongInfoAlpha = if (settings.showSongInfo &&
                currentPlayback.isPlaying &&
                displayedTitle.isNotBlank()
            ) 1f else 0f
            if (kotlin.math.abs(songInfoVisibilityAlpha - targetSongInfoAlpha) > 0.004f) {
                val step = (dtMs / 420f).coerceIn(0.01f, 0.25f)
                songInfoVisibilityAlpha = if (songInfoVisibilityAlpha < targetSongInfoAlpha) {
                    (songInfoVisibilityAlpha + step).coerceAtMost(targetSongInfoAlpha)
                } else {
                    (songInfoVisibilityAlpha - step).coerceAtLeast(targetSongInfoAlpha)
                }
                keepAnimating = true
            } else {
                songInfoVisibilityAlpha = targetSongInfoAlpha
            }

            if (textCrossfadeProgress < 1f) {
                val elapsed = (nowMs - textCrossfadeStartMs).coerceAtLeast(0L)
                val rawT = (elapsed.toFloat() / 560f).coerceIn(0f, 1f)
                textCrossfadeProgress = evaluateSmoothWallpaperEasing(rawT)
                if (rawT < 1f) keepAnimating = true else textCrossfadeProgress = 1f
            }

            drawWallpaperFrame()

            if (keepAnimating) {
                scheduleNextFrame()
            }
        }

        private fun drawWallpaperFrame() {
            val holder = surfaceHolder ?: return
            val w = surfaceWidth
            val h = surfaceHeight
            if (w <= 0 || h <= 0) return

            val buffer = ensureSnapshotBitmap(currentFrameBuffer, w, h)
            currentFrameBuffer = buffer
            val bufferCanvas = Canvas(buffer)

            // Render active scene onto bufferCanvas
            renderActiveScene(bufferCanvas, w.toFloat(), h.toFloat())

            // Blend outgoing snapshot with active scene during crossfade
            val outgoing = outgoingFrameSnapshot
            if (crossfadeProgress < 0.999f && outgoing != null && !outgoing.isRecycled) {
                // Draw outgoing base, then active scene on top with crossfadeProgress alpha
                var lockCanvas: Canvas? = null
                try {
                    lockCanvas = holder.lockHardwareCanvas() ?: holder.lockCanvas()
                    if (lockCanvas != null) {
                        bitmapPaint.alpha = 255
                        lockCanvas.drawBitmap(outgoing, 0f, 0f, bitmapPaint)
                        bitmapPaint.alpha = (crossfadeProgress * 255f).roundToInt().coerceIn(0, 255)
                        lockCanvas.drawBitmap(buffer, 0f, 0f, bitmapPaint)
                        bitmapPaint.alpha = 255
                    }
                } catch (_: Exception) {
                } finally {
                    if (lockCanvas != null) {
                        runCatching { holder.unlockCanvasAndPost(lockCanvas) }
                    }
                }
            } else {
                var lockCanvas: Canvas? = null
                try {
                    lockCanvas = holder.lockHardwareCanvas() ?: holder.lockCanvas()
                    if (lockCanvas != null) {
                        bitmapPaint.alpha = 255
                        lockCanvas.drawBitmap(buffer, 0f, 0f, bitmapPaint)
                    }
                } catch (_: Exception) {
                } finally {
                    if (lockCanvas != null) {
                        runCatching { holder.unlockCanvasAndPost(lockCanvas) }
                    }
                }
            }
        }

        private fun renderActiveScene(canvas: Canvas, w: Float, h: Float) {
            canvas.drawColor(Color.rgb(12, 12, 16))

            val sharpBmp = activeSharpBitmap
            val blurredBmp = activeBlurredBitmap ?: sharpBmp

            // When no music is playing, show the user's Default Gallery Wallpaper full-screen!
            val isShowingFallbackIdle = !currentPlayback.isPlaying && fallbackBitmap != null
            val effectiveModeT = if (isShowingFallbackIdle) 0f else currentModeT

            if (sharpBmp == null || sharpBmp.isRecycled) {
                drawFallbackGradient(canvas, w, h)
                return
            }

            if (effectiveModeT <= 0.005f) {
                // FULL SCREEN MODE (or idle default gallery wallpaper)
                drawCenterCropBitmap(canvas, sharpBmp, w, h, 255)
                val dimAlpha = (settings.backgroundDim * 255f).roundToInt().coerceIn(0, 200)
                if (dimAlpha > 0) {
                    dimPaint.color = Color.argb(dimAlpha, 0, 0, 0)
                    canvas.drawRect(0f, 0f, w, h, dimPaint)
                }
            } else {
                // MATERIAL YOU SHAPE MODE (or morphing between Full Screen and Material You Shape)
                // 1. Background layer: blurred album art
                val bgBmp = if (settings.blurAmount > 0.01f && blurredBmp != null && !blurredBmp.isRecycled) {
                    blurredBmp
                } else {
                    sharpBmp
                }
                drawCenterCropBitmap(canvas, bgBmp, w, h, 255)

                // Subtle background dim for depth and icon legibility
                val baseDim = lerp(settings.backgroundDim, (settings.backgroundDim + 0.14f).coerceAtMost(0.78f), effectiveModeT)
                val dimAlpha = (baseDim * 255f).roundToInt().coerceIn(0, 210)
                if (dimAlpha > 0) {
                    dimPaint.color = Color.argb(dimAlpha, 0, 0, 0)
                    canvas.drawRect(0f, 0f, w, h, dimPaint)
                }

                // 2. Foreground Material You Expressive Shape
                val cx = w * 0.5f
                val cy = h * currentPosY
                val shapeDiameter = min(w, h) * 0.72f * settings.shapeScale
                val totalRotRad = Math.toRadians((currentSpinDeg + continuousSpinDeg).toDouble())

                // Subtle expressive halo behind the shape
                if (effectiveModeT > 0.25f) {
                    val haloDiameter = shapeDiameter * 1.065f
                    buildMorphingWallpaperAndroidPath(
                        outPath = haloPath,
                        canvasWidth = w,
                        canvasHeight = h,
                        centerX = cx,
                        centerY = cy,
                        shapeDiameterPx = haloDiameter,
                        fromShape = fromShapeKind,
                        toShape = toShapeKind,
                        shapeMorphProgress = shapeMorphProgress,
                        shapeRotationRad = totalRotRad + Math.toRadians(16.0),
                        modeMorphT = 1f
                    )
                    haloPaint.alpha = (effectiveModeT * 42f).roundToInt().coerceIn(0, 60)
                    canvas.drawPath(haloPath, haloPaint)
                }

                // Main morphing shape path
                buildMorphingWallpaperAndroidPath(
                    outPath = shapePath,
                    canvasWidth = w,
                    canvasHeight = h,
                    centerX = cx,
                    centerY = cy,
                    shapeDiameterPx = shapeDiameter,
                    fromShape = fromShapeKind,
                    toShape = toShapeKind,
                    shapeMorphProgress = shapeMorphProgress,
                    shapeRotationRad = totalRotRad,
                    modeMorphT = effectiveModeT
                )

                val saveCount = canvas.save()
                canvas.clipPath(shapePath)

                // Draw sharp album art inside the shape bounds (smoothly interpolating bounds between full screen & shape box)
                val boxSize = shapeDiameter * 1.02f
                val left = lerp(0f, cx - boxSize * 0.5f, effectiveModeT)
                val top = lerp(0f, cy - boxSize * 0.5f, effectiveModeT)
                val right = lerp(w, cx + boxSize * 0.5f, effectiveModeT)
                val bottom = lerp(h, cy + boxSize * 0.5f, effectiveModeT)
                tempRect.set(left, top, right, bottom)
                drawCenterCropBitmapInRect(canvas, sharpBmp, tempRect, 255)
                canvas.restoreToCount(saveCount)

                // Subtle specular rim around the shape
                borderPaint.alpha = (effectiveModeT * 40f).roundToInt().coerceIn(0, 60)
                canvas.drawPath(shapePath, borderPaint)
            }

            // 3. Top-left Song Information Overlay (when toggle is enabled and music is playing)
            if (songInfoVisibilityAlpha > 0.01f && displayedTitle.isNotBlank()) {
                val density = applicationContext.resources.displayMetrics.density
                val startX = 24f * density
                val topY = 58f * density
                val maxTextWidth = (w - startX * 2f).coerceAtLeast(80f)

                topScrimPaint.alpha = (songInfoVisibilityAlpha * 180f).roundToInt().coerceIn(0, 255)
                canvas.drawRect(0f, 0f, w, h * 0.22f, topScrimPaint)

                if (textCrossfadeProgress < 0.999f && previousTitle.isNotBlank() && previousTitle != displayedTitle) {
                    val outAlpha = ((1f - textCrossfadeProgress) * songInfoVisibilityAlpha).coerceIn(0f, 1f)
                    val outShiftY = -textCrossfadeProgress * 8f * density
                    drawSongInfoTextBlock(
                        canvas = canvas,
                        title = previousTitle,
                        artist = previousArtist,
                        x = startX,
                        y = topY + outShiftY,
                        maxWidth = maxTextWidth,
                        alpha = outAlpha,
                        density = density
                    )
                }

                val inAlpha = (textCrossfadeProgress * songInfoVisibilityAlpha).coerceIn(0f, 1f)
                val inShiftY = (1f - textCrossfadeProgress) * 8f * density
                drawSongInfoTextBlock(
                    canvas = canvas,
                    title = displayedTitle,
                    artist = displayedArtist,
                    x = startX,
                    y = topY + inShiftY,
                    maxWidth = maxTextWidth,
                    alpha = inAlpha,
                    density = density
                )
            }
        }

        private fun drawSongInfoTextBlock(
            canvas: Canvas,
            title: String,
            artist: String,
            x: Float,
            y: Float,
            maxWidth: Float,
            alpha: Float,
            density: Float
        ) {
            if (alpha <= 0.01f) return
            titleTextPaint.alpha = (alpha * 245f).roundToInt().coerceIn(0, 255)
            artistTextPaint.alpha = (alpha * 205f).roundToInt().coerceIn(0, 255)

            val ellipsizedTitle = TextUtils.ellipsize(title, titleTextPaint, maxWidth, TextUtils.TruncateAt.END).toString()
            val titleBaseline = y - titleTextPaint.ascent()
            canvas.drawText(ellipsizedTitle, x, titleBaseline, titleTextPaint)

            if (artist.isNotBlank()) {
                val ellipsizedArtist = TextUtils.ellipsize(artist, artistTextPaint, maxWidth, TextUtils.TruncateAt.END).toString()
                val artistBaseline = titleBaseline + titleTextPaint.descent() + (4f * density) - artistTextPaint.ascent()
                canvas.drawText(ellipsizedArtist, x, artistBaseline, artistTextPaint)
            }
        }

        private fun drawCenterCropBitmap(canvas: Canvas, bitmap: Bitmap, dstW: Float, dstH: Float, alpha: Int) {
            tempRect.set(0f, 0f, dstW, dstH)
            drawCenterCropBitmapInRect(canvas, bitmap, tempRect, alpha)
        }

        private fun drawCenterCropBitmapInRect(canvas: Canvas, bitmap: Bitmap, dst: RectF, alpha: Int) {
            val bw = bitmap.width.toFloat().coerceAtLeast(1f)
            val bh = bitmap.height.toFloat().coerceAtLeast(1f)
            val dw = dst.width().coerceAtLeast(1f)
            val dh = dst.height().coerceAtLeast(1f)

            val scale = max(dw / bw, dh / bh)
            val dx = dst.left + (dw - bw * scale) * 0.5f
            val dy = dst.top + (dh - bh * scale) * 0.5f

            drawMatrix.reset()
            drawMatrix.postScale(scale, scale)
            drawMatrix.postTranslate(dx, dy)

            bitmapPaint.alpha = alpha.coerceIn(0, 255)
            canvas.drawBitmap(bitmap, drawMatrix, bitmapPaint)
            bitmapPaint.alpha = 255
        }

        private fun drawFallbackGradient(canvas: Canvas, w: Float, h: Float) {
            val grad = LinearGradient(
                0f,
                0f,
                w,
                h,
                intArrayOf(
                    Color.rgb(24, 20, 38),
                    Color.rgb(16, 18, 28),
                    Color.rgb(10, 10, 14)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            dimPaint.shader = grad
            canvas.drawRect(0f, 0f, w, h, dimPaint)
            dimPaint.shader = null
        }
    }
}
