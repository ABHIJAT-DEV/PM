package com.saurav.pixelmusic.presentation.screens.audiolab

import android.content.Context
import android.media.AudioManager
import android.media.MediaCodecList
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurav.pixelmusic.R
import com.saurav.pixelmusic.audiolab.MaxSpeed
import com.saurav.pixelmusic.audiolab.MinSpeed
import com.saurav.pixelmusic.audiolab.PlaybackSettings
import com.saurav.pixelmusic.audiolab.ReverbPresets
import com.saurav.pixelmusic.audiolab.audio.ReplayGainMode
import com.saurav.pixelmusic.audiolab.audio.SpatialMode
import com.saurav.pixelmusic.presentation.components.MiniPlayerHeight
import com.saurav.pixelmusic.presentation.navigation.Screen
import com.saurav.pixelmusic.presentation.screens.TabAnimation
import com.saurav.pixelmusic.presentation.screens.audiolab.stage.StagePicture
import com.saurav.pixelmusic.presentation.viewmodel.AudioLabViewModel
import com.saurav.pixelmusic.ui.theme.PixelMusicStatusBarStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class FxTab(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    Reverb(R.string.lab_effect_reverb, R.drawable.rounded_blur_on_24),
    Loudness(R.string.lab_effect_loudness, R.drawable.rounded_volume_up_24),
    Spatial(R.string.lab_effect_spatial, R.drawable.rounded_headphones_24),
    Stage(R.string.lab_effect_stage, R.drawable.rounded_surround_sound_24),
    Atmos(R.string.lab_effect_surround, R.drawable.rounded_speaker_24),
    Mono(R.string.lab_effect_mono, R.drawable.rounded_hearing_24),
    Speed(R.string.lab_effect_playback, R.drawable.rounded_speed_24),
    Volume(R.string.lab_effect_volume, R.drawable.rounded_show_chart_24),
}

private val FxTabEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioFxScreen(
    navController: NavController,
    viewModel: AudioLabViewModel = hiltViewModel(),
) {
    ListenToAudio()
    val reverb by viewModel.reverb.collectAsState()
    val loudness by viewModel.loudness.collectAsState()
    val spatial by viewModel.spatial.collectAsState()
    val playback by viewModel.playback.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val stage by viewModel.stage.collectAsState()
    val effects = activeEffects(reverb, loudness, spatial, playback, volume, stage)

    val cs = MaterialTheme.colorScheme
    PixelMusicStatusBarStyle(color = cs.background)
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val tabs = FxTab.entries
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    var tappedTabIndex by remember { mutableStateOf<Int?>(null) }
    val activePillIndex = tappedTabIndex ?: pagerState.currentPage

    fun isTabActive(tab: FxTab): Boolean = when (tab) {
        FxTab.Reverb -> reverb.enabled
        FxTab.Loudness -> loudness.enabled
        FxTab.Spatial -> spatial.enabled
        FxTab.Stage -> stage.enabled
        FxTab.Atmos -> spatial.surround
        FxTab.Mono -> spatial.mono
        FxTab.Speed -> !playback.isNormal
        FxTab.Volume -> volume.enabled
    }

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
                .padding(top = statusTop + 10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
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
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.lab_audio_effect),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface,
                    )
                    Text(
                        text = if (effects.isEmpty()) stringResource(R.string.lab_status_all_off)
                        else "${effects.size} active",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (effects.isEmpty()) cs.onSurfaceVariant else cs.primary,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            PrimaryScrollableTabRow(
                selectedTabIndex = activePillIndex,
                containerColor = Color.Transparent,
                edgePadding = 12.dp,
                indicator = {},
                divider = {},
            ) {
                tabs.forEachIndexed { index, tab ->
                    val enabled = isTabActive(tab)
                    val selected = activePillIndex == index
                    TabAnimation(
                        index = index,
                        title = tab.name,
                        selectedIndex = activePillIndex,
                        unselectedColor = if (enabled) cs.secondaryContainer.copy(alpha = 0.7f) else cs.surfaceContainerLow,
                        onUnselectedColor = if (enabled) cs.onSecondaryContainer else cs.onSurface.copy(alpha = 0.85f),
                        onClick = {
                            tappedTabIndex = index
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = index,
                                    animationSpec = tween(durationMillis = 320, easing = FxTabEasing),
                                )
                                tappedTabIndex = null
                            }
                        },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 4.dp),
                        ) {
                            Icon(
                                painter = painterResource(tab.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(tab.labelRes),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                beyondViewportPageCount = 1,
            ) { page ->
                val tab = tabs[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(top = 6.dp, bottom = navBottom + MiniPlayerHeight + 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (tab) {
                        FxTab.Reverb -> ReverbTabPage(viewModel)
                        FxTab.Loudness -> LoudnessTabPage(viewModel)
                        FxTab.Spatial -> SpatialTabPage(viewModel)
                        FxTab.Stage -> StageTabPage(viewModel) { navController.navigate(Screen.AudioLabStage.route) }
                        FxTab.Atmos -> AtmosTabPage(viewModel)
                        FxTab.Mono -> MonoTabPage(viewModel)
                        FxTab.Speed -> SpeedTabPage(viewModel)
                        FxTab.Volume -> VolumeTabPage(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
internal fun EffectCard(
    picture: @Composable () -> Unit,
    header: @Composable () -> Unit,
    expanded: Boolean,
    options: @Composable ColumnScope.() -> Unit,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    LabCard(padding = 16.dp) {
        header()
        LabCollapsible(expanded) {
            Spacer(Modifier.height(12.dp))
            options()
        }
        footer?.invoke(this)
    }
    LabCard(padding = 12.dp) {
        LabStage(height = 176.dp) { picture() }
    }
}

internal fun pct(v: Float) = "${(v * 100).roundToInt()}%"

@Composable
private fun ReverbTabPage(viewModel: AudioLabViewModel) {
    val reverb by viewModel.reverb.collectAsState()
    EffectCard(
        picture = { ReverbPicture(reverb) },
        header = {
            LabToggleRow(
                title = stringResource(R.string.fx_reverb_title),
                subtitle = stringResource(R.string.fx_reverb_sub),
                checked = reverb.enabled,
                onCheckedChange = { on -> viewModel.updateReverb { it.copy(enabled = on) } },
                iconRes = R.drawable.rounded_blur_on_24,
            )
        },
        expanded = reverb.enabled,
        options = {
            LabChips(
                chips = ReverbPresets.All.map { LabChip(it.id, reverbTabPresetName(it)) } +
                    (if (reverb.presetId == null) listOf(LabChip("custom", stringResource(R.string.fx_reverb_custom))) else emptyList()),
                selectedKey = reverb.presetId ?: "custom",
                onSelect = { id -> ReverbPresets.All.firstOrNull { it.id == id }?.let { p -> viewModel.updateReverb { it.withPreset(p) } } },
            )
            Spacer(Modifier.height(10.dp))
            LabPanel {
                LabSlider(stringResource(R.string.fx_mix), pct(reverb.mix), reverb.mix, { v -> viewModel.updateReverb { it.copy(mix = v, presetId = null) } }, 0f..1f)
                LabSlider(stringResource(R.string.fx_room_size), pct(reverb.roomSize), reverb.roomSize, { v -> viewModel.updateReverb { it.copy(roomSize = v, presetId = null) } }, 0f..1f)
                LabSlider(stringResource(R.string.fx_damping), pct(reverb.damping), reverb.damping, { v -> viewModel.updateReverb { it.copy(damping = v, presetId = null) } }, 0f..1f)
                LabSlider(stringResource(R.string.fx_wet), pct(reverb.wet), reverb.wet, { v -> viewModel.updateReverb { it.copy(wet = v, presetId = null) } }, 0f..1f)
                Text(
                    stringResource(R.string.fx_fine_tune),
                    fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.3.sp,
                    color = LabColors.Secondary, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                )
                LabSlider(stringResource(R.string.fx_dry), pct(reverb.dry), reverb.dry, { v -> viewModel.updateReverb { it.copy(dry = v, presetId = null) } }, 0f..1f)
                LabSlider(stringResource(R.string.fx_width), pct(reverb.width), reverb.width, { v -> viewModel.updateReverb { it.copy(width = v, presetId = null) } }, 0f..1f)
                LabSlider(stringResource(R.string.fx_pre_delay), "${reverb.preDelayMs.roundToInt()} ms", reverb.preDelayMs,
                    { v -> viewModel.updateReverb { it.copy(preDelayMs = v.roundToInt().toFloat(), presetId = null) } }, 0f..120f)
                LabSlider(stringResource(R.string.fx_low_cut), "${reverb.lowCutHz.roundToInt()} Hz", log10(reverb.lowCutHz),
                    { v -> viewModel.updateReverb { it.copy(lowCutHz = 10f.pow(v).roundToInt().toFloat(), presetId = null) } },
                    log10(20f)..log10(1000f))
            }
        },
    )
}

@Composable
private fun reverbTabPresetName(preset: ReverbPresets.Preset): String = when (preset.id) {
    "room" -> stringResource(R.string.fx_reverb_room)
    "studio" -> stringResource(R.string.fx_reverb_studio)
    "bathroom" -> stringResource(R.string.fx_reverb_bathroom)
    "plate" -> stringResource(R.string.fx_reverb_plate)
    "hall" -> stringResource(R.string.fx_reverb_hall)
    "cathedral" -> stringResource(R.string.fx_reverb_cathedral)
    "stadium" -> stringResource(R.string.fx_reverb_stadium)
    "dream" -> stringResource(R.string.fx_reverb_dream)
    else -> preset.name
}

@Composable
private fun LoudnessTabPage(viewModel: AudioLabViewModel) {
    val loudness by viewModel.loudness.collectAsState()
    val normalize by viewModel.normalizeEnabled.collectAsState()
    val onNormalize: (Boolean) -> Unit = viewModel::setNormalizeEnabled
    EffectCard(
        picture = { LoudnessPicture(loudness, viewModel.fx) },
        header = {
            LabToggleRow(
                title = stringResource(R.string.fx_loudness_title),
                subtitle = stringResource(R.string.fx_loudness_sub),
                checked = loudness.enabled,
                onCheckedChange = { on -> viewModel.updateLoudness { it.copy(enabled = on) } },
                iconRes = R.drawable.rounded_volume_up_24,
            )
        },
        expanded = loudness.enabled,
        options = {
            LabChips(
                chips = listOf(
                    LabChip("gentle", stringResource(R.string.fx_loudness_gentle)),
                    LabChip("punchy", stringResource(R.string.fx_loudness_punchy)),
                    LabChip("night", stringResource(R.string.fx_loudness_night)),
                    LabChip("max", stringResource(R.string.fx_loudness_max)),
                ),
                selectedKey = when {
                    loudness.boostDb == 2f && loudness.compression == 0.15f -> "gentle"
                    loudness.boostDb == 4f && loudness.compression == 0.3f -> "punchy"
                    loudness.boostDb == 3f && loudness.compression == 0.85f -> "night"
                    loudness.boostDb == 9f && loudness.compression == 0.5f -> "max"
                    else -> null
                },
                onSelect = { key ->
                    val (boost, comp) = when (key) {
                        "gentle" -> 2f to 0.15f
                        "night" -> 3f to 0.85f
                        "max" -> 9f to 0.5f
                        else -> 4f to 0.3f
                    }
                    viewModel.updateLoudness { it.copy(boostDb = boost, compression = comp) }
                },
            )
            Spacer(Modifier.height(10.dp))
            LabPanel {
                LabSlider(stringResource(R.string.fx_boost), "+${String.format("%.1f", loudness.boostDb)} dB", loudness.boostDb,
                    { v -> viewModel.updateLoudness { it.copy(boostDb = (v * 2).roundToInt() / 2f) } }, 0f..12f)
                LabSlider(stringResource(R.string.fx_compression), pct(loudness.compression), loudness.compression,
                    { v -> viewModel.updateLoudness { it.copy(compression = (v * 100).roundToInt() / 100f) } }, 0f..1f)
                Text(
                    stringResource(R.string.fx_compression_note),
                    fontFamily = InterFontFamily, fontSize = 12.sp, lineHeight = 16.sp, color = LabColors.Secondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        },
        footer = {
            Spacer(Modifier.height(8.dp))
            LabToggleRow(
                title = stringResource(R.string.fx_even_out),
                subtitle = stringResource(R.string.fx_even_out_sub),
                checked = normalize,
                onCheckedChange = onNormalize,
                iconRes = R.drawable.rounded_graphic_eq_24,
            )
            val rg by viewModel.normalize.collectAsState()
            LabCollapsible(normalize) {
                Spacer(Modifier.height(4.dp))
                LabPanel {
                    Text(
                        stringResource(R.string.fx_replaygain),
                        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.3.sp,
                        color = LabColors.Secondary,
                    )
                    LabChips(
                        chips = listOf(LabChip("track", stringResource(R.string.fx_rg_track)), LabChip("album", stringResource(R.string.fx_rg_album))),
                        selectedKey = if (rg.replayGainMode == ReplayGainMode.Album) "album" else "track",
                        onSelect = { key ->
                            viewModel.updateNormalize {
                                it.copy(replayGainMode = if (key == "album") ReplayGainMode.Album else ReplayGainMode.Track)
                            }
                        },
                    )
                    LabSlider(stringResource(R.string.fx_preamp), (if (rg.preampDb > 0) "+" else "") + String.format("%.1f dB", rg.preampDb), rg.preampDb,
                        { v -> viewModel.updateNormalize { it.copy(preampDb = (v * 2).roundToInt() / 2f) } }, -6f..6f)
                    Text(
                        stringResource(R.string.fx_replaygain_note),
                        fontFamily = InterFontFamily, fontSize = 12.sp, lineHeight = 16.sp, color = LabColors.Secondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
    )
}

private class CrossfeedLevel(val key: String, @StringRes val label: Int, val cutoff: Float, val feed: Float)

private val CrossfeedLevels = listOf(
    CrossfeedLevel("light", R.string.fx_xf_light, 700f, 4.5f),
    CrossfeedLevel("natural", R.string.fx_xf_natural, 700f, 6f),
    CrossfeedLevel("strong", R.string.fx_xf_strong, 650f, 9.5f),
)

@Composable
private fun SpatialTabPage(viewModel: AudioLabViewModel) {
    val spatial by viewModel.spatial.collectAsState()
    val stage by viewModel.stage.collectAsState()
    EffectCard(
        picture = { SpatialPicture(spatial) },
        header = {
            LabToggleRow(
                title = stringResource(R.string.fx_spatial_title),
                subtitle = stringResource(R.string.fx_spatial_sub),
                checked = spatial.enabled,
                onCheckedChange = { on -> viewModel.updateSpatial { it.copy(enabled = on) } },
                iconRes = R.drawable.rounded_headphones_24,
            )
        },
        expanded = spatial.enabled,
        options = {
            LabSegmented(
                options = listOf(stringResource(R.string.fx_mode_crossfeed), stringResource(R.string.fx_mode_speakers)),
                selectedIndex = if (spatial.mode == SpatialMode.Crossfeed) 0 else 1,
                onSelect = { i -> viewModel.updateSpatial { it.copy(mode = if (i == 0) SpatialMode.Crossfeed else SpatialMode.Speakers) } },
                height = 46.dp,
            )
            Spacer(Modifier.height(10.dp))
            if (spatial.mode == SpatialMode.Crossfeed) {
                LabChips(
                    chips = CrossfeedLevels.map { LabChip(it.key, stringResource(it.label)) },
                    selectedKey = CrossfeedLevels.firstOrNull { it.cutoff == spatial.cutoffHz && it.feed == spatial.feedDb }?.key,
                    onSelect = { key ->
                        CrossfeedLevels.firstOrNull { it.key == key }?.let { l ->
                            viewModel.updateSpatial { it.copy(cutoffHz = l.cutoff, feedDb = l.feed) }
                        }
                    },
                )
                Spacer(Modifier.height(10.dp))
                LabPanel {
                    LabSlider(stringResource(R.string.fx_head_shadow), "${spatial.cutoffHz.roundToInt()} Hz", spatial.cutoffHz,
                        { v -> viewModel.updateSpatial { it.copy(cutoffHz = (v / 10).roundToInt() * 10f) } }, 300f..1500f)
                    LabSlider(stringResource(R.string.fx_mode_crossfeed), "−${String.format("%.1f", spatial.feedDb)} dB", 16f - spatial.feedDb,
                        { v -> viewModel.updateSpatial { it.copy(feedDb = ((16f - v) * 2).roundToInt() / 2f) } }, 1f..15f)
                    LabSlider(stringResource(R.string.fx_amount), pct(spatial.amount), spatial.amount,
                        { v -> viewModel.updateSpatial { it.copy(amount = v) } }, 0f..1f)
                }
            } else {
                LabPanel {
                    LabSlider(stringResource(R.string.fx_speaker_angle), "±${spatial.speakerAngle.roundToInt()}°", spatial.speakerAngle,
                        { v -> viewModel.updateSpatial { it.copy(speakerAngle = v.roundToInt().toFloat()) } }, 10f..90f)
                    LabSlider(stringResource(R.string.fx_amount), pct(spatial.amount), spatial.amount,
                        { v -> viewModel.updateSpatial { it.copy(amount = v) } }, 0f..1f)
                    Text(
                        stringResource(R.string.fx_speakers_note),
                        fontFamily = InterFontFamily, fontSize = 12.sp, lineHeight = 16.sp, color = LabColors.Secondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
        footer = if (stage.enabled && spatial.enabled) {
            {
                Text(
                    stringResource(R.string.stage_spatial_note),
                    fontFamily = InterFontFamily, fontSize = 12.sp, lineHeight = 16.sp, color = LabColors.Secondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                )
            }
        } else null,
    )
}

@Composable
private fun StageTabPage(viewModel: AudioLabViewModel, onOpenRoom: () -> Unit) {
    val stage by viewModel.stage.collectAsState()
    val cs = MaterialTheme.colorScheme
    EffectCard(
        picture = { Box(Modifier.fillMaxSize().clickable(onClick = onOpenRoom)) { StagePicture(stage) } },
        header = {
            LabToggleRow(
                title = stringResource(R.string.stage_card_title),
                subtitle = stringResource(R.string.stage_card_sub),
                checked = stage.enabled,
                onCheckedChange = { on -> viewModel.updateStage { it.copy(enabled = on) } },
                iconRes = R.drawable.rounded_surround_sound_24,
            )
        },
        expanded = false,
        options = {},
        footer = {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onOpenRoom,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.primaryContainer,
                    contentColor = cs.onPrimaryContainer,
                ),
                modifier = Modifier.fillMaxWidth().height(46.dp),
            ) {
                Text(stringResource(R.string.stage_open), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.rounded_arrow_forward_24),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
    )
}

@Composable
private fun MonoTabPage(viewModel: AudioLabViewModel) {
    val spatial by viewModel.spatial.collectAsState()
    EffectCard(
        picture = { MonoPicture(enabled = spatial.mono) },
        header = {
            LabToggleRow(
                title = stringResource(R.string.fx_mono_title),
                subtitle = stringResource(R.string.fx_mono_sub),
                checked = spatial.mono,
                onCheckedChange = { on -> viewModel.updateSpatial { it.copy(mono = on) } },
                iconRes = R.drawable.rounded_hearing_24,
            )
        },
        expanded = false,
        options = {},
    )
}

@Composable
private fun MonoPicture(enabled: Boolean) {
    val merged by animateFloatAsState(if (enabled) 1f else 0f, tween(450, easing = FxTabEasing), label = "monoMerge")
    val ink = LabColors.Primary
    val accent = LabColors.Accent
    PictureCanvas { t, clock ->
        val w = size.width
        val h = size.height
        val cy = h / 2f
        val leftX = 20.dp.toPx()
        val rightX = w - 20.dp.toPx()
        val amp = h * (0.14f + 0.14f * clock.level.level.coerceAtMost(1f))
        val sep = h * 0.18f * (1f - merged)
        val pathL = androidx.compose.ui.graphics.Path()
        val pathR = androidx.compose.ui.graphics.Path()
        var x = leftX
        while (x <= rightX) {
            val p = (x - leftX) / (rightX - leftX)
            val env = sin(p * PI.toFloat())
            val waveA = sin(p * 4f * PI.toFloat() - t * 3.5f) * amp * env
            val waveB = sin(p * 4f * PI.toFloat() - t * 3.5f + (1f - merged) * 1.4f) * amp * env
            val yL = cy - sep + waveA
            val yR = cy + sep + waveB
            if (x == leftX) {
                pathL.moveTo(x, yL)
                pathR.moveTo(x, yR)
            } else {
                pathL.lineTo(x, yL)
                pathR.lineTo(x, yR)
            }
            x += 2.dp.toPx()
        }
        drawLine(ink.copy(alpha = 0.12f), Offset(leftX, cy), Offset(rightX, cy), 1.dp.toPx())
        drawPath(pathL, Color(0xFF3A96DE).copy(alpha = 0.85f * (1f - merged * 0.5f)), style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
        drawPath(pathR, Color(0xFFE05A82).copy(alpha = 0.85f * (1f - merged * 0.5f)), style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
        if (merged > 0.05f) {
            drawPath(pathL, accent.copy(alpha = merged), style = Stroke(2.6.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun AtmosTabPage(viewModel: AudioLabViewModel) {
    val spatial by viewModel.spatial.collectAsState()
    val context = LocalContext.current
    var device by remember { mutableStateOf<AtmosSupport?>(null) }
    LaunchedEffect(Unit) { device = withContext(Dispatchers.Default) { atmosSupport(context) } }

    EffectCard(
        picture = { AtmosPicture(enabled = spatial.surround, renderOnHeadphones = spatial.surround) },
        header = {
            LabToggleRow(
                title = stringResource(R.string.fx_atmos_title),
                subtitle = stringResource(R.string.fx_atmos_sub),
                checked = spatial.surround,
                onCheckedChange = { on -> viewModel.updateSpatial { it.copy(surround = on) } },
                iconRes = R.drawable.rounded_speaker_24,
            )
        },
        expanded = false,
        options = {},
        footer = {
            Text(
                stringResource(R.string.atmos_streaming_note),
                fontFamily = InterFontFamily, fontSize = 12.sp, lineHeight = 16.sp, color = LabColors.Secondary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            )
            device?.let { support ->
                Spacer(Modifier.height(10.dp))
                Row(Modifier.padding(horizontal = 6.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    SupportPill(stringResource(R.string.fx_atmos_decoder), stringResource(support.decoder))
                    SupportPill(stringResource(R.string.fx_atmos_spatializer), stringResource(support.spatializer))
                }
            }
        },
    )
}

private class AtmosSupport(@StringRes val decoder: Int, @StringRes val spatializer: Int)

private fun atmosSupport(context: Context): AtmosSupport {
    val decoder = runCatching {
        MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.any { info ->
            !info.isEncoder && info.supportedTypes.any { it.equals("audio/eac3-joc", ignoreCase = true) }
        }
    }.getOrDefault(false)
    val spatializer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) spatializerState(context) else R.string.fx_atmos_needs_android
    return AtmosSupport(if (decoder) R.string.fx_atmos_yes else R.string.fx_atmos_not_on_phone, spatializer)
}

@RequiresApi(Build.VERSION_CODES.S_V2)
@StringRes
private fun spatializerState(context: Context): Int = runCatching {
    val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val spatializer = manager.spatializer
    when {
        !spatializer.isAvailable -> R.string.fx_state_not_available
        spatializer.isEnabled -> R.string.fx_state_on
        else -> R.string.fx_state_off
    }
}.getOrDefault(R.string.fx_state_unknown)

@Composable
private fun SupportPill(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(LabColors.Row)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(label.uppercase(), fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 10.sp,
            letterSpacing = 1.sp, color = LabColors.Faint)
        Text(value, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = LabColors.Primary)
    }
}

private class SpeedPreset(val key: String, @StringRes val labelRes: Int?, val rawLabel: String?, val speed: Float, val preservePitch: Boolean, val withReverb: Boolean = false)

private val SpeedPresets = listOf(
    SpeedPreset("normal", R.string.fx_speed_normal, null, 1f, true),
    SpeedPreset("slowed", R.string.fx_speed_slowed, null, 0.85f, false),
    SpeedPreset("slowed_reverb", R.string.fx_speed_slowed_reverb, null, 0.85f, false, withReverb = true),
    SpeedPreset("nightcore", R.string.fx_speed_nightcore, null, 1.25f, false),
    SpeedPreset("0.75", null, "0.75×", 0.75f, true),
    SpeedPreset("1.25", null, "1.25×", 1.25f, true),
    SpeedPreset("1.5", null, "1.5×", 1.5f, true),
    SpeedPreset("2", null, "2×", 2f, true),
)

@Composable
private fun SpeedTabPage(viewModel: AudioLabViewModel) {
    val playback by viewModel.playback.collectAsState()
    val reverb by viewModel.reverb.collectAsState()
    val cs = MaterialTheme.colorScheme
    EffectCard(
        picture = { SpeedPicture(playback) },
        header = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (!playback.isNormal) cs.primaryContainer else cs.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_speed_24),
                        contentDescription = null,
                        tint = if (!playback.isNormal) cs.onPrimaryContainer else cs.onSecondaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.fx_speed_title), fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = LabColors.Primary)
                    Text(
                        stringResource(if (playback.preservePitch) R.string.fx_pitch_stays else R.string.fx_pitch_follows),
                        fontFamily = InterFontFamily, fontSize = 12.sp, color = LabColors.Secondary,
                    )
                }
                StepButton("−") { viewModel.updatePlayback { it.copy(speed = ((it.speed - 0.05f) * 100).roundToInt() / 100f) } }
                Text(
                    String.format("%.2f×", playback.speed),
                    fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LabColors.Primary,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                StepButton("+") { viewModel.updatePlayback { it.copy(speed = ((it.speed + 0.05f) * 100).roundToInt() / 100f) } }
            }
        },
        expanded = true,
        options = {
            LabChips(
                chips = SpeedPresets.map { LabChip(it.key, it.labelRes?.let { id -> stringResource(id) } ?: it.rawLabel.orEmpty()) },
                selectedKey = if (playback.speed == 0.85f && !playback.preservePitch) {
                    if (reverb.enabled) "slowed_reverb" else "slowed"
                } else {
                    SpeedPresets.firstOrNull { p ->
                        !p.withReverb && p.speed == playback.speed && p.preservePitch == playback.preservePitch &&
                            (!p.preservePitch || playback.pitchSemitones == 0f)
                    }?.key
                },
                onSelect = { key ->
                    val p = SpeedPresets.first { it.key == key }
                    viewModel.updatePlayback { it.copy(speed = p.speed, preservePitch = p.preservePitch, pitchSemitones = 0f) }
                    if (p.withReverb) {
                        ReverbPresets.All.firstOrNull { it.id == "hall" }?.let { hall ->
                            viewModel.updateReverb { it.withPreset(hall).copy(enabled = true) }
                        }
                    } else if (key == "slowed" && reverb.enabled) {
                        viewModel.updateReverb { it.copy(enabled = false) }
                    }
                },
            )
            Spacer(Modifier.height(10.dp))
            LabPanel {
                LabSlider(
                    stringResource(R.string.fx_speed), String.format("%.2f×", playback.speed), log10(playback.speed),
                    { v -> viewModel.updatePlayback { it.copy(speed = (10f.pow(v) * 100).roundToInt() / 100f) } },
                    log10(MinSpeed)..log10(MaxSpeed),
                )
                LabToggleRow(stringResource(R.string.fx_preserve_pitch), stringResource(R.string.fx_preserve_pitch_sub), playback.preservePitch,
                    { on -> viewModel.updatePlayback { it.copy(preservePitch = on) } })
                LabCollapsible(playback.preservePitch) {
                    LabSlider(
                        stringResource(R.string.fx_pitch),
                        stringResource(R.string.fx_semitones, (if (playback.pitchSemitones > 0) "+" else "") + playback.pitchSemitones.roundToInt()),
                        playback.pitchSemitones,
                        { v -> viewModel.updatePlayback { it.copy(pitchSemitones = v.roundToInt().toFloat()) } },
                        -12f..12f,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                LabTextAction(stringResource(R.string.fx_reset), R.drawable.ic_lab_refresh, enabled = !playback.isNormal) {
                    viewModel.updatePlayback { PlaybackSettings() }
                }
            }
        },
    )
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(32.dp).clip(CircleShape).background(LabColors.Row).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LabColors.Primary)
    }
}

private class CurveShape(val key: String, @StringRes val label: Int, val exponent: Float)

private val CurveShapes = listOf(
    CurveShape("gentle", R.string.fx_curve_gentle, 1.6f),
    CurveShape("exponential", R.string.fx_curve_exponential, 2.2f),
    CurveShape("steep", R.string.fx_curve_steep, 3.2f),
)

@Composable
private fun VolumeTabPage(viewModel: AudioLabViewModel) {
    val volume by viewModel.volume.collectAsState()
    EffectCard(
        picture = { VolumeCurvePicture(volume, 0.7f) },
        header = {
            LabToggleRow(
                title = stringResource(R.string.fx_volume_title),
                subtitle = stringResource(R.string.fx_volume_sub),
                checked = volume.enabled,
                onCheckedChange = { on -> viewModel.updateVolume { it.copy(enabled = on) } },
                iconRes = R.drawable.rounded_show_chart_24,
            )
        },
        expanded = volume.enabled,
        options = {
            LabChips(
                chips = CurveShapes.map { LabChip(it.key, stringResource(it.label)) },
                selectedKey = CurveShapes.firstOrNull { it.exponent == volume.exponent }?.key,
                onSelect = { key -> CurveShapes.firstOrNull { it.key == key }?.let { c -> viewModel.updateVolume { it.copy(exponent = c.exponent) } } },
            )
            Spacer(Modifier.height(10.dp))
            LabPanel {
                LabSlider(stringResource(R.string.fx_curve), String.format("%.1f", volume.exponent), volume.exponent,
                    { v -> viewModel.updateVolume { it.copy(exponent = (v * 10).roundToInt() / 10f) } }, 1f..4f)
                LabSlider(stringResource(R.string.fx_volume_limit), pct(volume.limit), volume.limit,
                    { v -> viewModel.updateVolume { it.copy(limit = (v * 20).roundToInt() / 20f) } }, 0.3f..1f)
                Text(
                    stringResource(R.string.fx_volume_note),
                    fontFamily = InterFontFamily, fontSize = 12.sp, lineHeight = 16.sp, color = LabColors.Secondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        },
    )
}
