package com.saurav.pixelmusic.data.service.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.LruCache
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.saurav.pixelmusic.data.model.PlayerInfo
import com.saurav.pixelmusic.data.preferences.AlbumArtQuality
import com.saurav.pixelmusic.data.preferences.MusicWallpaperShapePosition
import com.saurav.pixelmusic.presentation.components.SmartImageCache
import com.saurav.pixelmusic.utils.AlbumArtUtils
import com.saurav.pixelmusic.utils.LocalArtworkUri
import com.saurav.pixelmusic.utils.ThumbnailUrlUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Shared silky cubic-bezier easing matching `SmoothMorphEasing` on the AOD screen.
 */
val WallpaperSmoothMorphEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * Evaluates the same `CubicBezier(0.22, 1.0, 0.36, 1.0)` curve for raw `Float` progress in
 * Android `WallpaperService` Canvas animations without Compose runtime overhead.
 */
fun evaluateSmoothWallpaperEasing(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    if (x <= 0f) return 0f
    if (x >= 1f) return 1f
    return WallpaperSmoothMorphEasing.transform(x)
}

/**
 * Curated Material You Expressive shapes matching the AOD screen's mathematical polar definitions.
 * Ordered so consecutive taps cycle smoothly through Cookies, Rounded Triangle, Rounded Square,
 * Rounded Rectangle, Pill, Rounded Diamond, Oval, and Soft Pentagon.
 */
enum class WallpaperExpressiveShapeKind(val displayName: String, val shortLabel: String) {
    COOKIE_8("8-Sided Soft Cookie", "Cookie 8") {
        override fun rawRadiusAt(th: Double): Double = 1.0 + 0.036 * cos(8.0 * th)
    },
    ROUNDED_TRIANGLE("Rounded Triangle", "Triangle") {
        override fun rawRadiusAt(th: Double): Double {
            val a = th - PI / 2.0
            return 1.0 + 0.064 * cos(3.0 * a) - 0.011 * cos(6.0 * a)
        }
    },
    COOKIE_6("6-Sided Soft Cookie", "Cookie 6") {
        override fun rawRadiusAt(th: Double): Double = 1.0 + 0.042 * cos(6.0 * th)
    },
    ROUNDED_SQUIRCLE("Rounded Square", "Square") {
        override fun rawRadiusAt(th: Double): Double {
            val c = abs(cos(th))
            val s = abs(sin(th))
            return 1.0 / (c.pow(4.0) + s.pow(4.0)).pow(0.25)
        }
    },
    COOKIE_4("4-Sided Clover Cookie", "Cookie 4") {
        override fun rawRadiusAt(th: Double): Double = 1.0 + 0.046 * cos(4.0 * th + PI / 4.0)
    },
    ROUNDED_RECTANGLE("Rounded Rectangle", "Rectangle") {
        override fun rawRadiusAt(th: Double): Double {
            val c = abs(cos(th)) / 0.84
            val s = abs(sin(th)) / 1.02
            return 1.0 / (c.pow(4.2) + s.pow(4.2)).pow(1.0 / 4.2)
        }
    },
    COOKIE_12("12-Sided Sunny Cookie", "Cookie 12") {
        override fun rawRadiusAt(th: Double): Double = 1.0 + 0.025 * cos(12.0 * th)
    },
    PILL("Material You Pill", "Pill") {
        override fun rawRadiusAt(th: Double): Double {
            val a = th - (28.0 * PI / 180.0)
            val c = abs(cos(a)) / 0.78
            val s = abs(sin(a)) / 1.00
            return 1.0 / (c.pow(4.6) + s.pow(4.6)).pow(1.0 / 4.6)
        }
    },
    COOKIE_7("7-Sided Soft Cookie", "Cookie 7") {
        override fun rawRadiusAt(th: Double): Double = 1.0 + 0.036 * cos(7.0 * th - PI / 2.0)
    },
    ROUNDED_DIAMOND("Rounded Diamond", "Diamond") {
        override fun rawRadiusAt(th: Double): Double {
            val a = th - PI / 4.0
            val c = abs(cos(a))
            val s = abs(sin(a))
            return 1.0 / (c.pow(3.5) + s.pow(3.5)).pow(1.0 / 3.5)
        }
    },
    OVAL("Smooth Tilted Oval", "Oval") {
        override fun rawRadiusAt(th: Double): Double {
            val a = th + (22.0 * PI / 180.0)
            val c = cos(a) / 0.82
            val s = sin(a) / 1.00
            return 1.0 / sqrt(c * c + s * s)
        }
    },
    ROUNDED_PENTAGON("Soft Pentagon", "Pentagon") {
        override fun rawRadiusAt(th: Double): Double {
            val a = th - PI / 2.0
            return 1.0 + 0.034 * cos(5.0 * a) - 0.005 * cos(10.0 * a)
        }
    };

    abstract fun rawRadiusAt(th: Double): Double

    private val normScale: Double by lazy {
        val samples = 180
        var maxR = 0.001
        var sumR = 0.0
        for (i in 0 until samples) {
            val r = rawRadiusAt((2.0 * PI * i) / samples)
            if (r > maxR) maxR = r
            sumR += r
        }
        val meanR = sumR / samples
        (0.62 * maxR + 0.38 * meanR).coerceAtLeast(maxR * 0.94)
    }

    fun normalizedRadiusAt(th: Double): Float {
        return (rawRadiusAt(th) / normScale).toFloat().coerceAtMost(1.04f)
    }

    companion object {
        fun fromIndex(index: Int): WallpaperExpressiveShapeKind {
            val list = entries
            if (list.isEmpty()) return COOKIE_8
            return list[index.mod(list.size)]
        }

        fun nextIndex(currentIndex: Int): Int {
            val list = entries
            if (list.size <= 1) return 0
            return (currentIndex + 1).mod(list.size)
        }
    }
}

/**
 * Vertical center fraction on the screen/preview for Top, Middle, and Bottom shape placement.
 * Top leaves comfortable breathing room below the top-left song information overlay.
 */
fun MusicWallpaperShapePosition.verticalCenterFraction(): Float = when (this) {
    MusicWallpaperShapePosition.TOP -> 0.32f
    MusicWallpaperShapePosition.MIDDLE -> 0.51f
    MusicWallpaperShapePosition.BOTTOM -> 0.71f
}

/**
 * Builds an `android.graphics.Path` that smoothly morphs between:
 * - Full-screen rectangle (`modeMorphT = 0f`) and the Material You Expressive shape (`modeMorphT = 1f`)
 * - `fromShape` and `toShape` (`shapeMorphProgress = 0f..1f`) with `shapeRotationRad` spin.
 */
fun buildMorphingWallpaperAndroidPath(
    outPath: android.graphics.Path,
    canvasWidth: Float,
    canvasHeight: Float,
    centerX: Float,
    centerY: Float,
    shapeDiameterPx: Float,
    fromShape: WallpaperExpressiveShapeKind,
    toShape: WallpaperExpressiveShapeKind,
    shapeMorphProgress: Float,
    shapeRotationRad: Double,
    modeMorphT: Float = 1f,
    steps: Int = 240
) {
    outPath.reset()
    val w = canvasWidth.coerceAtLeast(1f)
    val h = canvasHeight.coerceAtLeast(1f)
    val baseRadius = (shapeDiameterPx * 0.482f).coerceAtLeast(1f)
    val clampedShapeMorph = shapeMorphProgress.coerceIn(0f, 1f)
    val clampedMode = modeMorphT.coerceIn(0f, 1f)
    val count = steps.coerceAtLeast(120)
    val twoPi = 2.0 * PI

    // Distance to screen edges from (centerX, centerY) for smooth Full Screen <-> Shape morphing
    val distLeft = centerX.coerceAtLeast(1f)
    val distRight = (w - centerX).coerceAtLeast(1f)
    val distTop = centerY.coerceAtLeast(1f)
    val distBottom = (h - centerY).coerceAtLeast(1f)
    val minScreenDim = min(w, h)
    val cornerR = minScreenDim * (clampedMode * 2.4f).coerceAtMost(0.45f)

    for (i in 0 until count) {
        val theta = (twoPi * i) / count
        val cosT = cos(theta).toFloat()
        val sinT = sin(theta).toFloat()

        val rotatedTheta = theta - shapeRotationRad
        val rFrom = fromShape.normalizedRadiusAt(rotatedTheta)
        val rTo = toShape.normalizedRadiusAt(rotatedTheta)
        val rShape = baseRadius * lerp(rFrom, rTo, clampedShapeMorph)

        val finalR = if (clampedMode >= 0.999f) {
            rShape
        } else {
            val rx = if (cosT >= 0f) distRight / cosT.coerceAtLeast(1e-5f) else distLeft / (-cosT).coerceAtLeast(1e-5f)
            val ry = if (sinT >= 0f) distBottom / sinT.coerceAtLeast(1e-5f) else distTop / (-sinT).coerceAtLeast(1e-5f)
            var rFull = min(rx, ry)
            if (cornerR > 1f) {
                val halfSpan = min(distLeft, distRight)
                val halfVert = min(distTop, distBottom)
                val absCos = abs(cosT).coerceAtLeast(1e-5f)
                val absSin = abs(sinT).coerceAtLeast(1e-5f)
                val icx = (halfSpan - cornerR).coerceAtLeast(0f)
                val icy = (halfVert - cornerR).coerceAtLeast(0f)
                val px = rFull * absCos
                val py = rFull * absSin
                if (px > icx && py > icy) {
                    val dot = icx * absCos + icy * absSin
                    val cVal = icx * icx + icy * icy - cornerR * cornerR
                    val disc = (dot * dot - cVal).coerceAtLeast(0f)
                    rFull = min(rFull, dot + sqrt(disc))
                }
            }
            lerp(rFull, rShape, clampedMode)
        }

        val x = (centerX + finalR * cosT).coerceIn(0f, w)
        val y = (centerY + finalR * sinT).coerceIn(0f, h)
        if (i == 0) outPath.moveTo(x, y) else outPath.lineTo(x, y)
    }
    outPath.close()
}

/**
 * Compose [Shape] wrapper around the polar Material You Expressive shape equation for the preview screen.
 */
class WallpaperMorphingComposeShape(
    private val fromShape: WallpaperExpressiveShapeKind,
    private val toShape: WallpaperExpressiveShapeKind,
    private val shapeMorphProgress: Float,
    private val shapeRotationRad: Double,
    private val steps: Int = 240
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width.coerceAtLeast(1f)
        val h = size.height.coerceAtLeast(1f)
        val cx = w * 0.5f
        val cy = h * 0.5f
        val baseRadius = min(w, h) * 0.482f
        val clampedMorph = shapeMorphProgress.coerceIn(0f, 1f)
        val count = steps.coerceAtLeast(120)
        val twoPi = 2.0 * PI

        val path = Path().apply {
            for (i in 0 until count) {
                val theta = (twoPi * i) / count
                val cosT = cos(theta).toFloat()
                val sinT = sin(theta).toFloat()
                val rotatedTheta = theta - shapeRotationRad
                val rFrom = fromShape.normalizedRadiusAt(rotatedTheta)
                val rTo = toShape.normalizedRadiusAt(rotatedTheta)
                val r = baseRadius * lerp(rFrom, rTo, clampedMorph)
                val x = (cx + r * cosT).coerceIn(0f, w)
                val y = (cy + r * sinT).coerceIn(0f, h)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

data class WallpaperPlaybackSnapshot(
    val songTitle: String = "",
    val artistName: String = "",
    val albumArtUriString: String? = null,
    val isPlaying: Boolean = false,
    val albumArtBytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WallpaperPlaybackSnapshot) return false
        return songTitle == other.songTitle &&
            artistName == other.artistName &&
            albumArtUriString == other.albumArtUriString &&
            isPlaying == other.isPlaying &&
            ((albumArtBytes == null) == (other.albumArtBytes == null))
    }

    override fun hashCode(): Int {
        var result = songTitle.hashCode()
        result = 31 * result + artistName.hashCode()
        result = 31 * result + (albumArtUriString?.hashCode() ?: 0)
        result = 31 * result + isPlaying.hashCode()
        result = 31 * result + (albumArtBytes?.size ?: 0)
        return result
    }
}

/**
 * Central state & zero-network artwork cache manager for Music Live Wallpaper.
 *
 * Strictly reuses album art already downloaded by Now Playing, Mini Player, Notification, or
 * local storage (`CachePolicy.DISABLED` for network requests) so the wallpaper never consumes
 * extra mobile/Wi-Fi data.
 */
object MusicLiveWallpaperStateHolder {
    private const val FALLBACK_FILE_NAME = "music_live_wallpaper_fallback.jpg"
    private const val MAX_ARTWORK_DIMENSION_PX = 1440
    private const val MAX_FALLBACK_DIMENSION_PX = 1920

    private val _playbackState = MutableStateFlow(WallpaperPlaybackSnapshot())
    val playbackState: StateFlow<WallpaperPlaybackSnapshot> = _playbackState.asStateFlow()

    private val bitmapCache = LruCache<String, Bitmap>(16)
    private val blurredBitmapCache = LruCache<String, Bitmap>(24)

    @Volatile
    private var cachedFallbackPath: String = ""

    @Volatile
    private var cachedFallbackUpdatedAt: Long = -1L

    @Volatile
    private var cachedFallbackBitmap: Bitmap? = null

    fun updateFromPlayerInfo(playerInfo: PlayerInfo) {
        _playbackState.update { current ->
            val resolvedArtBytes = playerInfo.albumArtBitmapData
                ?: if (current.albumArtUriString == playerInfo.albumArtUri) current.albumArtBytes else null
            WallpaperPlaybackSnapshot(
                songTitle = playerInfo.songTitle,
                artistName = playerInfo.artistName,
                albumArtUriString = playerInfo.albumArtUri,
                isPlaying = playerInfo.isPlaying,
                albumArtBytes = resolvedArtBytes
            )
        }
    }

    fun updatePlaybackQuickState(
        songTitle: String,
        artistName: String,
        albumArtUriString: String?,
        isPlaying: Boolean
    ) {
        _playbackState.update { current ->
            val keepBytes = if (current.albumArtUriString == albumArtUriString) current.albumArtBytes else null
            WallpaperPlaybackSnapshot(
                songTitle = songTitle,
                artistName = artistName,
                albumArtUriString = albumArtUriString,
                isPlaying = isPlaying,
                albumArtBytes = keepBytes
            )
        }
    }

    /**
     * Loads the already-downloaded album art [Bitmap] from Coil's memory cache, Coil's disk cache,
     * local media streams, or `MusicService` embedded bytes — NEVER downloading over the network again.
     */
    suspend fun loadCachedAlbumArtBitmap(
        context: Context,
        rawUriString: String?,
        embeddedArtBytes: ByteArray? = null
    ): Bitmap? = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val cleanUri = rawUriString?.trim()?.takeIf { it.isNotEmpty() }

        if (cleanUri != null) {
            val effectiveQuality = SmartImageCache.getEffectiveQuality()
            val optimizedEffective = ThumbnailUrlUtils.optimizeArtworkUrl(cleanUri, effectiveQuality) ?: cleanUri
            val canonicalEffective = ThumbnailUrlUtils.getCanonicalArtworkDiskCacheKey(optimizedEffective, effectiveQuality)
                ?: optimizedEffective

            synchronized(bitmapCache) {
                bitmapCache.get(canonicalEffective)?.takeIf { !it.isRecycled }?.let { return@withContext it }
                bitmapCache.get(cleanUri)?.takeIf { !it.isRecycled }?.let { return@withContext it }
            }

            // Poll briefly if Now Playing / Notification is currently writing the image into Coil's cache
            val candidateQualities = listOf(effectiveQuality, AlbumArtQuality.HIGH, AlbumArtQuality.LOW).distinct()
            val isRemote = cleanUri.startsWith("http://", ignoreCase = true) ||
                cleanUri.startsWith("https://", ignoreCase = true)

            val maxAttempts = if (isRemote) 12 else 2
            for (attempt in 0 until maxAttempts) {
                // 1. Check Coil MemoryCache across all canonical keys
                findInCoilMemoryCache(appContext, cleanUri, candidateQualities)?.let { bmp ->
                    val soft = ensureSoftwareBitmap(bmp)
                    synchronized(bitmapCache) {
                        bitmapCache.put(canonicalEffective, soft)
                    }
                    return@withContext soft
                }

                // 2. Check Coil DiskCache snapshots directly on disk (zero network!)
                findInCoilDiskCache(appContext, cleanUri, candidateQualities)?.let { bmp ->
                    synchronized(bitmapCache) {
                        bitmapCache.put(canonicalEffective, bmp)
                    }
                    return@withContext bmp
                }

                // 3. Check local URI streams (content://, file://, LocalArtworkUri)
                if (!isRemote) {
                    decodeLocalArtworkUri(appContext, cleanUri)?.let { bmp ->
                        synchronized(bitmapCache) {
                            bitmapCache.put(canonicalEffective, bmp)
                        }
                        return@withContext bmp
                    }
                }

                // 4. Try Coil offline-only execution (CachePolicy.DISABLED for network)
                for (q in candidateQualities) {
                    val optUrl = ThumbnailUrlUtils.optimizeArtworkUrl(cleanUri, q) ?: cleanUri
                    val diskKey = ThumbnailUrlUtils.getCanonicalArtworkDiskCacheKey(optUrl, q) ?: optUrl
                    val request = ImageRequest.Builder(appContext)
                        .data(optUrl)
                        .diskCacheKey(diskKey)
                        .memoryCacheKey(diskKey)
                        .size(MAX_ARTWORK_DIMENSION_PX, MAX_ARTWORK_DIMENSION_PX)
                        .precision(Precision.INEXACT)
                        .allowHardware(false)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .networkCachePolicy(CachePolicy.DISABLED) // Strictly forbid re-downloading!
                        .build()

                    val drawable = runCatching { appContext.imageLoader.execute(request).drawable }.getOrNull()
                    if (drawable != null) {
                        val bmp = runCatching {
                            drawable.toBitmap(
                                width = drawable.intrinsicWidth.coerceIn(1, MAX_ARTWORK_DIMENSION_PX),
                                height = drawable.intrinsicHeight.coerceIn(1, MAX_ARTWORK_DIMENSION_PX),
                                config = Bitmap.Config.ARGB_8888
                            )
                        }.getOrNull()
                        if (bmp != null && !bmp.isRecycled) {
                            synchronized(bitmapCache) {
                                bitmapCache.put(canonicalEffective, bmp)
                            }
                            return@withContext bmp
                        }
                    }
                }

                // 5. If embedded bytes from MusicService are available, use them if cache didn't hit on first pass
                val latestEmbedded = embeddedArtBytes ?: _playbackState.value.albumArtBytes
                if (latestEmbedded != null && latestEmbedded.isNotEmpty()) {
                    decodeSampledByteArray(latestEmbedded, MAX_ARTWORK_DIMENSION_PX)?.let { bmp ->
                        synchronized(bitmapCache) {
                            bitmapCache.put(canonicalEffective, bmp)
                        }
                        return@withContext bmp
                    }
                }

                if (attempt < maxAttempts - 1) {
                    delay(220L)
                }
            }
        }

        // Fallback to embedded bytes if URI was null
        val fallbackBytes = embeddedArtBytes ?: _playbackState.value.albumArtBytes
        if (fallbackBytes != null && fallbackBytes.isNotEmpty()) {
            return@withContext decodeSampledByteArray(fallbackBytes, MAX_ARTWORK_DIMENSION_PX)
        }

        null
    }

    private fun findInCoilMemoryCache(
        context: Context,
        rawUri: String,
        qualities: List<AlbumArtQuality>
    ): Bitmap? {
        val memCache = context.imageLoader.memoryCache ?: return null
        val candidateKeys = LinkedHashSet<String>()
        for (q in qualities) {
            val opt = ThumbnailUrlUtils.optimizeArtworkUrl(rawUri, q) ?: rawUri
            val canonical = ThumbnailUrlUtils.getCanonicalArtworkDiskCacheKey(opt, q) ?: opt
            candidateKeys.add(canonical)
            candidateKeys.add(opt)
        }
        candidateKeys.add(rawUri)

        for (keyStr in candidateKeys) {
            val direct = memCache[MemoryCache.Key(keyStr)]?.bitmap
            if (direct != null && !direct.isRecycled) return direct
        }

        // Also check prefix matches (e.g. canonicalDiskKey_widthxheight created by SmartImage)
        val allKeys = runCatching { memCache.keys.toList() }.getOrDefault(emptyList())
        for (cacheKey in allKeys) {
            val k = cacheKey.key
            if (candidateKeys.any { base -> k == base || k.startsWith("${base}_") }) {
                val bmp = memCache[cacheKey]?.bitmap
                if (bmp != null && !bmp.isRecycled) return bmp
            }
        }
        return null
    }

    private fun findInCoilDiskCache(
        context: Context,
        rawUri: String,
        qualities: List<AlbumArtQuality>
    ): Bitmap? {
        val diskCache = context.imageLoader.diskCache ?: return null
        val candidateKeys = LinkedHashSet<String>()
        for (q in qualities) {
            val opt = ThumbnailUrlUtils.optimizeArtworkUrl(rawUri, q) ?: rawUri
            val canonical = ThumbnailUrlUtils.getCanonicalArtworkDiskCacheKey(opt, q) ?: opt
            candidateKeys.add(canonical)
            candidateKeys.add(opt)
        }
        candidateKeys.add(rawUri)
        for (fallbackUrl in ThumbnailUrlUtils.getFallbackArtworkUrls(rawUri)) {
            candidateKeys.add(fallbackUrl)
            ThumbnailUrlUtils.getCanonicalArtworkDiskCacheKey(fallbackUrl)?.let(candidateKeys::add)
        }

        for (key in candidateKeys) {
            val bmp = runCatching {
                diskCache.openSnapshot(key)?.use { snapshot ->
                    decodeSampledFile(snapshot.data.toFile(), MAX_ARTWORK_DIMENSION_PX)
                }
            }.getOrNull()
            if (bmp != null && !bmp.isRecycled) return bmp
        }
        return null
    }

    private fun decodeLocalArtworkUri(context: Context, uriString: String): Bitmap? {
        return runCatching {
            val uri = if (uriString.startsWith("/")) {
                Uri.fromFile(File(uriString))
            } else {
                Uri.parse(uriString)
            }
            val bytes = AlbumArtUtils.openArtworkInputStream(context, uri)?.use { it.readBytes() }
                ?: context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@runCatching null
            decodeSampledByteArray(bytes, MAX_ARTWORK_DIMENSION_PX)
        }.getOrNull()
    }

    private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
            return bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
        }
        return bitmap
    }

    /**
     * Copies and optimizes the user-selected gallery fallback wallpaper into internal app storage
     * so it persists across reboots and loads instantaneously.
     */
    suspend fun saveFallbackWallpaperFromUri(context: Context, sourceUri: Uri): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val appContext = context.applicationContext
                val rawBytes = appContext.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
                    ?: return@runCatching null
                if (rawBytes.isEmpty()) return@runCatching null

                val rotationDeg = runCatching {
                    val exif = ExifInterface( rawBytes.inputStream() )
                    when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                }.getOrDefault(0f)

                val decoded = decodeSampledByteArray(rawBytes, MAX_FALLBACK_DIMENSION_PX)
                    ?: return@runCatching null

                val oriented = if (rotationDeg != 0f) {
                    val matrix = Matrix().apply { postRotate(rotationDeg) }
                    Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                } else {
                    decoded
                }

                val destFile = File(appContext.filesDir, FALLBACK_FILE_NAME)
                FileOutputStream(destFile).use { out ->
                    oriented.compress(Bitmap.CompressFormat.JPEG, 94, out)
                    out.flush()
                }

                cachedFallbackPath = destFile.absolutePath
                cachedFallbackUpdatedAt = System.currentTimeMillis()
                cachedFallbackBitmap = oriented
                destFile.absolutePath
            }.getOrNull()
        }

    suspend fun clearFallbackWallpaper(context: Context) = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.applicationContext.filesDir, FALLBACK_FILE_NAME)
            if (file.exists()) file.delete()
        }
        cachedFallbackPath = ""
        cachedFallbackUpdatedAt = 0L
        cachedFallbackBitmap = null
    }

    suspend fun loadFallbackWallpaperBitmap(
        context: Context,
        path: String,
        updatedAt: Long
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (path.isBlank()) return@withContext null
        val existing = cachedFallbackBitmap
        if (existing != null && !existing.isRecycled &&
            cachedFallbackPath == path && cachedFallbackUpdatedAt == updatedAt
        ) {
            return@withContext existing
        }

        val loaded = runCatching {
            val file = File(path)
            if (file.exists() && file.length() > 0L) {
                decodeSampledFile(file, MAX_FALLBACK_DIMENSION_PX)
            } else {
                val uri = Uri.parse(path)
                context.applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
                    decodeSampledByteArray(stream.readBytes(), MAX_FALLBACK_DIMENSION_PX)
                }
            }
        }.getOrNull()

        if (loaded != null) {
            cachedFallbackPath = path
            cachedFallbackUpdatedAt = updatedAt
            cachedFallbackBitmap = loaded
        }
        loaded
    }

    /**
     * Fast, artifact-free 3-pass separable box/Gaussian approximation blur on a downscaled buffer.
     * Pre-computed once per artwork/blur-level change so 60/120fps Canvas rendering simply draws
     * a hardware-filtered Bitmap with zero frame drops.
     */
    fun getOrCreateBlurredBitmap(
        source: Bitmap,
        sourceKey: String,
        blurAmount: Float
    ): Bitmap {
        val quantizedBlur = (blurAmount.coerceIn(0f, 1f) * 50f).roundToInt()
        if (quantizedBlur <= 0) return source
        val cacheKey = "${sourceKey}_blur_$quantizedBlur"
        synchronized(blurredBitmapCache) {
            blurredBitmapCache.get(cacheKey)?.takeIf { !it.isRecycled }?.let { return it }
        }

        val blurred = createBlurredBitmapInternal(source, blurAmount.coerceIn(0f, 1f))
        synchronized(blurredBitmapCache) {
            blurredBitmapCache.put(cacheKey, blurred)
        }
        return blurred
    }

    private fun createBlurredBitmapInternal(source: Bitmap, blurAmount: Float): Bitmap {
        val softSource = ensureSoftwareBitmap(source)
        val maxSmallSide = 200
        val srcW = softSource.width.coerceAtLeast(1)
        val srcH = softSource.height.coerceAtLeast(1)
        val scale = min(maxSmallSide.toFloat() / max(srcW, srcH).toFloat(), 1f)
        val w = (srcW * scale).roundToInt().coerceAtLeast(16)
        val h = (srcH * scale).roundToInt().coerceAtLeast(16)

        val small = Bitmap.createScaledBitmap(softSource, w, h, true)
        val radius = (blurAmount * 34f).roundToInt().coerceIn(1, 38)

        val pixels = IntArray(w * h)
        small.getPixels(pixels, 0, w, 0, 0, w, h)
        val temp = IntArray(w * h)

        // 3-pass separable box blur closely approximates a true Gaussian kernel
        repeat(3) {
            boxBlurHorizontal(pixels, temp, w, h, radius)
            boxBlurVertical(temp, pixels, w, h, radius)
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }

    private fun boxBlurHorizontal(src: IntArray, dst: IntArray, w: Int, h: Int, radius: Int) {
        val div = radius * 2 + 1
        for (y in 0 until h) {
            var rSum = 0
            var gSum = 0
            var bSum = 0
            val rowOffset = y * w
            for (i in -radius..radius) {
                val px = src[rowOffset + i.coerceIn(0, w - 1)]
                rSum += (px shr 16) and 0xFF
                gSum += (px shr 8) and 0xFF
                bSum += px and 0xFF
            }
            for (x in 0 until w) {
                val r = rSum / div
                val g = gSum / div
                val b = bSum / div
                dst[rowOffset + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

                val leftIdx = (x - radius).coerceIn(0, w - 1)
                val rightIdx = (x + radius + 1).coerceIn(0, w - 1)
                val pOut = src[rowOffset + leftIdx]
                val pIn = src[rowOffset + rightIdx]
                rSum += ((pIn shr 16) and 0xFF) - ((pOut shr 16) and 0xFF)
                gSum += ((pIn shr 8) and 0xFF) - ((pOut shr 8) and 0xFF)
                bSum += (pIn and 0xFF) - (pOut and 0xFF)
            }
        }
    }

    private fun boxBlurVertical(src: IntArray, dst: IntArray, w: Int, h: Int, radius: Int) {
        val div = radius * 2 + 1
        for (x in 0 until w) {
            var rSum = 0
            var gSum = 0
            var bSum = 0
            for (i in -radius..radius) {
                val py = i.coerceIn(0, h - 1)
                val px = src[py * w + x]
                rSum += (px shr 16) and 0xFF
                gSum += (px shr 8) and 0xFF
                bSum += px and 0xFF
            }
            for (y in 0 until h) {
                val r = rSum / div
                val g = gSum / div
                val b = bSum / div
                dst[y * w + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

                val topIdx = (y - radius).coerceIn(0, h - 1)
                val bottomIdx = (y + radius + 1).coerceIn(0, h - 1)
                val pOut = src[topIdx * w + x]
                val pIn = src[bottomIdx * w + x]
                rSum += ((pIn shr 16) and 0xFF) - ((pOut shr 16) and 0xFF)
                gSum += ((pIn shr 8) and 0xFF) - ((pOut shr 8) and 0xFF)
                bSum += (pIn and 0xFF) - (pOut and 0xFF)
            }
        }
    }

    private fun decodeSampledFile(file: File, maxDimensionPx: Int): Bitmap? {
        if (!file.exists() || file.length() <= 0L) return null
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, boundsOpts)
        if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, maxDimensionPx)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(file.absolutePath, decodeOpts)
    }

    private fun decodeSampledByteArray(bytes: ByteArray, maxDimensionPx: Int): Bitmap? {
        if (bytes.isEmpty()) return null
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOpts)
        if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, maxDimensionPx)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        val longest = max(width, height)
        while (longest / inSampleSize > maxDim) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }
}
