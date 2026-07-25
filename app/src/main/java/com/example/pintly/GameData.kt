package com.example.pintly

import android.content.Context
import androidx.annotation.ColorRes

/**
 * A single tile. Directives can be written at the start of a line in the tile files:
 *
 *     [requires: Power] All Powers are out of play      <- only after a Power has come up
 *     [clears: Power]   All Powers are out of play      <- removes Powers from "What's in play"
 *     [transient]       Everyone drinks their ghost count <- a one-off, don't list it
 *
 * Several categories mean "any one of these will do".
 */
data class Prompt(
    val text: String,
    val requires: List<String> = emptyList(),
    val clears: List<String> = emptyList(),
    val transient: Boolean = false,
    /** "1"/"2"/"3" to give ghosts to the named players, "clear", or "clearall". */
    val ghosts: String? = null
)

/**
 * A category of prompts ("tiles"). [defaultWeight] is the category's default share
 * (roughly a percentage; the non-Ultra defaults sum to 100). [info] is shown by the
 * ⓘ badge. [prompts] are loaded at runtime from a plain-text file in assets/tiles/.
 */
data class Category(
    val name: String,
    @ColorRes val colorRes: Int,
    val defaultWeight: Int,
    val info: String,
    val prompts: List<Prompt>,
    /** Ultra categories are drawn on a separate rare roll, not the weighted pool. */
    val isUltra: Boolean = false,
    /** Lasting effects (rules, powers, weaknesses, ghosts) shown in "What's in play". */
    val isPersistent: Boolean = false
)

/**
 * Game content.
 *
 * The prompt text for each category lives in **app/src/main/assets/tiles/<name>.txt**
 * — one prompt per line. That's deliberately easy to contribute to: no code, no build
 * knowledge, and no escaping needed. Blank lines and lines starting with `#` are
 * ignored. Use "Player X", "Player Y", "Player Z" as placeholders — they're replaced
 * with random player names at runtime (see [MainActivity]).
 *
 * To add a whole new category: add a CategoryMeta entry below (with a colour in
 * colors.xml) and a matching text file in assets/tiles/.
 */
object GameData {

    private data class CategoryMeta(
        val name: String,
        @ColorRes val colorRes: Int,
        val defaultWeight: Int,
        val info: String,
        val asset: String,
        val isUltra: Boolean = false,
        val isPersistent: Boolean = false
    )

    private val META = listOf(
        CategoryMeta("Regular", R.color.LightPurple, 43, "If no one is named, ask everyone the question", "tiles/regular.txt"),
        CategoryMeta("Action", R.color.LightRed, 18, "This is a task for whoever is named", "tiles/action.txt"),
        CategoryMeta("Rule", R.color.Gold, 4, "Rules affect everyone and stay in place until stated otherwise", "tiles/rule.txt", isPersistent = true),
        CategoryMeta("Weakness", R.color.SicklyGreen, 4, "This is a penalty that a single player holds until stated otherwise", "tiles/weakness.txt", isPersistent = true),
        CategoryMeta("Special", R.color.Violet, 3, "A player may use this once, whenever they want", "tiles/special.txt", isPersistent = true),
        CategoryMeta("Wild", R.color.ForestGreen, 1, "These are up to you, have some fun", "tiles/wild.txt"),
        CategoryMeta("Democracy", R.color.LightBlue, 13, "Everyone close your eyes and point to who you think it is. The minority drinks", "tiles/democracy.txt"),
        CategoryMeta("Power", R.color.CrimsonRed, 3, "This is a benefit that a single player holds until stated otherwise", "tiles/power.txt", isPersistent = true),
        CategoryMeta("Ghosts", R.color.Spectral, 4, "Each ghost adds +1 to every drink that player takes. They stack up until something banishes them", "tiles/ghosts.txt", isPersistent = true),
        CategoryMeta("Elimination", R.color.Granite, 2, "Remove something from play. Feel free to do this freely, not just on these cards", "tiles/elimination.txt"),
        CategoryMeta("Would You Rather", R.color.Flamingo, 5, "Everyone close your eyes. Thumbs up for option 1, thumbs down for option 2. The minority drinks", "tiles/would_you_rather.txt"),
        CategoryMeta("Ultra Challenge", R.color.UltraDark, 1, "The rarest card of all. Two players go head to head — the loser downs their drink", "tiles/ultra_challenge.txt", isUltra = true)
    )

    fun categories(context: Context): List<Category> = META.map { m ->
        Category(
            m.name, m.colorRes, m.defaultWeight, m.info,
            loadPrompts(context, m.asset), m.isUltra, m.isPersistent
        )
    }

    private val DIRECTIVE = Regex("""^\[(\w+)\s*:?\s*([^\]]*)\]\s*""", RegexOption.IGNORE_CASE)

    private fun loadPrompts(context: Context, asset: String): List<Prompt> = try {
        context.assets.open(asset).bufferedReader().useLines { lines ->
            lines.map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .map { parseLine(it) }
                .toList()
        }
    } catch (e: Exception) {
        emptyList()
    }

    private fun parseLine(line: String): Prompt {
        var rest = line
        val requires = mutableListOf<String>()
        val clears = mutableListOf<String>()
        var transient = false
        var ghosts: String? = null

        while (true) {
            val match = DIRECTIVE.find(rest) ?: break
            val raw = match.groupValues[2].trim()
            val values = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            when (match.groupValues[1].lowercase()) {
                "requires" -> requires.addAll(values)
                "clears" -> clears.addAll(values)
                "transient" -> transient = true
                "ghosts" -> ghosts = raw
                // Unknown directive: leave the line untouched rather than mangle it.
                else -> return Prompt(rest, requires, clears, transient, ghosts)
            }
            rest = rest.substring(match.range.last + 1).trim()
        }
        return Prompt(rest.trim(), requires, clears, transient, ghosts)
    }
}
