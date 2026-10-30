package com.saurav.pixelmusic.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurav.pixelmusic.R
import com.saurav.pixelmusic.data.model.Song
import com.saurav.pixelmusic.data.preferences.CollagePattern
import com.saurav.pixelmusic.utils.shapes.FluidBlobShape
import com.saurav.pixelmusic.utils.shapes.FluidBlobType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Stable
data class Config(val size: Dp, val width: Dp, val height: Dp, val align: Alignment, val rot: Float, val shape: Shape, val offsetX: Dp, val offsetY: Dp)

@Composable
fun AlbumArtCollage(
    songs: ImmutableList<Song>,
    modifier: Modifier = Modifier,
    height: Dp = 400.dp,
    padding: Dp = 0.dp,
    pattern: CollagePattern = CollagePattern.default,
    onSongClick: (Song) -> Unit,
) {
    if (pattern == CollagePattern.FLUID_BLOB) {
        FluidBlobCollage(
            songs = songs,
            modifier = modifier.padding(padding),
            height = height,
            onSongClick = onSongClick
        )
        return
    }

    val songsToShow = remember(songs) {
        if (songs.isEmpty()) {
            emptyList<Song?>().toImmutableList()
        } else {
            (0 until 6).map { songs.getOrNull(it % songs.size) }.toImmutableList()
        }
    }

    val contentHeight = remember(height, padding) { maxOf(0.dp, height - (padding * 2)) }
    val min = remember(height) { minOf(300.dp, height) }
    val shapeConfigs = remember(pattern, min, contentHeight) {
        buildCollageConfigs(pattern, min, contentHeight)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(padding)
    ) {
        if (shapeConfigs.isNotEmpty()) {
            val (topConfigs, bottomConfigs) = remember(shapeConfigs) {
                shapeConfigs.take(3) to shapeConfigs.drop(3)
            }

            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().height(contentHeight * 0.58f)) {
                    topConfigs.forEachIndexed { idx, cfg ->
                        songsToShow.getOrNull(idx)?.let { song ->
                            val interactionSource = remember(song.id) { MutableInteractionSource() }
                            SmartImage(
                                model = song.albumArtUriString,
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                crossfadeDurationMillis = 0,
                                targetSize = SmartImageListTargetSize,
                                isThumbnail = true,
                                modifier = Modifier
                                    .size(cfg.width, cfg.height)
                                    .align(cfg.align)
                                    .offset(cfg.offsetX, cfg.offsetY)
                                    .graphicsLayer(
                                        rotationZ = cfg.rot,
                                        clip = true,
                                        shape = cfg.shape
                                    )
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary)
                                    ) { onSongClick(song) }
                                    .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                            )
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(contentHeight * 0.42f)) {
                    bottomConfigs.forEachIndexed { j, cfg ->
                        val idx = j + 3
                        songsToShow.getOrNull(idx)?.let { song ->
                            val interactionSource = remember(song.id) { MutableInteractionSource() }
                            SmartImage(
                                model = song.albumArtUriString,
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                crossfadeDurationMillis = 0,
                                targetSize = SmartImageListTargetSize,
                                isThumbnail = true,
                                modifier = Modifier
                                    .size(cfg.width, cfg.height)
                                    .align(cfg.align)
                                    .offset(cfg.offsetX, cfg.offsetY)
                                    .graphicsLayer(
                                        rotationZ = cfg.rot,
                                        clip = true,
                                        shape = cfg.shape
                                    )
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary)
                                    ) { onSongClick(song) }
                                    .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                            )
                        }
                    }
                }
            }
        }

        if (songs.isEmpty()) {
            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.rounded_music_note_24),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Fluid Blob-Grid Overlay with biological asymmetry & Material You Expressive shapes:
 * - 3 Rounded Triangle Blobs in Top-Left, Top-Right, and Bottom-Left corners
 * - 1 Scalloped 7-Sided Cookie in the 4th corner (Bottom-Right)
 * - Material 3 Expressive 4-Lobe Clover Pill & 10-Sided Sunny badge in the middle
 * All items fit together with clean negative space and zero overlapping.
 */
@Composable
fun FluidBlobCollage(
    songs: ImmutableList<Song>,
    modifier: Modifier = Modifier,
    height: Dp = 415.dp,
    onSongClick: (Song) -> Unit
) {
    val songsToShow = remember(songs) {
        if (songs.isEmpty()) {
            emptyList<Song>()
        } else {
            (0 until 6).map { songs[it % songs.size] }
        }
    }

    if (songsToShow.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_music_note_24),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 4.dp)
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        // 1. Top-Left Corner: Rounded Triangle Blob
        val tlW = totalWidth * 0.45f
        val tlH = totalHeight * 0.41f
        FluidBlobItem(
            song = songsToShow.getOrNull(0),
            shape = remember { FluidBlobShape(FluidBlobType.CORNER_TRIANGLE_TL) },
            width = tlW,
            height = tlH,
            haloOffsetX = 3.dp,
            haloOffsetY = 2.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.24f - (tlW + 14.dp) * 0.5f, y = totalHeight * 0.215f - (tlH + 14.dp) * 0.5f),
            onClick = { songsToShow.getOrNull(0)?.let(onSongClick) }
        )

        // 2. Top-Right Corner: Rounded Triangle Blob
        val trW = totalWidth * 0.40f
        val trH = totalHeight * 0.33f
        FluidBlobItem(
            song = songsToShow.getOrNull(1),
            shape = remember { FluidBlobShape(FluidBlobType.CORNER_TRIANGLE_TR) },
            width = trW,
            height = trH,
            haloOffsetX = 2.dp,
            haloOffsetY = 3.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.76f - (trW + 14.dp) * 0.5f, y = totalHeight * 0.19f - (trH + 14.dp) * 0.5f),
            onClick = { songsToShow.getOrNull(1)?.let(onSongClick) }
        )

        // 3. Center Middle: Material You Expressive 4-Lobe Clover Pill
        val centerW = totalWidth * 0.47f
        val centerH = totalHeight * 0.40f
        FluidBlobItem(
            song = songsToShow.getOrNull(2),
            shape = remember { FluidBlobShape(FluidBlobType.CENTER_EXPRESSIVE_CLOVER_PILL) },
            width = centerW,
            height = centerH,
            haloOffsetX = 2.dp,
            haloOffsetY = 2.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.47f - (centerW + 14.dp) * 0.5f, y = totalHeight * 0.495f - (centerH + 14.dp) * 0.5f),
            onClick = { songsToShow.getOrNull(2)?.let(onSongClick) }
        )

        // 4. Middle-Right: Material You Expressive 10-Sided Sunny Badge
        val sunnySize = minOf(totalWidth * 0.23f, totalHeight * 0.20f)
        FluidBlobItem(
            song = songsToShow.getOrNull(3),
            shape = remember { FluidBlobShape(FluidBlobType.MIDDLE_EXPRESSIVE_SUNNY) },
            width = sunnySize,
            height = sunnySize,
            haloOffsetX = 1.dp,
            haloOffsetY = 2.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.845f - (sunnySize + 14.dp) * 0.5f, y = totalHeight * 0.535f - (sunnySize + 14.dp) * 0.5f),
            onClick = { songsToShow.getOrNull(3)?.let(onSongClick) }
        )

        // 5. Bottom-Left Corner: Rounded Triangle Blob
        val blW = totalWidth * 0.44f
        val blH = totalHeight * 0.36f
        FluidBlobItem(
            song = songsToShow.getOrNull(4),
            shape = remember { FluidBlobShape(FluidBlobType.CORNER_TRIANGLE_BL) },
            width = blW,
            height = blH,
            haloOffsetX = 3.dp,
            haloOffsetY = (-2).dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.245f - (blW + 14.dp) * 0.5f, y = totalHeight * 0.81f - (blH + 14.dp) * 0.5f),
            onClick = { songsToShow.getOrNull(4)?.let(onSongClick) }
        )

        // 6. Bottom-Right (4th Corner): 7-Sided Scalloped Cookie
        val cookieSize = minOf(totalWidth * 0.36f, totalHeight * 0.31f)
        FluidBlobItem(
            song = songsToShow.getOrNull(5),
            shape = remember { FluidBlobShape(FluidBlobType.CORNER_COOKIE_BR) },
            width = cookieSize,
            height = cookieSize,
            haloOffsetX = 2.dp,
            haloOffsetY = 2.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.765f - (cookieSize + 14.dp) * 0.5f, y = totalHeight * 0.825f - (cookieSize + 14.dp) * 0.5f),
            onClick = { songsToShow.getOrNull(5)?.let(onSongClick) }
        )

        // Floating Dreamy Accent Orbs on top of shape edges
        FloatingOrb(
            size = 26.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.43f, y = totalHeight * 0.19f)
        )
        FloatingOrb(
            size = 20.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.11f, y = totalHeight * 0.37f)
        )
        FloatingOrb(
            size = 22.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.11f, y = totalHeight * 0.61f)
        )
        FloatingOrb(
            size = 20.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.86f, y = totalHeight * 0.88f)
        )
    }
}

@Composable
private fun FluidBlobItem(
    song: Song?,
    shape: Shape,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    haloOffsetX: Dp = 0.dp,
    haloOffsetY: Dp = 0.dp,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val haloColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
    } else {
        Color(0xFFE5D4C6).copy(alpha = 0.88f)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        // Organic Halo Layer
        Box(
            modifier = Modifier
                .offset(x = haloOffsetX, y = haloOffsetY)
                .size(width + 14.dp, height + 14.dp)
                .graphicsLayer {
                    rotationZ = rotation
                    clip = true
                    this.shape = shape
                }
                .background(haloColor)
        )

        // Inner Artwork
        Box(
            modifier = Modifier
                .size(width, height)
                .graphicsLayer {
                    rotationZ = rotation
                    clip = true
                    this.shape = shape
                }
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
                    onClick = onClick
                )
        ) {
            if (song != null) {
                SmartImage(
                    model = song.albumArtUriString,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    crossfadeDurationMillis = 0,
                    isThumbnail = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun FloatingOrb(
    size: Dp,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val orbBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                Color.Transparent
            )
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFD6C6E8).copy(alpha = 0.9f),
                Color(0xFFF8D2BA).copy(alpha = 0.85f),
                Color(0xFFF3B89E).copy(alpha = 0.4f)
            )
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(orbBrush)
    )
}
