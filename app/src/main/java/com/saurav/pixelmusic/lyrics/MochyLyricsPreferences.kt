package com.saurav.pixelmusic.lyrics

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.saurav.pixelmusic.data.preferences.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class MochyLyricsStyle(
    val displayName: String,
    val description: String,
) {
    APPLE("Apple Motion", "Words rise and glow as sung with soft depth blur"),
    APPLE_V2("Apple V2", "Crisp syllable sweep with smooth vertical spring"),
    MOCHY_1("Mochy 1", "Expressive gradient highlight with subtle pulse"),
    LYRICS_V2("Lyrics V2", "Clean balanced typography with smooth crossfade"),
    METRO_LYRICS("Metro", "Bold editorial contrast with rhythmic reveal"),
    KARAOKE("Karaoke", "Progressive left-to-right syllable fill"),
    GLOW("Glow", "Luminous soft-light halo on the active line"),
    LEGACY("Wave", "Classic wave sweep across sung letters"),
    SLIDE("Slide", "Gentle directional glide on active line change"),
    FADE("Fade", "Minimal smooth opacity focus"),
    NONE("Classic", "Clean static highlight without motion");

    companion object {
        fun fromName(name: String?): MochyLyricsStyle =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: APPLE
    }
}

object MochyLyricsPreferences {
    val StyleKey = stringPreferencesKey("mochy_lyrics_style")
    val TextSizeKey = floatPreferencesKey("mochy_lyrics_text_size")
    val LineSpacingKey = floatPreferencesKey("mochy_lyrics_line_spacing")
    val SoftLightKey = booleanPreferencesKey("mochy_lyrics_soft_light")
    val AutoTranslateKey = booleanPreferencesKey("mochy_lyrics_auto_translate")
    val TranslateLanguageKey = stringPreferencesKey("mochy_lyrics_translate_lang")
    val AutoRomanizeKey = booleanPreferencesKey("mochy_lyrics_auto_romanize")
    val ProviderOrderKey = stringPreferencesKey("mochy_lyrics_provider_order")

    val DefaultProviderOrder: List<String> = listOf(
        "BetterLyrics",
        "YouLyPlus",
        "Paxsenix",
        "LrcRed",
        "SimpMusic",
        "LrcLib",
        "Deezer",
        "NetEase",
        "QQMusic",
        "Kugou",
    )

    val SupportedTranslateLanguages: List<Pair<String, String>> = listOf(
        "en" to "English",
        "hi" to "Hindi",
        "es" to "Spanish",
        "fr" to "French",
        "de" to "German",
        "ja" to "Japanese",
        "ko" to "Korean",
        "zh-CN" to "Chinese",
        "ar" to "Arabic",
        "pt" to "Portuguese",
        "ru" to "Russian",
        "id" to "Indonesian",
        "tr" to "Turkish",
        "bn" to "Bengali",
    )

    fun styleFlow(context: Context): Flow<MochyLyricsStyle> =
        context.dataStore.data.map { MochyLyricsStyle.fromName(it[StyleKey]) }

    fun textSizeFlow(context: Context): Flow<Float> =
        context.dataStore.data.map { (it[TextSizeKey] ?: 26f).coerceIn(18f, 36f) }

    fun lineSpacingFlow(context: Context): Flow<Float> =
        context.dataStore.data.map { (it[LineSpacingKey] ?: 1.25f).coerceIn(1.0f, 1.7f) }

    fun softLightFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[SoftLightKey] ?: true }

    fun autoTranslateFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[AutoTranslateKey] ?: false }

    fun translateLanguageFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[TranslateLanguageKey]?.ifBlank { "en" } ?: "en" }

    fun autoRomanizeFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[AutoRomanizeKey] ?: false }

    fun providerOrderFlow(context: Context): Flow<List<String>> =
        context.dataStore.data.map { deserializeProviderOrder(it[ProviderOrderKey].orEmpty()) }

    fun deserializeProviderOrder(raw: String): List<String> {
        if (raw.isBlank()) return DefaultProviderOrder
        val saved = raw.split(",").map { it.trim() }.filter { it in DefaultProviderOrder }.distinct()
        return saved + DefaultProviderOrder.filter { it !in saved }
    }

    suspend fun setStyle(context: Context, style: MochyLyricsStyle) {
        context.dataStore.edit { it[StyleKey] = style.name }
    }

    suspend fun setTextSize(context: Context, sizeSp: Float) {
        context.dataStore.edit { it[TextSizeKey] = sizeSp.coerceIn(18f, 36f) }
    }

    suspend fun setLineSpacing(context: Context, spacing: Float) {
        context.dataStore.edit { it[LineSpacingKey] = spacing.coerceIn(1.0f, 1.7f) }
    }

    suspend fun setSoftLight(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[SoftLightKey] = enabled }
    }

    suspend fun setAutoTranslate(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[AutoTranslateKey] = enabled }
    }

    suspend fun setTranslateLanguage(context: Context, langCode: String) {
        context.dataStore.edit { it[TranslateLanguageKey] = langCode }
    }

    suspend fun setAutoRomanize(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[AutoRomanizeKey] = enabled }
    }

    suspend fun setProviderOrder(context: Context, order: List<String>) {
        val clean = order.filter { it in DefaultProviderOrder }.distinct()
        val full = clean + DefaultProviderOrder.filter { it !in clean }
        context.dataStore.edit { it[ProviderOrderKey] = full.joinToString(",") }
    }
}
