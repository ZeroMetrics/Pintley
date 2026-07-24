package com.example.pintly

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.pintly.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** A single drawn tile: the resolved prompt plus the category it came from. */
    private data class Tile(val message: String, val category: Category)

    /** Working copy of the deck. Prompts are removed as they are drawn (no repeats). */
    private data class MutableCategory(val category: Category, val remaining: MutableList<String>)
    private lateinit var deck: MutableList<MutableCategory>

    /** History of everything shown so far, so Back/Next can move through it. */
    private val history = mutableListOf<Tile>()
    private var historyIndex = -1

    private val playerNames: List<String> by lazy {
        @Suppress("DEPRECATION")
        (intent.getStringArrayListExtra("names") ?: arrayListOf()).toList()
    }

    private var currentColor = Color.TRANSPARENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableImmersiveMode()

        deck = GameData.categories()
            .map { MutableCategory(it, it.prompts.toMutableList()) }
            .toMutableList()

        currentColor = ContextCompat.getColor(this, R.color.LightPurple)

        binding.nextArea.setOnClickListener { onNext() }
        binding.backArea.setOnClickListener { onBack() }
        binding.tileTypeLayout.setOnClickListener { showCategoryInfo() }
    }

    private fun onNext() {
        // If we have stepped back, move forward through existing history first.
        if (historyIndex < history.lastIndex) {
            historyIndex++
            display(history[historyIndex])
            return
        }

        val category = pickWeightedCategory()
        if (category == null) {
            binding.tileTypeText.text = ""
            binding.tileTypeIcon.visibility = View.GONE
            binding.messageText.text = getString(R.string.completed)
            return
        }

        val rawPrompt = category.remaining.removeAt(category.remaining.indices.random())
        val tile = Tile(substituteNames(rawPrompt), category.category)
        history.add(tile)
        historyIndex = history.lastIndex
        display(tile)
    }

    private fun onBack() {
        if (historyIndex > 0) {
            historyIndex--
            display(history[historyIndex])
        }
    }

    private fun display(tile: Tile) {
        binding.tileTypeIcon.visibility = View.VISIBLE
        binding.tileTypeText.text = tile.category.name
        binding.messageText.text = tile.message

        val target = ContextCompat.getColor(this, tile.category.colorRes)
        if (target != currentColor) {
            ObjectAnimator.ofObject(
                binding.BackgroundLayout,
                "backgroundColor",
                ArgbEvaluator(),
                currentColor,
                target
            ).apply {
                duration = 320
                start()
            }
        } else {
            binding.BackgroundLayout.setBackgroundColor(target)
        }
        currentColor = target

        // Gentle fade-in for the new prompt.
        binding.messageText.alpha = 0f
        binding.messageText.animate().alpha(1f).setDuration(260).start()
    }

    private fun showCategoryInfo() {
        val category = history.getOrNull(historyIndex)?.category ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(category.name)
            .setMessage(category.info)
            .setPositiveButton(R.string.got_it, null)
            .show()
    }

    /** Weighted pick among categories that still have prompts left. */
    private fun pickWeightedCategory(): MutableCategory? {
        val available = deck.filter { it.remaining.isNotEmpty() }
        if (available.isEmpty()) return null
        val totalWeight = available.sumOf { it.category.weight }
        var roll = (0 until totalWeight).random()
        for (item in available) {
            roll -= item.category.weight
            if (roll < 0) return item
        }
        return available.last()
    }

    /** Replace "Player X/Y/Z" with real names, shuffled fresh for each draw. */
    private fun substituteNames(message: String): String {
        if (playerNames.isEmpty()) return message
        val shuffled = playerNames.shuffled()
        val placeholders = listOf("Player X", "Player Y", "Player Z")
        var result = message
        placeholders.forEachIndexed { index, placeholder ->
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, shuffled[index % shuffled.size])
            }
        }
        return result
    }

    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            enableImmersiveMode()
        }
    }
}
