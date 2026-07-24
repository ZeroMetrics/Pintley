package com.example.pintly

import android.content.Context

/**
 * Per-category preferences: whether a category is enabled, and its weight (its rough
 * percentage share of the deck). Defaults come from [Category.defaultWeight]. Backed
 * by SharedPreferences so choices persist between sessions.
 */
object CategorySettings {

    private const val PREFS = "pintley_prefs"
    private const val KEY_ENABLED = "enabled_"
    private const val KEY_WEIGHT = "weight_"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context, name: String): Boolean =
        prefs(context).getBoolean(KEY_ENABLED + name, true)

    fun weight(context: Context, name: String, default: Int): Int =
        prefs(context).getInt(KEY_WEIGHT + name, default)

    /** Persist a full set of choices at once. */
    fun save(context: Context, choices: List<Choice>) {
        prefs(context).edit().apply {
            choices.forEach { c ->
                putBoolean(KEY_ENABLED + c.name, c.enabled)
                putInt(KEY_WEIGHT + c.name, c.weight)
            }
        }.apply()
    }

    /** Wipe overrides so everything returns to the built-in defaults. */
    fun resetToDefaults(context: Context) {
        prefs(context).edit().clear().apply()
    }

    data class Choice(val name: String, val enabled: Boolean, val weight: Int)
}
