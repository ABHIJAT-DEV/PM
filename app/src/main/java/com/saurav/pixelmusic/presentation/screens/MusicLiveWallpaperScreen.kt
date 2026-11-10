package com.saurav.pixelmusic.presentation.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CropPortrait
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.saurav.pixelmusic.data.preferences.MusicLiveWallpaperSettings
import com.saurav.pixelmusic.data.preferences.MusicWallpaperShapePosition
import com.saurav.pixelmusic.data.preferences.MusicWallpaperStyleMode
import com.saurav.pixelmusic.data.service.wallpaper.MusicLiveWallpaperService
import com.saurav.pixelmusic.data.service.wallpaper.MusicLiveWallpaperStateHolder
import com.saurav.pixelmusic.data.service.wallpaper.WallpaperExpressiveShapeKind
import com.saurav.pixelmusic.data.service.wallpaper.WallpaperMorphingComposeShape
import com.saurav.pixelmusic.data.service.wallpaper.WallpaperSmoothMorphEasing
import com.saurav.pixelmusic.data.service.wallpaper.verticalCenterFraction
import com.saurav.pixelmusic.presentation.components.CollapsibleCommonTopBar
import com.saurav.pixelmusic.presentation.components.MiniPlayerHeight
import com.saurav.pixelmusic.presentation.viewmodel.PlayerViewModel
import com.saurav.pixelmusic.presentation.viewmodel.SettingsViewModel
import com.saurav.pixelmusic.ui.modifiers.scrollMotionBlur
import com.saurav.pixelmusic.ui.theme.GoogleSansRounded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class WallpaperPreviewStateMode {
    MUSIC_ART,
    FALLBACK_WALLPAPER
}

@OptIn(UnstableApi::class)
@Composable
fun MusicLiveWallpaperScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    onNavigationIconClick: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val prefsRepo = playerViewModel.userPreferencesRepository
    val wallpaperSettings by prefsRepo.musicLiveWallpaperSettingsFlow
        .collectAsStateWithLifecycle(initialValue = MusicLiveWallpaperSettings())
    val playerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()

    val currentSong = playerState.currentSong
    val previewTitle = currentSong?.title?.takeIf { it.isNotBlank() } ?: "Midnight Horizon"
    val previewArtist = currentSong?.displayArtist?.takeIf { it.isNotBlank() }
        ?: currentSong?.artist?.takeIf { it.isNotBlank() }
        ?: "PixelMusic Expressive"

    // Check if MusicLiveWallpaperService is currently active on the device
    var isLiveWallpaperActive by remember { mutableStateOf(false) }
    val checkWallpaperActive: () -> Unit = remember(context) {
        {
            isLiveWallpaperActive = runCatching {
                val info = WallpaperManager.getInstance(context).wallpaperInfo
                info?.component == ComponentName(context, MusicLiveWallpaperService::class.java)
            }.getOrDefault(false)
        }
    }

    DisposableEffect(lifecycleOwner) {
        checkWallpaperActive()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkWallpaperActive()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Load already-cached album art (zero network downloads!) & fallback gallery bitmap
    var cachedSongBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fallbackBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var softwareBlurredBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(currentSong?.albumArtUriString) {
        val uriStr = currentSong?.albumArtUriString
        if (!uriStr.isNullOrBlank()) {
            val loaded = MusicLiveWallpaperStateHolder.loadCachedAlbumArtBitmap(context, uriStr)
            if (loaded != null) {
                cachedSongBitmap = loaded
            }
        }
    }

    LaunchedEffect(wallpaperSettings.fallbackWallpaperPath, wallpaperSettings.fallbackWallpaperUpdatedAt) {
        fallbackBitmap = if (wallpaperSettings.fallbackWallpaperPath.isNotBlank()) {
            MusicLiveWallpaperStateHolder.loadFallbackWallpaperBitmap(
                context,
                wallpaperSettings.fallbackWallpaperPath,
                wallpaperSettings.fallbackWallpaperUpdatedAt
            )
        } else {
            null
        }
    }

    // Preview mode toggle so user can inspect both Music Art and Fallback Wallpaper crossfades
    var previewStateMode by remember { mutableStateOf(WallpaperPreviewStateMode.MUSIC_ART) }

    val activePreviewBitmap = when (previewStateMode) {
        WallpaperPreviewStateMode.MUSIC_ART -> cachedSongBitmap ?: fallbackBitmap
        WallpaperPreviewStateMode.FALLBACK_WALLPAPER -> fallbackBitmap ?: cachedSongBitmap
    }

    // Pre-compute software blurred bitmap for Android < 12 or smooth fallback
    LaunchedEffect(activePreviewBitmap, wallpaperSettings.blurAmount) {
        val src = activePreviewBitmap
        if (src != null && !src.isRecycled) {
            softwareBlurredBitmap = withContext(Dispatchers.Default) {
                MusicLiveWallpaperStateHolder.getOrCreateBlurredBitmap(
                    source = src,
                    sourceKey = "preview_${src.hashCode()}",
                    blurAmount = wallpaperSettings.blurAmount
                )
            }
        } else {
            softwareBlurredBitmap = null
        }
    }

    // Gallery Picker for Default (Fallback) Wallpaper
    var isSavingFallback by remember { mutableStateOf(false) }
    val handlePickedGalleryUri: (android.net.Uri?) -> Unit = { uri ->
        if (uri != null) {
            isSavingFallback = true
            coroutineScope.launch {
                val savedPath = MusicLiveWallpaperStateHolder.saveFallbackWallpaperFromUri(context, uri)
                isSavingFallback = false
                if (savedPath != null) {
                    prefsRepo.setMusicWallpaperFallback(savedPath, System.currentTimeMillis())
                    Toast.makeText(context, "Default fallback wallpaper updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not load selected image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = handlePickedGalleryUri
    )
    val fallbackGetContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = handlePickedGalleryUri
    )

    val launchGalleryPicker: () -> Unit = {
        runCatching {
            pickVisualMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }.onFailure {
            runCatching { fallbackGetContentLauncher.launch("image/*") }
        }
    }

    // Collapsible Top Bar state
    val lazyListState = rememberLazyListState()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val minTopBarHeight = 64.dp + statusBarHeight
    val maxTopBarHeight = 180.dp
    val minTopBarHeightPx = with(density) { minTopBarHeight.toPx() }
    val maxTopBarHeightPx = with(density) { maxTopBarHeight.toPx() }

    val topBarHeight = remember { Animatable(maxTopBarHeightPx) }
    var collapseFraction by remember { mutableStateOf(0f) }

    LaunchedEffect(topBarHeight.value) {
        collapseFraction = 1f - ((topBarHeight.value - minTopBarHeightPx) / (maxTopBarHeightPx - minTopBarHeightPx)).coerceIn(0f, 1f)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val isScrollingDown = delta < 0
                if (!isScrollingDown && (lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0)) {
                    return Offset.Zero
                }
                val previousHeight = topBarHeight.value
                val newHeight = (previousHeight + delta).coerceIn(minTopBarHeightPx, maxTopBarHeightPx)
                val consumed = newHeight - previousHeight
                if (consumed.roundToInt() != 0) {
                    coroutineScope.launch { topBarHeight.snapTo(newHeight) }
                }
                val canConsumeScroll = !(isScrollingDown && newHeight == minTopBarHeightPx)
                return if (canConsumeScroll) Offset(0f, consumed) else Offset.Zero
            }
        }
    }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val shouldExpand = topBarHeight.value > (minTopBarHeightPx + maxTopBarHeightPx) / 2
            val canExpand = lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0
            val targetValue = if (shouldExpand && canExpand) maxTopBarHeightPx else minTopBarHeightPx
            if (topBarHeight.value != targetValue) {
                coroutineScope.launch {
                    topBarHeight.animateTo(targetValue, spring(stiffness = Spring.StiffnessMedium))
                }
            }
        }
    }

    // AOD-style Material You Shape Morphing + Rotational Spin state for the Preview
    var previousShapeIdx by remember { mutableIntStateOf(wallpaperSettings.shapeIndex) }
    var targetShapeIdx by remember { mutableIntStateOf(wallpaperSettings.shapeIndex) }
    val shapeMorphAnim = remember { Animatable(1f) }
    val tapSpinRotationDeg = remember { Animatable(0f) }

    val animateToShapeIndex: (Int) -> Unit = { newIdx ->
        val normalized = newIdx.mod(WallpaperExpressiveShapeKind.entries.size)
        previousShapeIdx = targetShapeIdx
        targetShapeIdx = normalized
        coroutineScope.launch {
            prefsRepo.setMusicWallpaperShapeIndex(normalized)
        }
        coroutineScope.launch {
            shapeMorphAnim.snapTo(0f)
            launch {
                shapeMorphAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 820, easing = WallpaperSmoothMorphEasing)
                )
            }
            launch {
                tapSpinRotationDeg.animateTo(
                    targetValue = tapSpinRotationDeg.value + 95f,
                    animationSpec = tween(durationMillis = 820, easing = WallpaperSmoothMorphEasing)
                )
            }
        }
    }

    LaunchedEffect(wallpaperSettings.shapeIndex) {
        val externalIdx = wallpaperSettings.shapeIndex.mod(WallpaperExpressiveShapeKind.entries.size)
        if (externalIdx != targetShapeIdx) {
            previousShapeIdx = targetShapeIdx
            targetShapeIdx = externalIdx
            shapeMorphAnim.snapTo(0f)
            launch {
                shapeMorphAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 820, easing = WallpaperSmoothMorphEasing)
                )
            }
            launch {
                tapSpinRotationDeg.animateTo(
                    targetValue = tapSpinRotationDeg.value + 95f,
                    animationSpec = tween(durationMillis = 820, easing = WallpaperSmoothMorphEasing)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize()
    ) {
        val currentTopBarHeightDp = with(density) { topBarHeight.value.toDp() }

        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(top = currentTopBarHeightDp + 8.dp),
            modifier = Modifier
                .fillMaxSize()
                .scrollMotionBlur(
                    lazyListState = lazyListState,
                    enabled = uiState.isUiMotionBlurEnabled
                )
        ) {
            // =========================================================================
            // 1. INTERACTIVE PICTURE-ONLY WALLPAPER PREVIEW
            // =========================================================================
            item(key = "wallpaper_preview_section") {
                SettingsSection(
                    title = "Live Preview",
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Wallpaper,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 18.dp)
                        ) {
                            // Picture-only phone wallpaper preview
                            WallpaperPictureOnlyPreview(
                                settings = wallpaperSettings,
                                previewMode = previewStateMode,
                                activeBitmap = activePreviewBitmap,
                                softwareBlurredBitmap = softwareBlurredBitmap,
                                songTitle = previewTitle,
                                artistName = previewArtist,
                                fromShape = WallpaperExpressiveShapeKind.fromIndex(previousShapeIdx),
                                toShape = WallpaperExpressiveShapeKind.fromIndex(targetShapeIdx),
                                shapeMorphProgress = shapeMorphAnim.value,
                                tapRotationDeg = tapSpinRotationDeg.value,
                                onTapShape = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val nextIdx = WallpaperExpressiveShapeKind.nextIndex(targetShapeIdx)
                                    animateToShapeIndex(nextIdx)
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.62f)
                                    .aspectRatio(9f / 19.5f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Active shape badge & tap hint
                            val activeShapeKind = WallpaperExpressiveShapeKind.fromIndex(targetShapeIdx)
                            AnimatedVisibility(
                                visible = wallpaperSettings.styleMode == MusicWallpaperStyleMode.MATERIAL_YOU_SHAPE &&
                                    previewStateMode == WallpaperPreviewStateMode.MUSIC_ART,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f))
                                        .clickable {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            animateToShapeIndex(WallpaperExpressiveShapeKind.nextIndex(targetShapeIdx))
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.TouchApp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "${activeShapeKind.displayName}  •  Tap shape in preview to morph",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // State switcher to test smooth crossfading between Now Playing Art & Default Fallback Wallpaper
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PreviewStateTab(
                                    label = "Music Playing",
                                    selected = previewStateMode == WallpaperPreviewStateMode.MUSIC_ART,
                                    onClick = { previewStateMode = WallpaperPreviewStateMode.MUSIC_ART },
                                    modifier = Modifier.weight(1f)
                                )
                                PreviewStateTab(
                                    label = "Fallback (No Music)",
                                    selected = previewStateMode == WallpaperPreviewStateMode.FALLBACK_WALLPAPER,
                                    onClick = { previewStateMode = WallpaperPreviewStateMode.FALLBACK_WALLPAPER },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 2. APPLY / ACTIVATE LIVE WALLPAPER CARD
            // =========================================================================
            item(key = "apply_wallpaper_section") {
                SettingsSection(
                    title = "Activation",
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Surface(
                        color = if (isLiveWallpaperActive) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLiveWallpaperActive) Icons.Rounded.CheckCircle else Icons.Outlined.Wallpaper,
                                    contentDescription = null,
                                    tint = if (isLiveWallpaperActive) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    },
                                    modifier = Modifier.size(26.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isLiveWallpaperActive) {
                                            "Music Live Wallpaper is Active"
                                        } else {
                                            "Set as System Live Wallpaper"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isLiveWallpaperActive) {
                                            "All customization changes below apply smoothly in real time on your Home & Lock screen."
                                        } else {
                                            "Apply PixelMusic Live Wallpaper to your Home screen or both Home and Lock screen."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val component = ComponentName(context, MusicLiveWallpaperService::class.java)
                                    val changeIntent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                                        putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    runCatching {
                                        context.startActivity(changeIntent)
                                    }.onFailure {
                                        val chooserIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        runCatching {
                                            context.startActivity(chooserIntent)
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "Live Wallpaper picker is not supported on this device",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Wallpaper,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isLiveWallpaperActive) "Re-open Live Wallpaper Picker" else "Apply Music Live Wallpaper",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 3. DEFAULT / FALLBACK WALLPAPER (FROM GALLERY)
            // =========================================================================
            item(key = "fallback_wallpaper_section") {
                SettingsSection(
                    title = "Default / Fallback Wallpaper",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Thumbnail of selected fallback image
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                        .clickable { launchGalleryPicker() }
                                ) {
                                    val fb = fallbackBitmap
                                    if (fb != null && !fb.isRecycled) {
                                        Image(
                                            bitmap = fb.asImageBitmap(),
                                            contentDescription = "Fallback wallpaper thumbnail",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.PhotoLibrary,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (fallbackBitmap != null) {
                                            "Custom Gallery Wallpaper Selected"
                                        } else {
                                            "Select Default Wallpaper from Gallery"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Shown when no music is playing. Smoothly crossfades to album art when playback starts.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = launchGalleryPicker,
                                    enabled = !isSavingFallback,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSavingFallback) {
                                            "Saving…"
                                        } else if (fallbackBitmap != null) {
                                            "Change from Gallery"
                                        } else {
                                            "Choose from Gallery"
                                        }
                                    )
                                }

                                if (wallpaperSettings.fallbackWallpaperPath.isNotBlank()) {
                                    FilledTonalButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                MusicLiveWallpaperStateHolder.clearFallbackWallpaper(context)
                                                prefsRepo.setMusicWallpaperFallback("", 0L)
                                                fallbackBitmap = null
                                            }
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Clear fallback wallpaper",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 4. WALLPAPER STYLE & MATERIAL YOU SHAPES CUSTOMIZATION
            // =========================================================================
            item(key = "wallpaper_style_section") {
                SettingsSection(
                    title = "Wallpaper Style & Shapes",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Style selector: Full Screen vs Material You Shapes
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Display Style",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Choose between edge-to-edge full screen album art or Material You expressive shapes over a blurred backdrop",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    WallpaperOptionCard(
                                        title = "Full Screen",
                                        subtitle = "Edge-to-edge album cover",
                                        selected = wallpaperSettings.styleMode == MusicWallpaperStyleMode.FULL_SCREEN,
                                        onClick = {
                                            previewStateMode = WallpaperPreviewStateMode.MUSIC_ART
                                            coroutineScope.launch {
                                                prefsRepo.setMusicWallpaperStyleMode(MusicWallpaperStyleMode.FULL_SCREEN)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    WallpaperOptionCard(
                                        title = "Material You Shapes",
                                        subtitle = "Cookies, triangle, square & more",
                                        selected = wallpaperSettings.styleMode == MusicWallpaperStyleMode.MATERIAL_YOU_SHAPE,
                                        onClick = {
                                            previewStateMode = WallpaperPreviewStateMode.MUSIC_ART
                                            coroutineScope.launch {
                                                prefsRepo.setMusicWallpaperStyleMode(MusicWallpaperStyleMode.MATERIAL_YOU_SHAPE)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Material You Shape Options (Position, Shape Picker, Blur Amount, Size)
                        AnimatedVisibility(
                            visible = wallpaperSettings.styleMode == MusicWallpaperStyleMode.MATERIAL_YOU_SHAPE,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Shape Position: Top, Middle, Bottom
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Shape Position",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Place the Material You shape at the top, middle, or bottom of the screen",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            WallpaperPositionChip(
                                                label = "Top",
                                                selected = wallpaperSettings.shapePosition == MusicWallpaperShapePosition.TOP,
                                                onClick = {
                                                    previewStateMode = WallpaperPreviewStateMode.MUSIC_ART
                                                    coroutineScope.launch {
                                                        prefsRepo.setMusicWallpaperShapePosition(MusicWallpaperShapePosition.TOP)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                            WallpaperPositionChip(
                                                label = "Middle",
                                                selected = wallpaperSettings.shapePosition == MusicWallpaperShapePosition.MIDDLE,
                                                onClick = {
                                                    previewStateMode = WallpaperPreviewStateMode.MUSIC_ART
                                                    coroutineScope.launch {
                                                        prefsRepo.setMusicWallpaperShapePosition(MusicWallpaperShapePosition.MIDDLE)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                            WallpaperPositionChip(
                                                label = "Bottom",
                                                selected = wallpaperSettings.shapePosition == MusicWallpaperShapePosition.BOTTOM,
                                                onClick = {
                                                    previewStateMode = WallpaperPreviewStateMode.MUSIC_ART
                                                    coroutineScope.launch {
                                                        prefsRepo.setMusicWallpaperShapePosition(MusicWallpaperShapePosition.BOTTOM)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                // Material You Shape Gallery / Quick Picker
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                            Text(
                                                text = "Material You Shape",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Tap the shape in the preview above or pick a shape below to smoothly morph",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState())
                                                .padding(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            WallpaperExpressiveShapeKind.entries.forEachIndexed { idx, kind ->
                                                val isSelected = targetShapeIdx == idx
                                                ShapeMiniPreviewChip(
                                                    kind = kind,
                                                    selected = isSelected,
                                                    onClick = {
                                                        previewStateMode = WallpaperPreviewStateMode.MUSIC_ART
                                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        animateToShapeIndex(idx)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Background Blur Amount Slider
                                SliderSettingsItem(
                                    label = "Background Blur Amount",
                                    value = wallpaperSettings.blurAmount,
                                    valueRange = 0.0f..1.0f,
                                    steps = 0,
                                    onValueChange = { amount ->
                                        coroutineScope.launch {
                                            prefsRepo.setMusicWallpaperBlurAmount(amount)
                                        }
                                    },
                                    valueText = { "${(it * 100).roundToInt()}%" }
                                )

                                // Shape Size Slider
                                SliderSettingsItem(
                                    label = "Shape Size",
                                    value = wallpaperSettings.shapeScale,
                                    valueRange = 0.60f..1.15f,
                                    steps = 0,
                                    onValueChange = { scale ->
                                        coroutineScope.launch {
                                            prefsRepo.setMusicWallpaperShapeScale(scale)
                                        }
                                    },
                                    valueText = { "${(it * 100).roundToInt()}%" }
                                )

                                SwitchSettingItem(
                                    title = "Tap Wallpaper Shape to Morph",
                                    subtitle = "Single-tap the shape on your Home screen or preview to smoothly change its Material You shape",
                                    checked = wallpaperSettings.tapWallpaperToChangeShape,
                                    onCheckedChange = { enabled ->
                                        coroutineScope.launch {
                                            prefsRepo.setMusicWallpaperTapToChangeShape(enabled)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.TouchApp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                )

                                SwitchSettingItem(
                                    title = "Morph Shape on Track Change",
                                    subtitle = "Smoothly morph and spin to the next Material You shape when a new song starts",
                                    checked = wallpaperSettings.animateShapeOnTrackChange,
                                    onCheckedChange = { enabled ->
                                        coroutineScope.launch {
                                            prefsRepo.setMusicWallpaperAnimateShapeOnTrackChange(enabled)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                )

                                SwitchSettingItem(
                                    title = "Ambient Shape Rotation",
                                    subtitle = "Gently rotate the Material You shape while music is playing (like AOD)",
                                    checked = wallpaperSettings.slowRotateWhilePlaying,
                                    onCheckedChange = { enabled ->
                                        coroutineScope.launch {
                                            prefsRepo.setMusicWallpaperSlowRotateWhilePlaying(enabled)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.SlowMotionVideo,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 5. SONG INFORMATION OVERLAY & SMOOTH CROSSFADE TRANSITIONS
            // =========================================================================
            item(key = "song_info_and_transitions_section") {
                SettingsSection(
                    title = "Overlay & Smooth Transitions",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SwitchSettingItem(
                            title = "Show Song Information",
                            subtitle = "Display the currently playing song title and artist on the top left of the wallpaper",
                            checked = wallpaperSettings.showSongInfo,
                            onCheckedChange = { show ->
                                coroutineScope.launch {
                                    prefsRepo.setMusicWallpaperShowSongInfo(show)
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        )

                        SliderSettingsItem(
                            label = "Song Change Crossfade Duration",
                            value = wallpaperSettings.crossfadeDurationMs / 1000f,
                            valueRange = 0.8f..4.0f,
                            steps = 0,
                            onValueChange = { seconds ->
                                coroutineScope.launch {
                                    prefsRepo.setMusicWallpaperCrossfadeDurationMs((seconds * 1000f).roundToInt())
                                }
                            },
                            valueText = { String.format(java.util.Locale.US, "%.1fs", it) }
                        )

                        SliderSettingsItem(
                            label = "Wallpaper Contrast Dim",
                            value = wallpaperSettings.backgroundDim,
                            valueRange = 0.0f..0.70f,
                            steps = 0,
                            onValueChange = { dim ->
                                coroutineScope.launch {
                                    prefsRepo.setMusicWallpaperBackgroundDim(dim)
                                }
                            },
                            valueText = { "${(it * 100).roundToInt()}%" }
                        )

                        // Data-saving cache reuse info card
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Zero extra data usage: Music Live Wallpaper directly reuses the album art already downloaded for the Now Playing screen and notifications.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(MiniPlayerHeight + 36.dp))
            }
        }

        CollapsibleCommonTopBar(
            title = "Music Live Wallpaper",
            collapseFraction = collapseFraction,
            headerHeight = currentTopBarHeightDp,
            onBackClick = onNavigationIconClick
        )
    }
}

/**
 * Picture-only interactive wallpaper preview.
 * Shows only the wallpaper composition (blurred/full background, Material You expressive shape at
 * Top/Middle/Bottom, and top-left song information when enabled), with single-tap shape morphing.
 */
@Composable
private fun WallpaperPictureOnlyPreview(
    settings: MusicLiveWallpaperSettings,
    previewMode: WallpaperPreviewStateMode,
    activeBitmap: Bitmap?,
    softwareBlurredBitmap: Bitmap?,
    songTitle: String,
    artistName: String,
    fromShape: WallpaperExpressiveShapeKind,
    toShape: WallpaperExpressiveShapeKind,
    shapeMorphProgress: Float,
    tapRotationDeg: Float,
    onTapShape: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFallbackIdleView = previewMode == WallpaperPreviewStateMode.FALLBACK_WALLPAPER
    val targetModeFraction = if (isFallbackIdleView || settings.styleMode == MusicWallpaperStyleMode.FULL_SCREEN) {
        0f
    } else {
        1f
    }

    val modeFraction by animateFloatAsState(
        targetValue = targetModeFraction,
        animationSpec = tween(durationMillis = 720, easing = WallpaperSmoothMorphEasing),
        label = "previewModeFraction"
    )

    val positionCenterFraction by animateFloatAsState(
        targetValue = settings.shapePosition.verticalCenterFraction(),
        animationSpec = tween(durationMillis = 680, easing = WallpaperSmoothMorphEasing),
        label = "previewPositionY"
    )

    val animatedShapeScale by animateFloatAsState(
        targetValue = settings.shapeScale,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "previewShapeScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "previewAmbientSpin")
    val continuousRotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "previewContinuousSpin"
    )

    val effectiveRotationDeg = tapRotationDeg + if (settings.slowRotateWhilePlaying) continuousRotationDeg else 0f
    val quantizedRotDeg = (effectiveRotationDeg * 4f).roundToInt() / 4f
    val quantizedMorph = (shapeMorphProgress * 90f).roundToInt() / 90f

    val morphShape = remember(fromShape, toShape, quantizedMorph, quantizedRotDeg) {
        WallpaperMorphingComposeShape(
            fromShape = fromShape,
            toShape = toShape,
            shapeMorphProgress = quantizedMorph,
            shapeRotationRad = Math.toRadians(quantizedRotDeg.toDouble())
        )
    }

    val haloShape = remember(fromShape, toShape, quantizedMorph, quantizedRotDeg) {
        WallpaperMorphingComposeShape(
            fromShape = fromShape,
            toShape = toShape,
            shapeMorphProgress = quantizedMorph,
            shapeRotationRad = Math.toRadians((quantizedRotDeg + 16f).toDouble())
        )
    }

    val crossfadeDuration = settings.crossfadeDurationMs.coerceIn(800, 4000)

    Surface(
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(26.dp)
            )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val previewW = maxWidth
            val previewH = maxHeight
            val shapeBaseSize = min(previewW.value, previewH.value).dp * 0.72f * animatedShapeScale
            val shapeCenterY = previewH * positionCenterFraction

            // 1. Background Layer (Crossfades smoothly like song crossfade; blurred in Material You Shape mode)
            Crossfade(
                targetState = Pair(activeBitmap, previewMode),
                animationSpec = tween(durationMillis = crossfadeDuration, easing = WallpaperSmoothMorphEasing),
                label = "previewBgCrossfade",
                modifier = Modifier.fillMaxSize()
            ) { (bmp, _) ->
                val effectiveBlur = settings.blurAmount * modeFraction
                if (bmp != null && !bmp.isRecycled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Wallpaper background preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (effectiveBlur > 0.01f) {
                                        Modifier.blur(
                                            radius = (effectiveBlur * 38f).dp,
                                            edgeTreatment = BlurredEdgeTreatment.Unbounded
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    } else {
                        val displayBmp = if (effectiveBlur > 0.02f && softwareBlurredBitmap != null && !softwareBlurredBitmap.isRecycled) {
                            softwareBlurredBitmap
                        } else {
                            bmp
                        }
                        Image(
                            bitmap = displayBmp.asImageBitmap(),
                            contentDescription = "Wallpaper background preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    DefaultExpressivePreviewArt(modifier = Modifier.fillMaxSize(), blurred = effectiveBlur > 0.05f)
                }
            }

            // Contrast dim overlay
            val effectiveDim = lerp(
                settings.backgroundDim,
                (settings.backgroundDim + 0.12f).coerceAtMost(0.75f),
                modeFraction
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = effectiveDim))
            )

            // 2. Material You Expressive Shape Layer (placed at Top, Middle, or Bottom; single-tap morphs shape!)
            if (modeFraction > 0.01f) {
                val curWidth = androidx.compose.ui.unit.lerp(previewW, shapeBaseSize, modeFraction)
                val curHeight = androidx.compose.ui.unit.lerp(previewH, shapeBaseSize, modeFraction)
                val curTopOffset = androidx.compose.ui.unit.lerp(
                    0.dp,
                    (shapeCenterY - shapeBaseSize * 0.5f).coerceIn(0.dp, (previewH - shapeBaseSize).coerceAtLeast(0.dp)),
                    modeFraction
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = curTopOffset)
                        .align(Alignment.TopCenter)
                        .size(width = curWidth, height = curHeight)
                ) {
                    // Subtle expressive backing halo
                    Box(
                        modifier = Modifier
                            .fillMaxSize(1.065f)
                            .graphicsLayer { alpha = modeFraction * 0.22f }
                            .clip(haloShape)
                            .background(Color.White)
                    )

                    // Morphing Material You Shape containing sharp album art
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(if (modeFraction > 0.15f) morphShape else RoundedCornerShape(0.dp))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.22f * modeFraction),
                                shape = if (modeFraction > 0.15f) morphShape else RoundedCornerShape(0.dp)
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onTapShape() }
                                )
                            }
                    ) {
                        Crossfade(
                            targetState = activeBitmap,
                            animationSpec = tween(durationMillis = crossfadeDuration, easing = WallpaperSmoothMorphEasing),
                            label = "previewShapeArtCrossfade",
                            modifier = Modifier.fillMaxSize()
                        ) { bmp ->
                            if (bmp != null && !bmp.isRecycled) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Shape album art preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                DefaultExpressivePreviewArt(modifier = Modifier.fillMaxSize(), blurred = false)
                            }
                        }
                    }
                }
            }

            // 3. Top-Left Song Information Overlay (only when toggle is enabled & in Music Playing state)
            AnimatedVisibility(
                visible = settings.showSongInfo && !isFallbackIdleView,
                enter = fadeIn(tween(420)),
                exit = fadeOut(tween(320)),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.48f),
                                    Color.Black.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 18.dp)
                ) {
                    AnimatedContent(
                        targetState = Pair(songTitle, artistName),
                        transitionSpec = {
                            fadeIn(tween(480)) togetherWith fadeOut(tween(360))
                        },
                        label = "previewSongInfoCrossfade"
                    ) { (title, artist) ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = title,
                                style = TextStyle(
                                    fontFamily = GoogleSansRounded,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.65f),
                                        offset = Offset(0f, 2f),
                                        blurRadius = 6f
                                    )
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = artist,
                                style = TextStyle(
                                    fontFamily = GoogleSansRounded,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.5.sp,
                                    color = Color.White.copy(alpha = 0.84f),
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.55f),
                                        offset = Offset(0f, 1.5f),
                                        blurRadius = 4f
                                    )
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultExpressivePreviewArt(
    modifier: Modifier = Modifier,
    blurred: Boolean
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surfaceContainerHighest

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(primaryContainer, primary, tertiary, surface),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tertiary.copy(alpha = if (blurred) 0.55f else 0.78f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.32f, size.height * 0.35f),
                radius = size.minDimension * 0.65f
            ),
            center = Offset(size.width * 0.32f, size.height * 0.35f),
            radius = size.minDimension * 0.65f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primary.copy(alpha = if (blurred) 0.50f else 0.72f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.72f, size.height * 0.68f),
                radius = size.minDimension * 0.58f
            ),
            center = Offset(size.width * 0.72f, size.height * 0.68f),
            radius = size.minDimension * 0.58f
        )
    }
}

@Composable
private fun ShapeMiniPreviewChip(
    kind: WallpaperExpressiveShapeKind,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val shapeColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Canvas(modifier = Modifier.size(34.dp)) {
                val cx = size.width * 0.5f
                val cy = size.height * 0.5f
                val rBase = size.minDimension * 0.45f
                val count = 96
                val twoPi = 2.0 * PI
                val path = Path().apply {
                    for (i in 0 until count) {
                        val th = (twoPi * i) / count
                        val r = rBase * kind.normalizedRadiusAt(th)
                        val x = cx + r * cos(th).toFloat()
                        val y = cy + r * sin(th).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(path = path, color = shapeColor.copy(alpha = if (selected) 0.28f else 0.16f))
                drawPath(path = path, color = shapeColor, style = Stroke(width = 2.dp.toPx()))
            }

            Text(
                text = kind.shortLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun PreviewStateTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = fg
        )
    }
}

@Composable
private fun WallpaperOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val titleColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val subtitleColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }
    }
}

@Composable
private fun WallpaperPositionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}
