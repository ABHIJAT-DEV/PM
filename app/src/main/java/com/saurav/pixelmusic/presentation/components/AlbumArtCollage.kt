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
            height = if (height < 480.dp) 500.dp else height,
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
 * Fluid Blob-Grid Overlay with biological asymmetry.
 * Displays 6 album covers clipped to organic fluid bezier blobs with soft ambient halos
 * and dreamy floating gradient bubbles tightly fitted together.
 */
@Composable
fun FluidBlobCollage(
    songs: ImmutableList<Song>,
    modifier: Modifier = Modifier,
    height: Dp = 390.dp,
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

        // Floating Dreamy Accent Orbs
        FloatingOrb(
            size = 28.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.48f, y = totalHeight * 0.19f)
        )
        FloatingOrb(
            size = 22.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.13f, y = totalHeight * 0.48f)
        )
        FloatingOrb(
            size = 20.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = totalWidth * 0.88f, y = totalHeight * 0.70f)
        )

        // 1. Top-Left: Vertical Egg / Droplet (Prominent)
        FluidBlobItem(
            song = songsToShow.getOrNull(0),
            shape = remember { FluidBlobShape(FluidBlobType.EGG_DROPLET) },
            width = (totalWidth * 0.47f).coerceIn(160.dp, 188.dp),
            height = (totalHeight * 0.47f).coerceIn(170.dp, 198.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 6.dp, y = 4.dp),
            onClick = { songsToShow.getOrNull(0)?.let(onSongClick) }
        )

        // 2. Top-Right: Horizontal Bean
        FluidBlobItem(
            song = songsToShow.getOrNull(1),
            shape = remember { FluidBlobShape(FluidBlobType.HORIZONTAL_BEAN) },
            width = (totalWidth * 0.46f).coerceIn(150.dp, 178.dp),
            height = (totalHeight * 0.35f).coerceIn(125.dp, 148.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = 8.dp),
            onClick = { songsToShow.getOrNull(1)?.let(onSongClick) }
        )

        // 3. Center: Slanted Oval (Tucked organically into middle)
        FluidBlobItem(
            song = songsToShow.getOrNull(2),
            shape = remember { FluidBlobShape(FluidBlobType.SLANTED_OVAL) },
            rotation = -24f,
            width = (totalWidth * 0.50f).coerceIn(170.dp, 198.dp),
            height = (totalHeight * 0.36f).coerceIn(130.dp, 150.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-10).dp, y = 4.dp),
            onClick = { songsToShow.getOrNull(2)?.let(onSongClick) }
        )

        // 4. Middle-Right: Pebble Circle (Tightly placed beside center oval)
        val circleSize = (totalWidth * 0.24f).coerceIn(80.dp, 94.dp)
        FluidBlobItem(
            song = songsToShow.getOrNull(3),
            shape = remember { FluidBlobShape(FluidBlobType.PEBBLE_CIRCLE) },
            width = circleSize,
            height = circleSize,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-8).dp, y = 30.dp),
            onClick = { songsToShow.getOrNull(3)?.let(onSongClick) }
        )

        // 5. Bottom-Left: Bulb Teardrop
        FluidBlobItem(
            song = songsToShow.getOrNull(4),
            shape = remember { FluidBlobShape(FluidBlobType.BULB_TEARDROP) },
            width = (totalWidth * 0.47f).coerceIn(160.dp, 188.dp),
            height = (totalHeight * 0.39f).coerceIn(140.dp, 162.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 8.dp, y = (-4).dp),
            onClick = { songsToShow.getOrNull(4)?.let(onSongClick) }
        )

        // 6. Bottom-Right: Amoeba Wave
        FluidBlobItem(
            song = songsToShow.getOrNull(5),
            shape = remember { FluidBlobShape(FluidBlobType.AMOEBA_WAVE) },
            width = (totalWidth * 0.46f).coerceIn(155.dp, 182.dp),
            height = (totalHeight * 0.37f).coerceIn(135.dp, 158.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-6).dp, y = (-6).dp),
            onClick = { songsToShow.getOrNull(5)?.let(onSongClick) }
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
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val haloColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
    } else {
        Color(0xFFEADFD4).copy(alpha = 0.85f)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        // Organic Halo Layer
        Box(
            modifier = Modifier
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
