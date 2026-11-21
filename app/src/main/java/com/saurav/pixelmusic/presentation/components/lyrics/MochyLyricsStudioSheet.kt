package com.saurav.pixelmusic.presentation.components.lyrics

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurav.pixelmusic.lyrics.MochyLyricsEngine
import com.saurav.pixelmusic.lyrics.MochyLyricsPreferences
import com.saurav.pixelmusic.lyrics.MochyLyricsStyle
import com.saurav.pixelmusic.ui.theme.GoogleSansRounded
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Lyrics Style & Providers Studio bottom sheet:
 * - Swipeable live-animated Lyrics Animation cards for the 5 exact styles
 *   (Style 1, Style 2, Boring, Plain, and PixelMusic)
 * - Romanisation, Romanisation as main line, and Google Translate toggles
 * - Typography & Soft Light depth blur sliders
 * - All 10 lyrics providers priority ordering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MochyLyricsStudioSheet(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentStyle by remember(context) { MochyLyricsPreferences.styleFlow(context) }
        .collectAsStateWithLifecycle(initialValue = MochyLyricsStyle.MOTION)
    val textSize by remember(context) { MochyLyricsPreferences.textSizeFlow(context) }
        .collectAsStateWithLifecycle(initialValue = 26f)
    val lineSpacing by remember(context) { MochyLyricsPreferences.lineSpacingFlow(context) }
        .collectAsStateWithLifecycle(initialValue = 1.25f)
    val softLight by remember(context) { MochyLyricsPreferences.softLightFlow(context) }
        .collectAsStateWithLifecycle(initialValue = true)
    val autoTranslate by remember(context) { MochyLyricsPreferences.autoTranslateFlow(context) }
        .collectAsStateWithLifecycle(initialValue = false)
    val translateLang by remember(context) { MochyLyricsPreferences.translateLanguageFlow(context) }
        .collectAsStateWithLifecycle(initialValue = "en")
    val autoRomanize by remember(context) { MochyLyricsPreferences.autoRomanizeFlow(context) }
        .collectAsStateWithLifecycle(initialValue = false)
    val romanizationAsMain by remember(context) { MochyLyricsPreferences.romanizationAsMainFlow(context) }
        .collectAsStateWithLifecycle(initialValue = false)
    val providerOrder by remember(context) { MochyLyricsPreferences.providerOrderFlow(context) }
        .collectAsStateWithLifecycle(initialValue = MochyLyricsPreferences.DefaultProviderOrder)

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Shared loop progress driving the live preview cards
    val infinite = rememberInfiniteTransition(label = "style_preview_loop")
    val previewPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "preview_phase",
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = navBottom + 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Lyrics Style & Providers",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.Bold,
            )

            // 1. Swipeable Lyrics Animation Cards (Exact 5 Styles)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Lyrics Animation",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MochyLyricsStyle.entries.forEach { style ->
                        LyricsStylePreviewCard(
                            style = style,
                            selected = style == currentStyle,
                            phase = previewPhase,
                            onClick = { scope.launch { MochyLyricsPreferences.setStyle(context, style) } },
                        )
                    }
                }
                Text(
                    text = "Swipe, then tap a style to use it · ${currentStyle.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // 2. Romanisation & Translation
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Romanisation & Translation",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                StudioToggleRow(
                    title = "Romanisation",
                    subtitle = "Show Latin script reading for Hindi, Cyrillic, and Japanese Kana",
                    checked = autoRomanize,
                    onCheckedChange = { scope.launch { MochyLyricsPreferences.setAutoRomanize(context, it) } },
                )
                if (autoRomanize) {
                    StudioToggleRow(
                        title = "Romanisation as the main line",
                        subtitle = "Put romanised lyrics on top and original script underneath",
                        checked = romanizationAsMain,
                        onCheckedChange = { scope.launch { MochyLyricsPreferences.setRomanizationAsMain(context, it) } },
                    )
                }
                StudioToggleRow(
                    title = "Translation",
                    subtitle = "Translate lyrics on the fly with Google Translate (no API key needed)",
                    checked = autoTranslate,
                    onCheckedChange = { scope.launch { MochyLyricsPreferences.setAutoTranslate(context, it) } },
                )
                if (autoTranslate) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        MochyLyricsPreferences.SupportedTranslateLanguages.forEach { (code, label) ->
                            FilterChip(
                                selected = translateLang == code,
                                onClick = { scope.launch { MochyLyricsPreferences.setTranslateLanguage(context, code) } },
                                label = { Text(label, fontFamily = GoogleSansRounded) },
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // 3. Typography & Soft Light
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Typography & Visuals",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Text Size", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("${textSize.roundToInt()} sp", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = textSize,
                    onValueChange = { scope.launch { MochyLyricsPreferences.setTextSize(context, it) } },
                    valueRange = 18f..36f,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Line Spacing", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("${(lineSpacing * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = lineSpacing,
                    onValueChange = { scope.launch { MochyLyricsPreferences.setLineSpacing(context, it) } },
                    valueRange = 1.0f..1.7f,
                )
                StudioToggleRow(
                    title = "Soft Light & Depth Blur",
                    subtitle = "Subtly blur inactive lines to keep focus on the active lyric",
                    checked = softLight,
                    onCheckedChange = { scope.launch { MochyLyricsPreferences.setSoftLight(context, it) } },
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // 4. Provider Priority Order (all 10 Mochy providers)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Provider Priority Order",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "PixelMusic asks these providers from top to bottom for word-synced lyrics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                providerOrder.forEachIndexed { index, providerId ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${index + 1}. ${MochyLyricsEngine.getDisplayName(providerId)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                enabled = index > 0,
                                onClick = {
                                    val mutable = providerOrder.toMutableList()
                                    val item = mutable.removeAt(index)
                                    mutable.add(index - 1, item)
                                    scope.launch { MochyLyricsPreferences.setProviderOrder(context, mutable) }
                                },
                                modifier = Modifier.size(34.dp),
                            ) {
                                Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Move up", modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                enabled = index < providerOrder.lastIndex,
                                onClick = {
                                    val mutable = providerOrder.toMutableList()
                                    val item = mutable.removeAt(index)
                                    mutable.add(index + 1, item)
                                    scope.launch { MochyLyricsPreferences.setProviderOrder(context, mutable) }
                                },
                                modifier = Modifier.size(34.dp),
                            ) {
                                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Move down", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsStylePreviewCard(
    style: MochyLyricsStyle,
    selected: Boolean,
    phase: Float,
    onClick: () -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val previewBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val activeColor = MaterialTheme.colorScheme.onSurface
    val accentColor = MaterialTheme.colorScheme.primary
    val dimColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = if (selected) BorderStroke(2.dp, accentColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .width(276.dp)
            .height(142.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: label + style title
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Lyrics\nAnimation",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = GoogleSansRounded,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp,
                )
                Text(
                    text = style.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) accentColor else activeColor,
                )
            }

            // Right: live animated mini preview matching the selected style's actual motion
            Box(
                modifier = Modifier
                    .width(126.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(previewBg, previewBg.copy(alpha = 0.65f))
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                val sampleWords = remember { listOf("would", "you", "still", "love", "me?") }
                val sweepPos = phase * sampleWords.size
                val cascadeLag = if (style.risesWords) sin(phase * PI).toFloat() * 5f else 0f
                val springScale = if (style == MochyLyricsStyle.PIXELMUSIC) {
                    1f + 0.06f * sin(phase * PI).toFloat()
                } else {
                    1f
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Active line preview
                    if (style == MochyLyricsStyle.PIXELMUSIC) {
                        Text(
                            text = "would you still love me?",
                            style = TextStyle(
                                fontFamily = GoogleSansRounded,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                            ),
                            modifier = Modifier.graphicsLayer {
                                scaleX = springScale
                                scaleY = springScale
                            },
                        )
                    } else {
                        val lineText = buildAnnotatedString {
                            sampleWords.forEachIndexed { idx, w ->
                                val progress = (sweepPos - idx).coerceIn(0f, 1f)
                                val wordColor = androidx.compose.ui.graphics.lerp(dimColor, activeColor, progress)
                                val wordShadow = if (style.shaded && progress in 0.1f..0.95f) {
                                    Shadow(color = accentColor.copy(alpha = 0.55f), blurRadius = 10f)
                                } else {
                                    null
                                }
                                withStyle(
                                    SpanStyle(
                                        color = wordColor,
                                        fontWeight = if (progress > 0.2f) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        shadow = wordShadow,
                                    )
                                ) {
                                    append(w)
                                }
                                if (idx < sampleWords.lastIndex) append(" ")
                            }
                        }
                        val waveLift = if (!style.risesWords && style.shaded) {
                            -sin(phase * 2.0 * PI).toFloat() * 2.2f
                        } else if (style.risesWords) {
                            (1f - phase.coerceIn(0f, 0.5f) * 2f) * 3.5f
                        } else {
                            0f
                        }
                        Text(
                            text = lineText,
                            style = TextStyle(
                                fontFamily = GoogleSansRounded,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                            ),
                            modifier = Modifier.graphicsLayer {
                                translationY = waveLift
                            },
                        )
                    }

                    // Upcoming line preview (shows Style 1 / Boring's cascade lag)
                    Text(
                        text = "of course, what\ndo you mean\nmy love",
                        style = TextStyle(
                            fontFamily = GoogleSansRounded,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = dimColor,
                        ),
                        modifier = Modifier.graphicsLayer {
                            translationY = cascadeLag
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontFamily = GoogleSansRounded, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
