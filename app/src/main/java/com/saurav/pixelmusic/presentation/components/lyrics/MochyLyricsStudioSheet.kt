package com.saurav.pixelmusic.presentation.components.lyrics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurav.pixelmusic.lyrics.MochyLyricsEngine
import com.saurav.pixelmusic.lyrics.MochyLyricsPreferences
import com.saurav.pixelmusic.lyrics.MochyLyricsStyle
import com.saurav.pixelmusic.ui.theme.GoogleSansRounded
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Mochy Lyrics Studio bottom sheet: lets the user switch between all 11 Mochy lyrics
 * showing styles, tune font size & line spacing, toggle soft-light depth blur,
 * enable live Google Translate & offline Romanization, and reorder all 10 lyrics providers.
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
        .collectAsStateWithLifecycle(initialValue = MochyLyricsStyle.APPLE)
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
    val providerOrder by remember(context) { MochyLyricsPreferences.providerOrderFlow(context) }
        .collectAsStateWithLifecycle(initialValue = MochyLyricsPreferences.DefaultProviderOrder)

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

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

            // 1. Showing Styles (All 11 Mochy styles)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Showing Style",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = currentStyle.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MochyLyricsStyle.entries.forEach { style ->
                        val selected = style == currentStyle
                        FilterChip(
                            selected = selected,
                            onClick = { scope.launch { MochyLyricsPreferences.setStyle(context, style) } },
                            label = {
                                Text(
                                    text = style.displayName,
                                    fontFamily = GoogleSansRounded,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // 2. Typography & Depth Blur
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

            // 3. Translation & Romanization
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Translation & Romanization",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                StudioToggleRow(
                    title = "Auto-Translate Lyrics",
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
                StudioToggleRow(
                    title = "Auto-Romanize Lyrics",
                    subtitle = "Show Latin script reading for Hindi, Cyrillic, and Japanese Kana",
                    checked = autoRomanize,
                    onCheckedChange = { scope.launch { MochyLyricsPreferences.setAutoRomanize(context, it) } },
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
