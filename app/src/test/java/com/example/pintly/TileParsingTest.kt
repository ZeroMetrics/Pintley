package com.example.pintly

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The tag parsing that turns a line of a tile file into a [Prompt]. */
class TileParsingTest {

    @Test
    fun `a plain line is left as it is`() {
        val prompt = GameData.parseLine("Player X, drink 2")
        assertEquals("Player X, drink 2", prompt.text)
        assertTrue(prompt.requires.isEmpty())
        assertFalse(prompt.transient)
        assertFalse(prompt.haunted)
        assertEquals(null, prompt.ghosts)
    }

    @Test
    fun `tags are stripped off the front`() {
        val prompt = GameData.parseLine("[ghosts: 2] Player X, you are haunted. Take 2 ghosts")
        assertEquals("Player X, you are haunted. Take 2 ghosts", prompt.text)
        assertEquals("2", prompt.ghosts)
    }

    @Test
    fun `several tags can be stacked`() {
        val prompt = GameData.parseLine("[haunted][transient][ghosts: clear] Player X, you have been cleansed")
        assertEquals("Player X, you have been cleansed", prompt.text)
        assertTrue(prompt.haunted)
        assertTrue(prompt.transient)
        assertEquals("clear", prompt.ghosts)
    }

    @Test
    fun `requires and clears take a list`() {
        val prompt = GameData.parseLine("[requires: Rule, Power][clears: Power] All Powers are out of play")
        assertEquals(listOf("Rule", "Power"), prompt.requires)
        assertEquals(listOf("Power"), prompt.clears)
        assertEquals("All Powers are out of play", prompt.text)
    }

    @Test
    fun `the banishing card parses whole`() {
        val prompt =
            GameData.parseLine("[haunted][clears: Ghosts][ghosts: clearall] Sunrise! Every ghost is banished from the game")
        assertTrue(prompt.haunted)
        assertEquals(listOf("Ghosts"), prompt.clears)
        assertEquals("clearall", prompt.ghosts)
        assertEquals("Sunrise! Every ghost is banished from the game", prompt.text)
    }

    @Test
    fun `an unknown tag is left in the text rather than mangled`() {
        val prompt = GameData.parseLine("[wibble] Player X, drink 2")
        assertEquals("[wibble] Player X, drink 2", prompt.text)
    }
}
