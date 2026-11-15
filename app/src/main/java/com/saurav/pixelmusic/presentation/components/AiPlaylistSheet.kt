@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)

package com.saurav.pixelmusic.presentation.components

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import com.saurav.pixelmusic.R
import com.saurav.pixelmusic.presentation.viewmodel.LastFmTrack
import com.saurav.pixelmusic.ui.theme.GoogleSansRounded
import kotlinx.coroutines.delay
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

@Composable
fun AiPlaylistSheet(
    onDismiss: () -> Unit,
    onGenerateClick: (
        mode: String,
        count: Int,
        timePeriod: String,
        seedTrackName: String,
        seedArtistName: String,
        seedArtistInput: String,
        tagInput: String
    ) -> Unit,
    isGenerating: Boolean,
    isSuccess: Boolean,
    status: String?,
    error: String?,
    onRetry: () -> Unit,
    topTracks: List<LastFmTrack> = emptyList(),
    topArtists: List<String> = emptyList(),
    searchTrackResults: List<LastFmTrack> = emptyList(),
    searchArtistResults: List<String> = emptyList(),
    isSearchingSeeds: Boolean = false,
    isSearchingTopSeeds: Boolean = false,
    onSearchTrack: (String, String) -> Unit = { _, _ -> },
    onSearchArtist: (String) -> Unit = {},
    onLoadTopTracks: () -> Unit = {},
    onLoadTopArtists: () -> Unit = {}
) {
    val heroArtworkUris = rememberAiHeroArtworkUris()

    // Auto-dismiss right as the full-screen success portal animation finishes dissolving
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            delay(1380L)
            onDismiss()
        }
    }

    if (isGenerating || isSuccess) {
        AiPlaylistGeneratingFullScreenPage(
            isSuccess = isSuccess,
            status = status,
            error = error,
            heroArtworkUris = heroArtworkUris,
            onDismiss = onDismiss
        )
    } else {
        val sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        )

        SmoothModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentWindowInsets = { WindowInsets.navigationBars },
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            },
            shape = AbsoluteSmoothCornerShape(
                cornerRadiusTL = 28.dp,
                smoothnessAsPercentTL = 60,
                cornerRadiusTR = 28.dp,
                smoothnessAsPercentTR = 60
            )
        ) {
            DailyMixLastFmSheetContent(
                error = error,
                heroArtworkUris = heroArtworkUris,
                onDismiss = onDismiss,
                onRetry = onRetry,
                onGenerateClick = onGenerateClick,
                topTracks = topTracks,
                topArtists = topArtists,
                searchTrackResults = searchTrackResults,
                searchArtistResults = searchArtistResults,
                isSearchingSeeds = isSearchingSeeds,
                isSearchingTopSeeds = isSearchingTopSeeds,
                onSearchTrack = onSearchTrack,
                onSearchArtist = onSearchArtist,
                onLoadTopTracks = onLoadTopTracks,
                onLoadTopArtists = onLoadTopArtists
            )
        }
    }
}

private data class DailyMixModeItem(
    val id: String,
    val label: String,
    @DrawableRes val iconRes: Int,
    val baseShape: AiExpressiveShapeType,
    val morphedShape: AiExpressiveShapeType
)

@Composable
private fun DailyMixLastFmSheetContent(
    error: String?,
    heroArtworkUris: List<String>,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onGenerateClick: (
        mode: String,
        count: Int,
        timePeriod: String,
        seedTrackName: String,
        seedArtistName: String,
        seedArtistInput: String,
        tagInput: String
    ) -> Unit,
    topTracks: List<LastFmTrack>,
    topArtists: List<String>,
    searchTrackResults: List<LastFmTrack>,
    searchArtistResults: List<String>,
    isSearchingSeeds: Boolean,
    isSearchingTopSeeds: Boolean,
    onSearchTrack: (String, String) -> Unit,
    onSearchArtist: (String) -> Unit,
    onLoadTopTracks: () -> Unit,
    onLoadTopArtists: () -> Unit
) {
    var selectedMode by rememberSaveable { mutableStateOf("recommendations") }
    var trackCountText by rememberSaveable { mutableStateOf("30") }
    var seedTrackName by rememberSaveable { mutableStateOf("") }
    var seedArtistName by rememberSaveable { mutableStateOf("") }
    var seedArtistInput by rememberSaveable { mutableStateOf("") }
    var tagInput by rememberSaveable { mutableStateOf("") }
    var timePeriod by rememberSaveable { mutableStateOf("overall") }
    var isRefining by rememberSaveable { mutableStateOf(false) }

    // Ambient, mode-reactive background matching the AI playlist sheet
    val ambientScheme = MaterialTheme.colorScheme
    val ambientDark = ambientScheme.surface.luminance() < 0.5f
    val ambientPick = remember(selectedMode) { (selectedMode.hashCode() and 0x7fffffff) % 3 }
    val ambientBase = listOf(
        if (ambientDark) ambientScheme.primary else ambientScheme.primaryContainer,
        if (ambientDark) ambientScheme.tertiary else ambientScheme.tertiaryContainer,
        if (ambientDark) ambientScheme.secondary else ambientScheme.secondaryContainer
    )
    val ambientA by animateColorAsState(targetValue = ambientBase[ambientPick], animationSpec = tween(800), label = "dm_ambient_a")
    val ambientB by animateColorAsState(targetValue = ambientBase[(ambientPick + 1) % 3], animationSpec = tween(800), label = "dm_ambient_b")
    val ambientC by animateColorAsState(targetValue = ambientBase[(ambientPick + 2) % 3], animationSpec = tween(800), label = "dm_ambient_c")
    val ambientTransition = rememberInfiniteTransition(label = "dm_sheet_ambient")
    val ambientPhaseState = ambientTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dm_ambient_phase"
    )
    val ambientEnergy by animateFloatAsState(
        targetValue = 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "dm_ambient_energy"
    )

    // Hero shapes morph & spin smoothly whenever mode, period, or count changes
    var heroFrom by remember { mutableIntStateOf(0) }
    var heroTo by remember { mutableIntStateOf(0) }
    val heroMorph = remember { Animatable(1f) }
    val stateSignature = remember(selectedMode, timePeriod, trackCountText) {
        ((selectedMode.hashCode() * 31 + timePeriod.hashCode() * 7 + trackCountText.hashCode()) and 0x7fffffff) % 11 + 1
    }
    LaunchedEffect(stateSignature) {
        if (stateSignature != heroTo) {
            heroFrom = heroTo
            heroTo = stateSignature
            heroMorph.snapTo(0f)
            heroMorph.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 820, easing = AiSmoothMorphEasing)
            )
        }
    }
    val reactionKick = remember { Animatable(0f) }
    LaunchedEffect(stateSignature) {
        reactionKick.animateTo(
            targetValue = 0.40f,
            animationSpec = tween(durationMillis = 200, easing = AiSmoothMorphEasing)
        )
        reactionKick.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 620, easing = AiSmoothMorphEasing)
        )
    }

    // Lock the bottom sheet dialog window against IME resize/pan jumps
    val view = LocalView.current
    DisposableEffect(view) {
        var p = view.parent
        while (p != null) {
            if (p is DialogWindowProvider) {
                p.window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                break
            }
            p = p.parent
        }
        val parentView = view.parent as? android.view.View
        if (parentView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(parentView) { _, insets ->
                WindowInsetsCompat.Builder(insets)
                    .setInsets(WindowInsetsCompat.Type.ime(), Insets.NONE)
                    .build()
            }
            ViewCompat.setWindowInsetsAnimationCallback(
                parentView,
                object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                    override fun onProgress(
                        insets: WindowInsetsCompat,
                        runningAnimations: MutableList<WindowInsetsAnimationCompat>
                    ): WindowInsetsCompat = insets
                }
            )
        }
        onDispose {
            if (parentView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(parentView, null)
                ViewCompat.setWindowInsetsAnimationCallback(parentView, null)
            }
        }
    }

    BackHandler(enabled = isRefining) {
        isRefining = false
    }

    val refineProgress by animateFloatAsState(
        targetValue = if (isRefining) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = AiSmoothMorphEasing),
        label = "dm_sheet_refine_progress"
    )
    val showMainPage = !isRefining || refineProgress < 0.999f
    val showRefinePage = isRefining || refineProgress > 0.001f
    val mainScrollState = rememberScrollState()
    var refineReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(500L)
        refineReady = true
    }

    val isInputReady = when (selectedMode) {
        "similar-tracks" -> seedTrackName.isNotBlank() && seedArtistName.isNotBlank()
        "similar-artists" -> seedArtistInput.isNotBlank()
        "tag" -> tagInput.isNotBlank()
        else -> true
    }

    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithCache {
                val brushA = Brush.radialGradient(listOf(ambientA.copy(alpha = 1f), Color.Transparent), Offset.Zero, 1f)
                val brushB = Brush.radialGradient(listOf(ambientB.copy(alpha = 1f), Color.Transparent), Offset.Zero, 1f)
                val brushC = Brush.radialGradient(listOf(ambientC.copy(alpha = 1f), Color.Transparent), Offset.Zero, 1f)
                onDrawBehind {
                    val t = ambientPhaseState.value
                    val e = ambientEnergy
                    val k = reactionKick.value.coerceAtLeast(0f)
                    val w = size.width
                    val h = size.height
                    fun blob(brush: Brush, cx: Float, cy: Float, radius: Float, alpha: Float) {
                        withTransform({
                            translate(cx, cy)
                            scale(radius, radius, Offset.Zero)
                        }) {
                            drawCircle(
                                brush = brush,
                                radius = 1f,
                                center = Offset.Zero,
                                alpha = alpha.coerceIn(0f, 1f)
                            )
                        }
                    }
                    blob(
                        brushA,
                        w * (0.15f + 0.10f * kotlin.math.cos(t)),
                        h * (0.16f + 0.05f * kotlin.math.sin(t)),
                        w * (0.75f + 0.18f * k),
                        0.30f * e + 0.18f * k
                    )
                    blob(
                        brushB,
                        w * (0.88f + 0.08f * kotlin.math.sin(t * 1f + 1.7f)),
                        h * (0.42f + 0.07f * kotlin.math.cos(t + 2.3f)),
                        w * (0.65f + 0.15f * k),
                        0.24f * e + 0.14f * k
                    )
                    blob(
                        brushC,
                        w * (0.45f + 0.14f * kotlin.math.cos(t + 4.1f)),
                        h * (0.88f + 0.05f * kotlin.math.sin(t + 0.6f)),
                        w * (0.80f + 0.12f * k),
                        0.22f * e + 0.12f * k
                    )
                }
            },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val p = refineProgress.coerceIn(0f, 1f)
                        alpha = 1f - p
                        val s = 1f - 0.035f * p
                        scaleX = s
                        scaleY = s
                        translationX = -18.dp.toPx() * p
                    }
                    .verticalScroll(mainScrollState)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            val kick = reactionKick.value.coerceAtLeast(0f)
                            val s = 1f + 0.035f * kick
                            scaleX = s
                            scaleY = s
                            translationY = -3.dp.toPx() * kick
                        }
                ) {
                    AiLibraryIndicatorsAnimation(
                        isGenerating = false,
                        isSuccess = false,
                        heroArtworkUris = heroArtworkUris,
                        morphFrom = heroFrom,
                        morphTo = heroTo,
                        morphProgress = { heroMorph.value }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                DailyMixLastFmMainView(
                    selectedMode = selectedMode,
                    onModeChange = { selectedMode = it },
                    trackCountText = trackCountText,
                    timePeriod = timePeriod,
                    seedTrackName = seedTrackName,
                    onSeedTrackNameChange = { seedTrackName = it },
                    seedArtistName = seedArtistName,
                    onSeedArtistNameChange = { seedArtistName = it },
                    seedArtistInput = seedArtistInput,
                    onSeedArtistInputChange = { seedArtistInput = it },
                    tagInput = tagInput,
                    onTagInputChange = { tagInput = it },
                    isInputReady = isInputReady,
                    error = error,
                    onRetry = onRetry,
                    onOpenRefine = { isRefining = true },
                    onGenerate = {
                        val count = (trackCountText.toIntOrNull() ?: 30).coerceIn(5, 100)
                        onGenerateClick(
                            selectedMode,
                            count,
                            timePeriod,
                            seedTrackName,
                            seedArtistName,
                            seedArtistInput,
                            tagInput
                        )
                    },
                    topTracks = topTracks,
                    topArtists = topArtists,
                    searchTrackResults = searchTrackResults,
                    searchArtistResults = searchArtistResults,
                    isSearchingSeeds = isSearchingSeeds,
                    isSearchingTopSeeds = isSearchingTopSeeds,
                    onSearchTrack = onSearchTrack,
                    onSearchArtist = onSearchArtist,
                    onLoadTopTracks = onLoadTopTracks,
                    onLoadTopArtists = onLoadTopArtists
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val p = refineProgress.coerceIn(0f, 1f)
                        alpha = p
                        val s = 0.965f + 0.035f * p
                        scaleX = s
                        scaleY = s
                        translationX = 18.dp.toPx() * (1f - p)
                    }
            ) {
                if (refineReady || isRefining) {
                    DailyMixLastFmRefineView(
                        selectedMode = selectedMode,
                        trackCountText = trackCountText,
                        onTrackCountChange = { trackCountText = it },
                        timePeriod = timePeriod,
                        onTimePeriodChange = { timePeriod = it },
                        onBack = { isRefining = false },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    ) { measurables, constraints ->
        val looseConstraints = constraints.copy(minHeight = 0)
        val mainPlaceable = measurables[0].measure(looseConstraints)
        val refinePlaceable = measurables[1].measure(
            looseConstraints.copy(
                minWidth = mainPlaceable.width,
                maxWidth = mainPlaceable.width,
                maxHeight = mainPlaceable.height.coerceAtLeast(1)
            )
        )
        layout(mainPlaceable.width, mainPlaceable.height) {
            if (showMainPage) {
                mainPlaceable.placeRelative(0, 0)
            }
            if (showRefinePage) {
                refinePlaceable.placeRelative(0, 0)
            }
        }
    }
}

@Composable
private fun DailyMixLastFmMainView(
    selectedMode: String,
    onModeChange: (String) -> Unit,
    trackCountText: String,
    timePeriod: String,
    seedTrackName: String,
    onSeedTrackNameChange: (String) -> Unit,
    seedArtistName: String,
    onSeedArtistNameChange: (String) -> Unit,
    seedArtistInput: String,
    onSeedArtistInputChange: (String) -> Unit,
    tagInput: String,
    onTagInputChange: (String) -> Unit,
    isInputReady: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onOpenRefine: () -> Unit,
    onGenerate: () -> Unit,
    topTracks: List<LastFmTrack>,
    topArtists: List<String>,
    searchTrackResults: List<LastFmTrack>,
    searchArtistResults: List<String>,
    isSearchingSeeds: Boolean,
    isSearchingTopSeeds: Boolean,
    onSearchTrack: (String, String) -> Unit,
    onSearchArtist: (String) -> Unit,
    onLoadTopTracks: () -> Unit,
    onLoadTopArtists: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    val isDark = colors.surface.luminance() < 0.5f

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = colors.surfaceContainerHigh,
        unfocusedContainerColor = colors.surfaceContainerHigh,
        disabledContainerColor = colors.surfaceContainerHigh,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )
    val smoothFieldShape = remember { AbsoluteSmoothCornerShape(16.dp, 60) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Daily Mix",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            textAlign = TextAlign.Center,
            color = colors.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Based on your Last.fm history",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        DailyMixModeQuickSelectRow(
            selectedMode = selectedMode,
            onModeClick = { mode ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onModeChange(mode)
            }
        )

        // Conditional inputs for modes that require seeds or tags
        AnimatedVisibility(
            visible = selectedMode == "similar-tracks" || selectedMode == "similar-artists" || selectedMode == "tag",
            enter = expandVertically(tween(420, easing = AiSmoothMorphEasing)) + fadeIn(tween(380)),
            exit = shrinkVertically(tween(320, easing = AiSmoothMorphEasing)) + fadeOut(tween(240))
        ) {
            Surface(
                shape = remember { AbsoluteSmoothCornerShape(22.dp, 60) },
                color = colors.surfaceContainer,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedMode) {
                        "similar-tracks" -> {
                            OutlinedTextField(
                                value = seedTrackName,
                                onValueChange = onSeedTrackNameChange,
                                label = { Text("Seed Track Name") },
                                placeholder = { Text("e.g. Blinding Lights") },
                                trailingIcon = {
                                    IconButton(onClick = { onSearchTrack(seedTrackName, seedArtistName) }) {
                                        Icon(Icons.Rounded.Search, contentDescription = "Search", tint = colors.primary)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = smoothFieldShape,
                                colors = fieldColors,
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = seedArtistName,
                                onValueChange = onSeedArtistNameChange,
                                label = { Text("Seed Artist Name") },
                                placeholder = { Text("e.g. The Weeknd") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = smoothFieldShape,
                                colors = fieldColors,
                                singleLine = true
                            )
                            Button(
                                onClick = onLoadTopTracks,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primaryContainer,
                                    contentColor = colors.onPrimaryContainer
                                ),
                                shape = AbsoluteSmoothCornerShape(14.dp, 60),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.rounded_library_music_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pick From My Top Tracks", fontFamily = GoogleSansRounded, fontWeight = FontWeight.Bold)
                            }
                            if (isSearchingSeeds || isSearchingTopSeeds) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(22.dp))
                                }
                            }
                            if (searchTrackResults.isNotEmpty()) {
                                Text("Search Results", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = colors.primary)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    for (track in searchTrackResults.take(5)) {
                                        SeedSearchResultItem(
                                            title = track.name,
                                            subtitle = track.artist,
                                            onClick = {
                                                onSeedTrackNameChange(track.name)
                                                onSeedArtistNameChange(track.artist)
                                            }
                                        )
                                    }
                                }
                            }
                            if (topTracks.isNotEmpty()) {
                                Text("My Top Tracks", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = colors.primary)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    for (track in topTracks.take(5)) {
                                        SeedSearchResultItem(
                                            title = track.name,
                                            subtitle = track.artist,
                                            onClick = {
                                                onSeedTrackNameChange(track.name)
                                                onSeedArtistNameChange(track.artist)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        "similar-artists" -> {
                            OutlinedTextField(
                                value = seedArtistInput,
                                onValueChange = onSeedArtistInputChange,
                                label = { Text("Seed Artist Name") },
                                placeholder = { Text("e.g. Coldplay") },
                                trailingIcon = {
                                    IconButton(onClick = { onSearchArtist(seedArtistInput) }) {
                                        Icon(Icons.Rounded.Search, contentDescription = "Search", tint = colors.primary)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = smoothFieldShape,
                                colors = fieldColors,
                                singleLine = true
                            )
                            Button(
                                onClick = onLoadTopArtists,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primaryContainer,
                                    contentColor = colors.onPrimaryContainer
                                ),
                                shape = AbsoluteSmoothCornerShape(14.dp, 60),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.rounded_people_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pick From My Top Artists", fontFamily = GoogleSansRounded, fontWeight = FontWeight.Bold)
                            }
                            if (isSearchingSeeds || isSearchingTopSeeds) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(22.dp))
                                }
                            }
                            if (searchArtistResults.isNotEmpty()) {
                                Text("Search Results", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = colors.primary)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    for (artist in searchArtistResults.take(5)) {
                                        SeedSearchResultItem(
                                            title = artist,
                                            subtitle = "Artist",
                                            onClick = { onSeedArtistInputChange(artist) }
                                        )
                                    }
                                }
                            }
                            if (topArtists.isNotEmpty()) {
                                Text("My Top Artists", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = colors.primary)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    for (artist in topArtists.take(5)) {
                                        SeedSearchResultItem(
                                            title = artist,
                                            subtitle = "Artist",
                                            onClick = { onSeedArtistInputChange(artist) }
                                        )
                                    }
                                }
                            }
                        }
                        "tag" -> {
                            OutlinedTextField(
                                value = tagInput,
                                onValueChange = onTagInputChange,
                                label = { Text("Genre or Tag") },
                                placeholder = { Text("e.g. rock, lofi, jazz") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = smoothFieldShape,
                                colors = fieldColors,
                                singleLine = true
                            )
                            val quickTags = remember { listOf("lofi", "rock", "pop", "jazz", "indie", "ambient") }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                quickTags.forEach { tag ->
                                    ExpressiveChoiceChip(
                                        label = tag,
                                        selected = tagInput.equals(tag, ignoreCase = true),
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onTagInputChange(tag)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = colors.errorContainer,
                shape = AbsoluteSmoothCornerShape(18.dp, 60),
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = error,
                        color = colors.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(painter = painterResource(R.drawable.rounded_restore_24), contentDescription = null, tint = colors.error, modifier = Modifier.size(16.dp))
                        Text("Tap to Retry", color = colors.error, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Refine Section Card matching AI Playlist Sheet
        val refineCardShape = remember { AbsoluteSmoothCornerShape(22.dp, 60) }
        val displayCount = trackCountText.ifBlank { "30" }
        val periodLabel = when (timePeriod) {
            "overall" -> "All Time"
            "12month" -> "Yearly"
            "6month" -> "6 Months"
            "3month" -> "3 Months"
            "1month" -> "Monthly"
            "7day" -> "Weekly"
            else -> "All Time"
        }
        Surface(
            shape = refineCardShape,
            color = colors.surfaceContainer,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(refineCardShape)
                .clickable(onClick = onOpenRefine)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = colors.primaryContainer,
                    shape = AbsoluteSmoothCornerShape(12.dp, 60)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_tune_24),
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Refine your mix",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RefineBadge(text = "Tracks: $displayCount")
                        if (selectedMode == "top" || selectedMode == "library") {
                            RefineBadge(text = "Period: $periodLabel")
                        }
                    }
                }

                Icon(
                    painter = painterResource(R.drawable.rounded_chevron_right_24),
                    contentDescription = "Refine options",
                    tint = colors.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Generate Mix Expressive Action Button
        val btnInteraction = remember { MutableInteractionSource() }
        val isBtnPressed by btnInteraction.collectIsPressedAsState()
        val btnScale by animateFloatAsState(
            targetValue = if (isBtnPressed) 0.94f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "dm_gen_scale"
        )
        val btnCorner by animateDpAsState(
            targetValue = if (isBtnPressed) 18.dp else 28.dp,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "dm_gen_corner"
        )
        val btnContainer by animateColorAsState(
            targetValue = if (isInputReady) colors.primary else colors.surfaceContainerHighest,
            animationSpec = tween(220),
            label = "dm_gen_container"
        )
        val btnContent by animateColorAsState(
            targetValue = if (isInputReady) colors.onPrimary else colors.onSurfaceVariant.copy(alpha = 0.45f),
            animationSpec = tween(220),
            label = "dm_gen_content"
        )

        Surface(
            onClick = {
                if (isInputReady) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onGenerate()
                }
            },
            enabled = isInputReady,
            interactionSource = btnInteraction,
            shape = AbsoluteSmoothCornerShape(btnCorner, 60),
            color = btnContainer,
            contentColor = btnContent,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(56.dp)
                .graphicsLayer {
                    scaleX = btnScale
                    scaleY = btnScale
                }
                .aiSoftSurfaceGlow(
                    color = if (isInputReady) colors.primary else Color.Transparent,
                    cornerRadius = btnCorner,
                    spread = 14.dp,
                    offsetY = 4.dp,
                    maxAlpha = if (isInputReady) (if (isDark) 0.34f else 0.22f) else 0f
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_instant_mix_24),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Generate Mix",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DailyMixLastFmRefineView(
    selectedMode: String,
    trackCountText: String,
    onTrackCountChange: (String) -> Unit,
    timePeriod: String,
    onTimePeriodChange: (String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = colors.surfaceContainerHigh,
                    contentColor = colors.onSurface
                )
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Refine your mix",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            FilledTonalIconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = colors.surfaceContainerHigh,
                    contentColor = colors.onSurface
                )
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Playlist size card
        Surface(
            shape = remember { AbsoluteSmoothCornerShape(22.dp, 60) },
            color = colors.surfaceContainer,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Playlist size",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("15", "25", "30", "40", "50").forEach { countOption ->
                        ExpressiveChoiceChip(
                            label = "$countOption",
                            selected = trackCountText == countOption,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTrackCountChange(countOption)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = trackCountText,
                    onValueChange = { onTrackCountChange(it.filter { ch -> ch.isDigit() }.take(3)) },
                    label = { Text("Custom song count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = remember { AbsoluteSmoothCornerShape(14.dp, 60) }
                )
            }
        }

        if (selectedMode == "top" || selectedMode == "library") {
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = remember { AbsoluteSmoothCornerShape(22.dp, 60) },
                color = colors.surfaceContainer,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Time period",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val periods = listOf(
                        "overall" to "All Time",
                        "12month" to "Yearly",
                        "6month" to "6 Months",
                        "3month" to "3 Months",
                        "1month" to "Monthly",
                        "7day" to "Weekly"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periods.forEach { (pId, pLabel) ->
                            ExpressiveChoiceChip(
                                label = pLabel,
                                selected = timePeriod == pId,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTimePeriodChange(pId)
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val doneInteractionSource = remember { MutableInteractionSource() }
        val isDonePressed by doneInteractionSource.collectIsPressedAsState()
        val doneScale by animateFloatAsState(
            targetValue = if (isDonePressed) 0.95f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "dm_done_scale"
        )
        val doneCorner by animateDpAsState(
            targetValue = if (isDonePressed) 16.dp else 26.dp,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "dm_done_corner"
        )

        Surface(
            onClick = onBack,
            interactionSource = doneInteractionSource,
            shape = AbsoluteSmoothCornerShape(doneCorner, 60),
            color = colors.primary,
            contentColor = colors.onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .graphicsLayer {
                    scaleX = doneScale
                    scaleY = doneScale
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DailyMixModeQuickSelectRow(
    selectedMode: String,
    onModeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = remember {
        listOf(
            DailyMixModeItem("recommendations", "My Recommendations", R.drawable.rounded_auto_awesome_24, AiExpressiveShapeType.COOKIE, AiExpressiveShapeType.FOUR_HEAD_COOKIE),
            DailyMixModeItem("recent", "Recent Tracks", R.drawable.rounded_history_24, AiExpressiveShapeType.FOUR_HEAD_COOKIE, AiExpressiveShapeType.CIRCLE),
            DailyMixModeItem("top", "Top Tracks", R.drawable.rounded_leaderboard_24, AiExpressiveShapeType.ROUNDED_TRIANGLE, AiExpressiveShapeType.SIX_COOKIE),
            DailyMixModeItem("library", "My Library", R.drawable.rounded_library_music_24, AiExpressiveShapeType.CIRCLE, AiExpressiveShapeType.FOUR_HEAD_COOKIE),
            DailyMixModeItem("similar-tracks", "Similar Tracks", R.drawable.rounded_music_note_24, AiExpressiveShapeType.ROUNDED_SQUARE, AiExpressiveShapeType.COOKIE),
            DailyMixModeItem("similar-artists", "Similar Artists", R.drawable.rounded_people_24, AiExpressiveShapeType.SIX_COOKIE, AiExpressiveShapeType.ROUNDED_TRIANGLE),
            DailyMixModeItem("tag", "By Tag / Genre", R.drawable.rounded_sell_24, AiExpressiveShapeType.SUNNY_COOKIE, AiExpressiveShapeType.FOUR_HEAD_COOKIE),
            DailyMixModeItem("mix", "My Mix", R.drawable.rounded_shuffle_24, AiExpressiveShapeType.COOKIE, AiExpressiveShapeType.ROUNDED_SQUARE)
        )
    }

    val scrollState = rememberScrollState()
    val sharedBadgeSpinState = rememberInfiniteTransition(label = "dm_shared_pill_badge_spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dm_shared_pill_badge_spin_value"
    )
    val rows = remember(modes) {
        List(2) { rowIdx ->
            modes.mapIndexedNotNull { idx, item ->
                if (idx % 2 == rowIdx) idx to item else null
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { (index, mode) ->
                    DailyMixModePill(
                        mode = mode,
                        index = index,
                        isSelected = selectedMode == mode.id,
                        badgeSpinProvider = { sharedBadgeSpinState.value },
                        onClick = { onModeClick(mode.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyMixModePill(
    mode: DailyMixModeItem,
    index: Int,
    isSelected: Boolean,
    badgeSpinProvider: () -> Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "dm_pill_scale"
    )
    val cornerTL by animateDpAsState(
        targetValue = if (isPressed || isSelected) (if (index % 2 == 0) 14.dp else 28.dp) else 28.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "dm_pill_ctl"
    )
    val cornerBR by animateDpAsState(
        targetValue = if (isPressed || isSelected) (if (index % 2 == 0) 14.dp else 28.dp) else 28.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "dm_pill_cbr"
    )
    val cornerTR by animateDpAsState(
        targetValue = if (isPressed || isSelected) (if (index % 2 == 0) 28.dp else 14.dp) else 28.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "dm_pill_ctr"
    )
    val cornerBL by animateDpAsState(
        targetValue = if (isPressed || isSelected) (if (index % 2 == 0) 28.dp else 14.dp) else 28.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "dm_pill_cbl"
    )
    val pillShape = remember(cornerTL, cornerTR, cornerBL, cornerBR) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTL = cornerTL,
            smoothnessAsPercentTL = 60,
            cornerRadiusTR = cornerTR,
            smoothnessAsPercentTR = 60,
            cornerRadiusBL = cornerBL,
            smoothnessAsPercentBL = 60,
            cornerRadiusBR = cornerBR,
            smoothnessAsPercentBR = 60
        )
    }

    val morphFraction by animateFloatAsState(
        targetValue = if (isPressed || isSelected) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "dm_icon_morph"
    )

    val isHighlighted = isPressed || isSelected
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val restingPillShadowColor = remember(primaryColor, isDark) {
        if (isDark) lerp(Color.Black, primaryColor, 0.42f) else lerp(Color.Black, primaryColor, 0.58f)
    }
    val clampedMorph = morphFraction.coerceIn(0f, 1f)
    val pillGlowColor = lerp(restingPillShadowColor, primaryColor, clampedMorph)
    val pillGlowSpread = 5.dp + 9.dp * clampedMorph
    val pillGlowOffsetY = 2.dp + 2.5.dp * clampedMorph
    val basePillGlowAlpha = if (isDark) 0.20f else 0.11f
    val activePillGlowAlpha = if (isDark) 0.36f else 0.25f
    val pillGlowMaxAlpha = basePillGlowAlpha + (activePillGlowAlpha - basePillGlowAlpha) * clampedMorph

    val containerColor by animateColorAsState(
        targetValue = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(180),
        label = "dm_pill_color"
    )
    val badgeColor by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.primary
        } else {
            when (index % 3) {
                0 -> MaterialTheme.colorScheme.primaryContainer
                1 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        },
        animationSpec = tween(180),
        label = "dm_badge_color"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            when (index % 3) {
                0 -> MaterialTheme.colorScheme.onPrimaryContainer
                1 -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            }
        },
        animationSpec = tween(180),
        label = "dm_icon_tint"
    )

    val pillTilt by animateFloatAsState(
        targetValue = if (isPressed) (if (index % 2 == 0) -2.5f else 2.5f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "dm_pill_tilt"
    )
    val spinDir = if (index % 2 == 0) 1f else -1f
    val pillGradientAlpha = (0.20f + 0.35f * morphFraction).coerceIn(0f, 1f)
    val badgeHighlight = lerp(badgeColor, Color.White, 0.30f)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = pillShape,
        color = containerColor,
        shadowElevation = 0.dp,
        modifier = modifier
            .height(48.dp)
            .wrapContentWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = pillTilt
            }
            .aiSoftSurfaceGlow(
                color = pillGlowColor,
                cornerRadius = 24.dp,
                spread = pillGlowSpread,
                offsetY = pillGlowOffsetY,
                maxAlpha = pillGlowMaxAlpha
            )
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .wrapContentWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(badgeColor.copy(alpha = pillGradientAlpha), Color.Transparent)
                    )
                )
                .padding(start = 7.dp, end = 16.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .graphicsLayer {
                        rotationZ = badgeSpinProvider() * spinDir + morphFraction * (if (index % 2 == 0) 25f else -25f)
                    }
                    .drawAiMorphShape(
                        from = mode.baseShape,
                        to = mode.morphedShape,
                        morphProgress = { morphFraction },
                        brush = Brush.linearGradient(listOf(badgeHighlight, badgeColor))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(mode.iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            rotationZ = -(badgeSpinProvider() * spinDir + morphFraction * (if (index % 2 == 0) 25f else -25f))
                        }
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = mode.label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = if (isHighlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun SeedSearchResultItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AbsoluteSmoothCornerShape(14.dp, 60),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_music_note_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
