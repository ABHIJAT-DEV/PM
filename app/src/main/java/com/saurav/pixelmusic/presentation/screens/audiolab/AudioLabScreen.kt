package com.saurav.pixelmusic.presentation.screens.audiolab

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurav.pixelmusic.R
import com.saurav.pixelmusic.audiolab.EqPresets
import com.saurav.pixelmusic.audiolab.EqSettings
import com.saurav.pixelmusic.presentation.components.MiniPlayerHeight
import com.saurav.pixelmusic.presentation.navigation.Screen
import com.saurav.pixelmusic.presentation.viewmodel.AudioLabViewModel
import com.saurav.pixelmusic.ui.theme.PixelMusicStatusBarStyle
import kotlin.math.sin

private val HubCardHeight = 116.dp

@Composable
fun AudioLabScreen(
    navController: NavController,
    viewModel: AudioLabViewModel = hiltViewModel(),
) {
    val eq by viewModel.eq.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val reverb by viewModel.reverb.collectAsState()
    val loudness by viewModel.loudness.collectAsState()
    val spatial by viewModel.spatial.collectAsState()
    val playback by viewModel.playback.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val stage by viewModel.stage.collectAsState()
    val effects = activeEffects(reverb, loudness, spatial, playback, volume, stage)

    ListenToAudio()

    val cs = MaterialTheme.colorScheme
    PixelMusicStatusBarStyle(color = cs.background)
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = statusTop + 12.dp, bottom = navBottom + MiniPlayerHeight + 32.dp)
                .padding(horizontal = 16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            ) {
                FilledIconButton(
                    onClick = { navController.navigateUp() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = cs.surfaceContainerLow,
                        contentColor = cs.onSurface,
                    ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_arrow_back_24),
                        contentDescription = stringResource(R.string.lab_back),
                    )
                }
            }

            Text(
                text = "Audio FX",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Shape your sound, spatial stage, and transitions",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(Modifier.height(22.dp))

            val equalizer: @Composable (Modifier) -> Unit = { modifier ->
                ExpressiveHubCard(
                    title = stringResource(R.string.lab_equalizer),
                    status = equalizerStatus(eq, presets.associate { it.id to it.name }),
                    iconRes = R.drawable.rounded_graphic_eq_24,
                    active = eq.enabled,
                    onClick = { navController.navigate(Screen.AudioLabEq.route) },
                    modifier = modifier,
                ) { EqualizerSketch(eq) }
            }
            val effect: @Composable (Modifier) -> Unit = { modifier ->
                ExpressiveHubCard(
                    title = stringResource(R.string.lab_audio_effect),
                    status = effectsStatus(effects),
                    iconRes = R.drawable.rounded_tune_24,
                    active = effects.isNotEmpty(),
                    onClick = { navController.navigate(Screen.AudioLabFx.route) },
                    modifier = modifier,
                ) { FxSketch(active = effects.isNotEmpty()) }
            }
            val stageTile: @Composable (Modifier) -> Unit = { modifier ->
                ExpressiveHubCard(
                    title = stringResource(R.string.stage_card_title),
                    status = if (stage.enabled) stringResource(R.string.stage_on) else stringResource(R.string.lab_status_off),
                    iconRes = R.drawable.rounded_surround_sound_24,
                    active = stage.enabled,
                    onClick = { navController.navigate(Screen.AudioLabStage.route) },
                    modifier = modifier,
                ) { StageMiniSketch(active = stage.enabled) }
            }
            val transition: @Composable (Modifier) -> Unit = { modifier ->
                ExpressiveHubCard(
                    title = stringResource(R.string.lab_transition),
                    status = stringResource(R.string.lab_transition_auto),
                    iconRes = R.drawable.rounded_auto_awesome_motion_24,
                    active = true,
                    onClick = { navController.navigate(Screen.EditTransition.createRoute(null)) },
                    modifier = modifier,
                ) { TransitionSketch(active = true) }
            }

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= 560.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            equalizer(Modifier.weight(1f))
                            effect(Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            stageTile(Modifier.weight(1f))
                            transition(Modifier.weight(1f))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        equalizer(Modifier)
                        effect(Modifier)
                        stageTile(Modifier)
                        transition(Modifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun effectsStatus(effects: List<ActiveEffect>): String {
    val names = effects.map { stringResource(effectLabelRes(it.key)) }
    return when {
        names.isEmpty() -> stringResource(R.string.lab_status_all_off)
        names.size <= 2 -> names.joinToString(" · ")
        else -> stringResource(R.string.lab_effects_and_more, names.take(2).joinToString(", "), names.size - 2)
    }
}

@Composable
private fun equalizerStatus(eq: EqSettings, userPresets: Map<String, String>): String {
    if (!eq.enabled) return stringResource(R.string.lab_status_off)
    val custom = stringResource(R.string.lab_status_custom)
    val curve = eq.presetId?.let { id -> EqPresets.BuiltIn.firstOrNull { it.id == id }?.name ?: userPresets[id] } ?: custom
    val correction = eq.correction?.takeIf { eq.correctionEnabled }?.model
    return if (correction != null) "$curve · $correction" else curve
}

@Composable
private fun ExpressiveHubCard(
    title: String,
    status: String,
    @DrawableRes iconRes: Int,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sketch: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val cs = MaterialTheme.colorScheme

    val cardBg by animateColorAsState(
        targetValue = if (active) cs.surfaceContainerHigh else cs.surfaceContainer,
        animationSpec = tween(260),
        label = "hubCardBg",
    )
    val iconBg by animateColorAsState(
        targetValue = if (active) cs.primaryContainer else cs.secondaryContainer,
        animationSpec = tween(260),
        label = "hubIconBg",
    )
    val iconFg by animateColorAsState(
        targetValue = if (active) cs.onPrimaryContainer else cs.onSecondaryContainer,
        animationSpec = tween(260),
        label = "hubIconFg",
    )
    val statusBg by animateColorAsState(
        targetValue = if (active) cs.primary.copy(alpha = 0.14f) else cs.surfaceContainerHighest,
        animationSpec = tween(260),
        label = "hubStatusBg",
    )
    val statusFg by animateColorAsState(
        targetValue = if (active) cs.primary else cs.onSurfaceVariant,
        animationSpec = tween(260),
        label = "hubStatusFg",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HubCardHeight)
            .pressBounce(interaction, pressedScale = 0.97f)
            .clip(RoundedCornerShape(28.dp))
            .background(cardBg)
            .clickable(interaction, null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .damageBoundary(),
    ) {
        Box(Modifier.matchParentSize()) { sketch() }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconFg,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(0.72f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusFg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun sketchTime(moving: () -> Boolean = { true }): androidx.compose.runtime.State<Float> {
    val onScreen = rememberOnScreen()
    val held = rememberPicturesHeld(onScreen)
    return rememberLabTime(held, moving)
}

@Composable
private fun BoxScope.EqualizerSketch(eq: EqSettings) {
    fun band(from: Double, to: Double): Float {
        if (!eq.enabled) return 0f
        val inBand = eq.nodes.filter { it.enabled && it.frequency >= from && it.frequency < to }
        return if (inBand.isEmpty()) 0f else (inBand.sumOf { it.gain } / inBand.size).toFloat()
    }
    val low by animateFloatAsState(band(0.0, 250.0), spring(dampingRatio = 0.6f, stiffness = 140f), label = "low")
    val mid by animateFloatAsState(band(250.0, 4000.0), spring(dampingRatio = 0.6f, stiffness = 140f), label = "mid")
    val high by animateFloatAsState(band(4000.0, 30_000.0), spring(dampingRatio = 0.6f, stiffness = 140f), label = "high")
    val level = remember { LevelFollower() }
    val time = sketchTime { eq.enabled && com.saurav.pixelmusic.audiolab.audio.AudioTap.isLive }
    val last = remember { floatArrayOf(-1f) }
    val hubCard = MaterialTheme.colorScheme.surfaceContainerHigh
    val sketch = LabColors.Sketch
    val activeKnob = LabColors.Accent
    Canvas(Modifier.fillMaxSize()) {
        val t = time.value
        if (t != last[0]) { level.update(0.016f); last[0] = t }
        val panel = size.height * 0.92f
        val origin = Offset(size.width - panel * 0.72f, size.height * 0.08f)
        rotate(-14f, Offset(origin.x + panel / 2, origin.y + panel / 2)) {
            drawRoundRect(sketch, origin, Size(panel, panel), CornerRadius(panel * 0.22f), style = Stroke(3.dp.toPx()))
            listOf(low, mid, high).forEachIndexed { i, gain ->
                val x = origin.x + panel * (0.25f + i * 0.22f)
                val top = origin.y + panel * 0.16f
                val bottom = origin.y + panel * 0.84f
                drawLine(sketch, Offset(x, top), Offset(x, bottom), 3.5.dp.toPx(), StrokeCap.Round)
                val bob = level.punch * 3.5.dp.toPx() * sin(t * 9f + i * 2f)
                val y = (top + bottom) / 2 - (gain / 12f) * (bottom - top) / 2 + bob
                drawCircle(hubCard, 9.dp.toPx(), Offset(x, y))
                drawCircle(if (eq.enabled) activeKnob else sketch, 9.dp.toPx(), Offset(x, y), style = Stroke(3.5.dp.toPx()))
            }
        }
    }
}

@Composable
private fun BoxScope.FxSketch(active: Boolean) {
    val measurer = rememberTextMeasurer()
    val glow by animateFloatAsState(if (active) 1f else 0f, tween(500), label = "fx")
    val time = sketchTime { active }
    val sketch = LabColors.Sketch
    val accent = LabColors.Accent
    Canvas(Modifier.fillMaxSize()) {
        val style = TextStyle(
            fontFamily = InterFontFamily, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic,
            fontSize = (size.height / density * 0.92f).sp,
            drawStyle = Stroke(width = 1.6.dp.toPx()),
        )
        val layout = measurer.measure("FX", style)
        val topLeft = Offset(size.width - layout.size.width * 0.86f, (size.height - layout.size.height) / 2f + size.height * 0.06f)
        drawText(layout, color = sketch, topLeft = topLeft)
        if (glow > 0.01f) {
            val sweep = (time.value * 0.35f) % 1f
            val x = topLeft.x + layout.size.width * (sweep * 1.6f - 0.3f)
            drawText(
                layout,
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, accent.copy(alpha = 0.85f * glow), Color.Transparent),
                    startX = x - 60.dp.toPx(), endX = x + 60.dp.toPx(),
                ),
                topLeft = topLeft,
            )
        }
    }
}

@Composable
private fun BoxScope.StageMiniSketch(active: Boolean) {
    val on by animateFloatAsState(if (active) 1f else 0.45f, tween(400), label = "stage")
    val sketch = LabColors.Sketch
    val primary = LabColors.Primary
    val accent = LabColors.Accent
    Canvas(Modifier.fillMaxHeight().width(64.dp).align(Alignment.CenterEnd).padding(end = 18.dp, top = 26.dp, bottom = 26.dp)) {
        val w = size.width
        val h = size.height
        val color = (if (active) primary else sketch).copy(alpha = on)
        val stroke = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round)
        drawRect(color, Offset(w * 0.12f, h * 0.25f), Size(w * 0.56f, h * 0.62f), style = stroke)
        drawRect(color.copy(alpha = on * 0.6f), Offset(w * 0.32f, h * 0.1f), Size(w * 0.56f, h * 0.62f), style = stroke)
        drawCircle(if (active) accent else sketch, 3.5.dp.toPx(), Offset(w * 0.5f, h * 0.48f))
    }
}

@Composable
private fun BoxScope.TransitionSketch(active: Boolean) {
    val on by animateFloatAsState(if (active) 1f else 0.4f, tween(400), label = "t")
    val sketch = LabColors.Sketch
    val accent = LabColors.Accent
    Canvas(Modifier.fillMaxHeight().width(64.dp).align(Alignment.CenterEnd).padding(end = 18.dp, top = 28.dp, bottom = 28.dp)) {
        val w = size.width
        val h = size.height
        val out = Path().apply { moveTo(0f, 0f); cubicTo(w * 0.5f, 0f, w * 0.5f, h, w, h) }
        val inn = Path().apply { moveTo(0f, h); cubicTo(w * 0.5f, h, w * 0.5f, 0f, w, 0f) }
        drawPath(out, accent.copy(alpha = 0.55f * on), style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
        drawPath(inn, sketch.copy(alpha = on), style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
    }
}
