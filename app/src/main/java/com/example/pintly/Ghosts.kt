package com.example.pintly

/**
 * The ghost bookkeeping, kept out of [MainActivity] so it can be tested on its own.
 *
 * Totals are never held as a running count. They are worked out by replaying the cards
 * played so far, the same way "What's in play" is rebuilt — so stepping Back really does
 * un-do a ghost card. A running total could not manage that, because "clear" and
 * "clearall" throw away the very numbers you would need to put things back.
 */
object Ghosts {

    /** One card's ghost effect, and the players it was resolved against. */
    data class Effect(val kind: String?, val targets: List<String>)

    /**
     * A correction made by hand with the +/- buttons. [after] is how many cards had been
     * played when it was made, so a later "clearall" wipes it like any other ghost.
     */
    data class Adjustment(val after: Int, val player: String, val delta: Int)

    // Both braces escaped — Android's ICU regex engine rejects a bare closing brace.
    private val TOKEN = Regex("""\{(\d+)\}""")

    /**
     * Ghost totals after the first [played] cards, with [adjustments] folded in at the
     * points they were made. Players on zero are left out.
     */
    fun replay(
        effects: List<Effect>,
        adjustments: List<Adjustment>,
        played: Int
    ): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        // Step 0 is the opening screen, before any card — corrections can live there too.
        for (step in 0..played) {
            if (step > 0) apply(counts, effects[step - 1])
            adjustments.filter { it.after == step }
                .forEach { set(counts, it.player, (counts[it.player] ?: 0) + it.delta) }
        }
        return counts
    }

    /**
     * Numbers written as {2} in a tile are drinks the named player takes, so their ghosts
     * get added: 2 drinks with 2 ghosts shows as 4. Untagged numbers (push-ups, seconds,
     * drinks handed to other people) are deliberately left alone.
     */
    fun applyMath(text: String, bonus: Int): String =
        TOKEN.replace(text) { m -> (m.groupValues[1].toInt() + bonus).toString() }

    private fun apply(counts: MutableMap<String, Int>, effect: Effect) {
        // distinct(): with few players the same name can fill both X and Y, and nobody
        // should be haunted twice by one card.
        val targets = effect.targets.distinct()
        when (val kind = effect.kind?.lowercase()) {
            null -> return
            "clearall" -> counts.clear()
            "clear" -> targets.forEach { counts.remove(it) }
            "transfer" -> if (targets.size >= 2) {
                val held = counts[targets[0]] ?: 0
                if (held > 0) {
                    set(counts, targets[0], held - 1)
                    set(counts, targets[1], (counts[targets[1]] ?: 0) + 1)
                }
            }
            "swap" -> if (targets.size >= 2) {
                val first = counts[targets[0]] ?: 0
                val second = counts[targets[1]] ?: 0
                set(counts, targets[0], second)
                set(counts, targets[1], first)
            }
            else -> kind.toIntOrNull()?.let { amount ->
                targets.forEach { set(counts, it, (counts[it] ?: 0) + amount) }
            }
        }
    }

    private fun set(counts: MutableMap<String, Int>, player: String, count: Int) {
        if (count <= 0) counts.remove(player) else counts[player] = count
    }
}
