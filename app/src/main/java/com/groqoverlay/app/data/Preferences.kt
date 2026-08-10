package com.groqoverlay.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf

object PrefsKeys {
    const val GROQ_MODEL = "groq_model"
    const val SYSTEM_PROMPT = "system_prompt"
    const val THEME_MODE = "theme_mode"
    const val ACCENT_COLOR = "accent_color"
    const val DYNAMIC_COLORS = "dynamic_colors"
    const val USE_GRADIENT = "use_gradient"
    const val ANIMATIONS = "animations"
    const val OVERLAY_OPACITY = "overlay_opacity"
    const val OVERLAY_FONT_SIZE = "overlay_font_size"
    const val OVERLAY_CORNER_RADIUS = "overlay_corner_radius"
    const val OVERLAY_BLUR_BG = "overlay_blur_bg"
    const val DEFAULT_MODEL = "llama-3.1-8b-instant"

    val MODELS = listOf(
        "llama-3.1-8b-instant",
        "llama-3.3-70b-versatile",
        "deepseek-r1-distill-llama-70b",
        "gemma2-9b-it",
        "mixtral-8x7b-32768"
    )
}

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val secure = SecurePreferences(context)
        val groqKey = mutableStateOf(secure.getGroqKey())
    val groqModel = mutableStateOf(prefs.getString(PrefsKeys.GROQ_MODEL, PrefsKeys.DEFAULT_MODEL) ?: PrefsKeys.DEFAULT_MODEL)
    val systemPrompt = mutableStateOf(prefs.getString(PrefsKeys.SYSTEM_PROMPT, "") ?: "")
    val themeMode = mutableStateOf(prefs.getString(PrefsKeys.THEME_MODE, "auto") ?: "auto")
    val accentColor = mutableStateOf(prefs.getString(PrefsKeys.ACCENT_COLOR, "blue") ?: "blue")
    val dynamicColors = mutableStateOf(prefs.getBoolean(PrefsKeys.DYNAMIC_COLORS, true))
    val useGradient = mutableStateOf(prefs.getBoolean(PrefsKeys.USE_GRADIENT, true))
    val animations = mutableStateOf(prefs.getBoolean(PrefsKeys.ANIMATIONS, true))
    val overlayOpacity = mutableStateOf(prefs.getInt(PrefsKeys.OVERLAY_OPACITY, 95))
    val overlayFontSize = mutableStateOf(prefs.getInt(PrefsKeys.OVERLAY_FONT_SIZE, 14))
    val overlayCornerRadius = mutableStateOf(prefs.getInt(PrefsKeys.OVERLAY_CORNER_RADIUS, 20))
    val overlayBlurBg = mutableStateOf(prefs.getBoolean(PrefsKeys.OVERLAY_BLUR_BG, false))

    fun saveGroqKey(value: String) { groqKey.value = value; secure.setGroqKey(value) }
    fun saveGroqModel(value: String) { groqModel.value = value; prefs.edit().putString(PrefsKeys.GROQ_MODEL, value).apply() }
    fun saveSystemPrompt(value: String) { systemPrompt.value = value; prefs.edit().putString(PrefsKeys.SYSTEM_PROMPT, value).apply() }
    fun saveThemeMode(value: String) { themeMode.value = value; prefs.edit().putString(PrefsKeys.THEME_MODE, value).apply() }
    fun saveAccentColor(value: String) { accentColor.value = value; prefs.edit().putString(PrefsKeys.ACCENT_COLOR, value).apply() }
    fun saveDynamicColors(value: Boolean) { dynamicColors.value = value; prefs.edit().putBoolean(PrefsKeys.DYNAMIC_COLORS, value).apply() }
    fun saveUseGradient(value: Boolean) { useGradient.value = value; prefs.edit().putBoolean(PrefsKeys.USE_GRADIENT, value).apply() }
    fun saveAnimations(value: Boolean) { animations.value = value; prefs.edit().putBoolean(PrefsKeys.ANIMATIONS, value).apply() }
    fun saveOverlayOpacity(value: Int) { overlayOpacity.value = value; prefs.edit().putInt(PrefsKeys.OVERLAY_OPACITY, value).apply() }
    fun saveOverlayFontSize(value: Int) { overlayFontSize.value = value; prefs.edit().putInt(PrefsKeys.OVERLAY_FONT_SIZE, value).apply() }
    fun saveOverlayCornerRadius(value: Int) { overlayCornerRadius.value = value; prefs.edit().putInt(PrefsKeys.OVERLAY_CORNER_RADIUS, value).apply() }
    fun saveOverlayBlurBg(value: Boolean) { overlayBlurBg.value = value; prefs.edit().putBoolean(PrefsKeys.OVERLAY_BLUR_BG, value).apply() }
}
