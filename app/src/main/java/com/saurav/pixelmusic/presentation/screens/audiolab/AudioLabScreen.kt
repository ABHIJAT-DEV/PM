package com.saurav.pixelmusic.presentation.screens.audiolab

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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurav.pixelmusic.R
import com.saurav.pixelmusic.audiolab.EqPresets
import com.saurav.pixelmusic.audiolab.EqSettings
import com.saurav.pixelmusic.presentation.navigation.Screen
import com.saurav.pixelmusic.presentation.viewmodel.AudioLabViewModel
import kotlin.math.sin

private val HubCard = Color(0xFF222225)
private val Sketch = Color(0xFF6A6A70)

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

    AudioLabPage(
        title = LabTitle("Audio", "Lab"),
        onBack = { navController.navigateUp() },
    ) {
        val equalizer: @Composable (Modifier) -> Unit = { modifier ->
            BigHubCard(
                title = stringResource(R.string.lab_equalizer),
                status = equalizerStatus(eq, presets.associate { it.id to it.name }),
                on = eq.enabled,
                onClick = { navController.navigate(Screen.AudioLabEq.route) },
                modifier = modifier,
            ) { EqualizerSketch(eq) }
        }
        val effect: @Composable (Modifier) -> Unit = { modifier ->
            BigHubCard(
                title = stringResource(R.string.lab_audio_effect),
                status = effectsStatus(effects),
                on = effects.isNotEmpty(),
                onClick = { navController.navigate(Screen.AudioLabFx.route) },
                modifier = modifier,
            ) { FxSketch(active = effects.isNotEmpty()) }
        }
        val stageTile: @Composable (Modifier) -> Unit = { modifier ->
            SmallHubCard(
                title = stringResource(R.string.stage_card_title),
                status = if (stage.enabled) stringResource(R.string.stage_on) else stringResource(R.string.lab_status_off),
                onClick = { navController.navigate(Screen.AudioLabStage.route) },
                modifier = modifier,
            ) { StageMiniSketch(active = stage.enabled) }
        }
        val transition: @Composable (Modifier) -> Unit = { modifier ->
            SmallHubCard(
                title = stringResource(R.string.lab_transition),
                status = stringResource(R.string.lab_transition_auto),
                onClick = { navController.navigate(Screen.EditTransition.createRoute(null)) },
                modifier = modifier,
            ) { TransitionSketch(active = true) }
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 560.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
                        equalizer(Modifier.weight(1f).fillMaxHeight())
                        effect(Modifier.weight(1f).fillMaxHeight())
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
                        stageTile(Modifier.weight(1f).fillMaxHeight())
                        transition(Modifier.weight(1f).fillMaxHeight())
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

@Composable
private fun effectsStatus(effects: List<ActiveEffect>): String {
    val names = effects.map { stringResource(effectLabelRes(it.key)).uppercase() }
    return when {
        names.isEmpty() -> stringResource(R.string.lab_status_all_off)
        names.size <= 2 -> names.joinToString(", ")
        else -> stringResource(R.string.lab_effects_and_more, names.take(2).joinToString(", "), names.size - 2)
    }
}

@Composable
private fun equalizerStatus(eq: EqSettings, userPresets: Map<String, String>): String {
    if (!eq.enabled) return stringResource(R.string.lab_status_off)
    val custom = stringResource(R.string.lab_status_custom)
    val curve = eq.presetId?.let { id -> EqPresets.BuiltIn.firstOrNull { it.id == id }?.name ?: userPresets[id] } ?: custom
    val correction = eq.correction?.takeIf { eq.correctionEnabled }?.model
    return (if (correction != null) "$curve · $correction" else curve).uppercase()
}

@Composable
private fun BigHubCard(
    title: String,
    status: String,
    on: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sketch: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 128.dp)
            .pressBounce(interaction, pressedScale = 0.97f)
            .clip(RoundedCornerShape(26.dp))
            .background(HubCard)
            .clickable(interaction, null, onClick = onClick)
            .damageBoundary(),
    ) {
        Box(Modifier.matchParentSize()) { sketch() }
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 18.dp)) {
            StatusLine(status, on, Modifier.fillMaxWidth(0.66f))
            Spacer(Modifier.height(30.dp))
            Text(
                title,
                fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 30.sp,
                letterSpacing = (-0.03).em, color = Color.White,
            )
        }
    }
}

@Composable
private fun SmallHubCard(
    title: String,
    status: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sketch: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .pressBounce(interaction, pressedScale = 0.97f)
            .clip(RoundedCornerShape(20.dp))
            .background(HubCard)
            .clickable(interaction, null, onClick = onClick)
            .damageBoundary(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.matchParentSize()) { sketch() }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp).fillMaxWidth(0.62f)) {
            Text(title, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            Text(
                status.uppercase(),
                fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFF8A8A8E),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusLine(status: String, on: Boolean, modifier: Modifier = Modifier) {
    val dot by androidx.compose.animation.animateColorAsState(
        if (on) Color(0xFF8BE07A) else Color(0xFF5C5C63), tween(300), label = "dot",
    )
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(8.dp))
        Text(
            status,
            fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF8A8A8E),
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
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
    Canvas(Modifier.fillMaxSize()) {
        val t = time.value
        if (t != last[0]) { level.update(0.016f); last[0] = t }
        val panel = size.height * 1.05f
        val origin = Offset(size.width - panel * 0.78f, size.height * 0.05f)
        rotate(-14f, Offset(origin.x + panel / 2, origin.y + panel / 2)) {
            drawRoundRect(Sketch, origin, Size(panel, panel), CornerRadius(panel * 0.2f), style = Stroke(3.5.dp.toPx()))
            listOf(low, mid, high).forEachIndexed { i, gain ->
                val x = origin.x + panel * (0.25f + i * 0.22f)
                val top = origin.y + panel * 0.16f
                val bottom = origin.y + panel * 0.84f
                drawLine(Sketch, Offset(x, top), Offset(x, bottom), 4.dp.toPx(), StrokeCap.Round)
                val bob = level.punch * 4.dp.toPx() * sin(t * 9f + i * 2f)
                val y = (top + bottom) / 2 - (gain / 12f) * (bottom - top) / 2 + bob
                drawCircle(HubCard, 11.dp.toPx(), Offset(x, y))
                drawCircle(if (eq.enabled) Color(0xFF9A9AA0) else Sketch, 11.dp.toPx(), Offset(x, y), style = Stroke(4.dp.toPx()))
            }
        }
    }
}

@Composable
private fun BoxScope.FxSketch(active: Boolean) {
    val measurer = rememberTextMeasurer()
    val glow by animateFloatAsState(if (active) 1f else 0f, tween(500), label = "fx")
    val time = sketchTime { active }
    Canvas(Modifier.fillMaxSize()) {
        val style = TextStyle(
            fontFamily = InterFontFamily, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic,
            fontSize = (size.height / density * 1.05f).sp,
            drawStyle = Stroke(width = 1.6.dp.toPx()),
        )
        val layout = measurer.measure("FX", style)
        val topLeft = Offset(size.width - layout.size.width * 0.86f, (size.height - layout.size.height) / 2f + size.height * 0.08f)
        drawText(layout, color = Sketch, topLeft = topLeft)
        if (glow > 0.01f) {
            val sweep = (time.value * 0.35f) % 1f
            val x = topLeft.x + layout.size.width * (sweep * 1.6f - 0.3f)
            drawText(
                layout,
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.85f * glow), Color.Transparent),
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
    Canvas(Modifier.fillMaxHeight().width(56.dp).align(Alignment.CenterEnd).padding(end = 14.dp, top = 16.dp, bottom = 16.dp)) {
        val w = size.width
        val h = size.height
        val color = (if (active) Color.White else Sketch).copy(alpha = on)
        val stroke = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round)
        drawRect(color, Offset(w * 0.12f, h * 0.25f), Size(w * 0.56f, h * 0.62f), style = stroke)
        drawRect(color.copy(alpha = on * 0.6f), Offset(w * 0.32f, h * 0.1f), Size(w * 0.56f, h * 0.62f), style = stroke)
        drawCircle(if (active) Color(0xFFF2D46B) else Sketch, 3.dp.toPx(), Offset(w * 0.5f, h * 0.48f))
    }
}

@Composable
private fun BoxScope.TransitionSketch(active: Boolean) {
    val on by animateFloatAsState(if (active) 1f else 0.4f, tween(400), label = "t")
    Canvas(Modifier.fillMaxHeight().width(56.dp).align(Alignment.CenterEnd).padding(end = 14.dp, top = 18.dp, bottom = 18.dp)) {
        val w = size.width
        val h = size.height
        val out = Path().apply { moveTo(0f, 0f); cubicTo(w * 0.5f, 0f, w * 0.5f, h, w, h) }
        val inn = Path().apply { moveTo(0f, h); cubicTo(w * 0.5f, h, w * 0.5f, 0f, w, 0f) }
        drawPath(out, Sketch.copy(alpha = on), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        drawPath(inn, Sketch.copy(alpha = on), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
}
