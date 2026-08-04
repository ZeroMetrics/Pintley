package com.example.pintly

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The ghost bookkeeping is plain Kotlin with no Android in it, so it can be checked here
 * rather than on a device.
 */
class GhostsTest {

    private fun gain(amount: Int, vararg who: String) = Ghosts.Effect(amount.toString(), who.toList())

    private fun replay(vararg effects: Ghosts.Effect) =
        Ghosts.replay(effects.toList(), emptyList(), effects.size)

    @Test
    fun `a gain card haunts everyone it names`() {
        assertEquals(mapOf("Ada" to 1, "Bo" to 1), replay(gain(1, "Ada", "Bo")))
    }

    @Test
    fun `ghosts stack up`() {
        assertEquals(mapOf("Ada" to 5), replay(gain(2, "Ada"), gain(3, "Ada")))
    }

    @Test
    fun `one name filling both placeholders only counts once`() {
        assertEquals(mapOf("Ada" to 1), replay(gain(1, "Ada", "Ada")))
    }

    @Test
    fun `clear empties the named player only`() {
        val counts = replay(gain(2, "Ada"), gain(1, "Bo"), Ghosts.Effect("clear", listOf("Ada")))
        assertEquals(mapOf("Bo" to 1), counts)
    }

    @Test
    fun `clearall empties everyone`() {
        val counts = replay(gain(2, "Ada"), gain(1, "Bo"), Ghosts.Effect("clearall", emptyList()))
        assertEquals(emptyMap<String, Int>(), counts)
    }

    @Test
    fun `transfer moves one ghost`() {
        val counts = replay(gain(2, "Ada"), Ghosts.Effect("transfer", listOf("Ada", "Bo")))
        assertEquals(mapOf("Ada" to 1, "Bo" to 1), counts)
    }

    @Test
    fun `transfer does nothing when the giver has none`() {
        val counts = replay(gain(1, "Bo"), Ghosts.Effect("transfer", listOf("Ada", "Bo")))
        assertEquals(mapOf("Bo" to 1), counts)
    }

    @Test
    fun `swap exchanges the two counts`() {
        val counts = replay(gain(3, "Ada"), gain(1, "Bo"), Ghosts.Effect("swap", listOf("Ada", "Bo")))
        assertEquals(mapOf("Ada" to 1, "Bo" to 3), counts)
    }

    @Test
    fun `players on zero are left out`() {
        val counts = replay(gain(1, "Ada"), Ghosts.Effect("transfer", listOf("Ada", "Bo")))
        assertEquals(mapOf("Bo" to 1), counts)
    }

    @Test
    fun `an ordinary card leaves the totals alone`() {
        assertEquals(mapOf("Ada" to 1), replay(gain(1, "Ada"), Ghosts.Effect(null, emptyList())))
    }

    // ---- Stepping back through the game ------------------------------------

    @Test
    fun `stepping back gives the totals from that point`() {
        val cards = listOf(gain(1, "Ada"), gain(2, "Ada"), Ghosts.Effect("clearall", emptyList()))
        assertEquals(emptyMap<String, Int>(), Ghosts.replay(cards, emptyList(), 0))
        assertEquals(mapOf("Ada" to 1), Ghosts.replay(cards, emptyList(), 1))
        assertEquals(mapOf("Ada" to 3), Ghosts.replay(cards, emptyList(), 2))
        assertEquals(emptyMap<String, Int>(), Ghosts.replay(cards, emptyList(), 3))
    }

    @Test
    fun `stepping back un-does a clearall, which a running total could not`() {
        val cards = listOf(gain(2, "Ada"), Ghosts.Effect("clearall", emptyList()))
        assertEquals(mapOf("Ada" to 2), Ghosts.replay(cards, emptyList(), 1))
    }

    // ---- Corrections made by hand ------------------------------------------

    @Test
    fun `a correction is folded in`() {
        val counts = Ghosts.replay(
            listOf(gain(1, "Ada")),
            listOf(Ghosts.Adjustment(after = 1, player = "Ada", delta = 2)),
            1
        )
        assertEquals(mapOf("Ada" to 3), counts)
    }

    @Test
    fun `a correction made before any card is played still counts`() {
        val counts = Ghosts.replay(
            emptyList(),
            listOf(Ghosts.Adjustment(after = 0, player = "Ada", delta = 1)),
            0
        )
        assertEquals(mapOf("Ada" to 1), counts)
    }

    @Test
    fun `a clearall wipes a correction made before it`() {
        val counts = Ghosts.replay(
            listOf(gain(1, "Ada"), Ghosts.Effect("clearall", emptyList())),
            listOf(Ghosts.Adjustment(after = 1, player = "Ada", delta = 2)),
            2
        )
        assertEquals(emptyMap<String, Int>(), counts)
    }

    @Test
    fun `a correction after a clearall survives it`() {
        val counts = Ghosts.replay(
            listOf(gain(1, "Ada"), Ghosts.Effect("clearall", emptyList())),
            listOf(Ghosts.Adjustment(after = 2, player = "Ada", delta = 2)),
            2
        )
        assertEquals(mapOf("Ada" to 2), counts)
    }

    // ---- Ghost-adjusted drinks ---------------------------------------------

    @Test
    fun `braced numbers gain the players ghosts`() {
        assertEquals("Ada, drink 4", Ghosts.applyMath("Ada, drink {2}", 2))
    }

    @Test
    fun `braces come off even with no ghosts`() {
        assertEquals("Ada, drink 2", Ghosts.applyMath("Ada, drink {2}", 0))
    }

    @Test
    fun `plain numbers are left alone`() {
        assertEquals(
            "Ada, do 20 push-ups or drink 5",
            Ghosts.applyMath("Ada, do 20 push-ups or drink {3}", 2)
        )
    }

    @Test
    fun `a zero token renders as the ghost count`() {
        assertEquals("Ada, drink 3", Ghosts.applyMath("Ada, drink {0}", 3))
    }
}
