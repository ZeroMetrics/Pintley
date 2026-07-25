package com.example.pintly

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.pintly.databinding.ActivityMainBinding
import com.example.pintly.databinding.DialogPlayersBinding
import com.example.pintly.databinding.ItemPlayerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private companion object {
        /** Chance of an Ultra Challenge on any given card (~once per game). */
        const val ULTRA_CHANCE = 0.005
    }

    private lateinit var binding: ActivityMainBinding

    /** A single drawn tile: the resolved prompt plus the category it came from. */
    private data class Tile(val message: String, val category: Category)

    /** Working copy of the deck. Prompts are removed as they are drawn (no repeats). */
    private data class MutableCategory(
        val category: Category,
        val remaining: MutableList<String>,
        val weight: Int
    )
    private lateinit var deck: MutableList<MutableCategory>

    /** History of everything shown so far, so Back/Next can move through it. */
    private val history = mutableListOf<Tile>()
    private var historyIndex = -1

    /** Live player list — can be edited mid-game without resetting the deck. */
    private val players = mutableListOf<String>()

    private var currentColor = Color.TRANSPARENT
    private var backgroundIsGradient = false
    private var bgAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableImmersiveMode()
        SoundManager.init(this)

        @Suppress("DEPRECATION")
        players.addAll(intent.getStringArrayListExtra("names") ?: arrayListOf())

        buildDeck()
        resetToStart()

        binding.nextArea.setOnClickListener { onNext() }
        binding.backArea.setOnClickListener { onBack() }
        binding.tileTypeLayout.setOnClickListener { SoundManager.playPop(); showCategoryInfo() }
        binding.playersButton.setOnClickListener { SoundManager.playPop(); showPlayersDialog() }
    }

    private fun buildDeck() {
        // Honour the player's category choices; distinct() drops exact duplicate
        // prompts so no card is ever seen twice.
        deck = GameData.categories()
            .filter { CategorySettings.isEnabled(this, it.name) }
            .map {
                MutableCategory(
                    it,
                    it.prompts.distinct().toMutableList(),
                    CategorySettings.weight(this, it.name, it.defaultWeight)
                )
            }
            .toMutableList()
    }

    private fun resetToStart() {
        history.clear()
        historyIndex = -1
        bgAnimator?.cancel()
        bgAnimator = null
        currentColor = ContextCompat.getColor(this, R.color.LightPurple)
        binding.BackgroundLayout.setBackgroundColor(currentColor)
        backgroundIsGradient = false
        binding.tileTypeText.text = ""
        binding.tileTypeIcon.visibility = View.GONE
        binding.messageText.text = getString(R.string.begin_hint)
        binding.messageText.alpha = 1f
        binding.hintLeft.animate().alpha(0.35f).setDuration(200).start()
        binding.hintRight.animate().alpha(0.45f).setDuration(200).start()
    }

    private fun onNext() {
        fadeHintsAway()

        // If we have stepped back, move forward through existing history first.
        if (historyIndex < history.lastIndex) {
            SoundManager.playPop()
            historyIndex++
            display(history[historyIndex], isNew = false)
            return
        }

        val category = pickNextCategory()
        if (category == null) {
            SoundManager.playPop()
            binding.tileTypeText.text = ""
            binding.tileTypeIcon.visibility = View.GONE
            binding.messageText.text = getString(R.string.completed)
            return
        }

        // Ultra plays its own grander sound in the reveal.
        if (!category.category.isUltra) SoundManager.playPop()

        val rawPrompt = category.remaining.removeAt(category.remaining.indices.random())
        val tile = Tile(substituteNames(rawPrompt), category.category)
        history.add(tile)
        historyIndex = history.lastIndex
        display(tile, isNew = true)
    }

    private fun onBack() {
        if (historyIndex > 0) {
            SoundManager.playPop()
            historyIndex--
            display(history[historyIndex], isNew = false)
        }
    }

    /**
     * @param isNew true for a freshly drawn card (plays the full reveal), false when
     * revisiting a card via Back/Next (just shows it, no sound/flash re-fire).
     */
    private fun display(tile: Tile, isNew: Boolean) {
        binding.tileTypeIcon.visibility = View.VISIBLE
        binding.tileTypeText.text = tile.category.name
        binding.messageText.text = tile.message

        if (tile.category.isUltra) {
            showUltraBackground(isNew)
        } else {
            showNormalBackground(ContextCompat.getColor(this, tile.category.colorRes))
        }
    }

    private fun showNormalBackground(target: Int) {
        // Cancel any in-flight crossfade so it can't overwrite the new background.
        bgAnimator?.cancel()
        bgAnimator = null

        if (backgroundIsGradient || target == currentColor) {
            binding.BackgroundLayout.setBackgroundColor(target)
            backgroundIsGradient = false
        } else {
            bgAnimator = ObjectAnimator.ofObject(
                binding.BackgroundLayout,
                "backgroundColor",
                ArgbEvaluator(),
                currentColor,
                target
            ).apply {
                duration = 320
                start()
            }
        }
        currentColor = target

        binding.tileTypeLayout.apply { scaleX = 1f; scaleY = 1f }
        binding.messageText.apply {
            scaleX = 1f
            scaleY = 1f
            alpha = 0f
            animate().alpha(1f).setDuration(260).start()
        }
    }

    /** Show the Ultra gradient. [isNew] adds the full reveal (flash, sound, haptic, pop-in). */
    private fun showUltraBackground(isNew: Boolean) {
        bgAnimator?.cancel()
        bgAnimator = null
        binding.BackgroundLayout.setBackgroundResource(R.drawable.bg_ultra_gradient)
        backgroundIsGradient = true
        currentColor = ContextCompat.getColor(this, R.color.ultra_blue)

        if (isNew) {
            vibrate()
            SoundManager.playUltra()
            binding.flashOverlay.apply {
                alpha = 0.9f
                animate().alpha(0f).setDuration(650).start()
            }
            binding.messageText.apply {
                alpha = 0f
                scaleX = 0.5f
                scaleY = 0.5f
                animate().alpha(1f).scaleX(1f).scaleY(1f)
                    .setInterpolator(OvershootInterpolator())
                    .setDuration(520).start()
            }
            binding.tileTypeLayout.apply {
                scaleX = 0.7f
                scaleY = 0.7f
                animate().scaleX(1f).scaleY(1f).setDuration(420).start()
            }
        } else {
            binding.tileTypeLayout.apply { scaleX = 1f; scaleY = 1f }
            binding.messageText.apply {
                scaleX = 1f
                scaleY = 1f
                alpha = 0f
                animate().alpha(1f).setDuration(260).start()
            }
        }
    }

    private fun vibrate() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(150)
            }
        } catch (_: Exception) {
            // Device may have no vibrator — ignore.
        }
    }

    private fun fadeHintsAway() {
        if (binding.hintRight.alpha > 0f) {
            binding.hintLeft.animate().alpha(0f).setDuration(220).start()
            binding.hintRight.animate().alpha(0f).setDuration(220).start()
        }
    }

    private fun showCategoryInfo() {
        val category = history.getOrNull(historyIndex)?.category ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(category.name)
            .setMessage(category.info)
            .setPositiveButton(R.string.got_it, null)
            .show()
    }

    // ---- Manage players (mid-game) ------------------------------------------

    private fun showPlayersDialog() {
        val dialogBinding = DialogPlayersBinding.inflate(layoutInflater)
        val container = dialogBinding.dialogPlayersContainer

        players.forEach { addPlayerRow(container, it) }
        if (players.isEmpty()) addPlayerRow(container, "")

        dialogBinding.dialogAddButton.setOnClickListener {
            SoundManager.playPop()
            addPlayerRow(container, "", focus = true)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.players_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.done) { _, _ ->
                val updated = collectNames(container)
                if (updated.isNotEmpty()) {
                    players.clear()
                    players.addAll(updated)
                    Snackbar.make(binding.root, R.string.players_updated, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
            .setNeutralButton(R.string.new_game) { _, _ -> confirmNewGame() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun addPlayerRow(container: ViewGroup, name: String, focus: Boolean = false) {
        val row = ItemPlayerBinding.inflate(layoutInflater, container, false)
        row.playerName.setText(name)
        row.removeButton.setOnClickListener {
            if (container.childCount > 1) container.removeView(row.root) else row.playerName.text = null
        }
        container.addView(row.root)
        if (focus) row.playerName.requestFocus()
    }

    private fun collectNames(container: ViewGroup): List<String> {
        val names = mutableListOf<String>()
        for (i in 0 until container.childCount) {
            val field = container.getChildAt(i).findViewById<EditText>(R.id.player_name)
            val name = field.text.toString().trim()
            if (name.isNotBlank()) names.add(name)
        }
        return names
    }

    private fun confirmNewGame() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.new_game_confirm_title)
            .setMessage(R.string.new_game_confirm_message)
            .setPositiveButton(R.string.new_game) { _, _ ->
                buildDeck()
                resetToStart()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ---- Helpers ------------------------------------------------------------

    /** Roll for a rare Ultra Challenge first, otherwise a normal weighted card. */
    private fun pickNextCategory(): MutableCategory? {
        val ultra = deck.firstOrNull { it.category.isUltra && it.remaining.isNotEmpty() }
        if (ultra != null && Random.nextDouble() < ULTRA_CHANCE) return ultra
        return pickWeightedCategory()
    }

    /** Weighted pick among ordinary (non-Ultra) categories that still have prompts left. */
    private fun pickWeightedCategory(): MutableCategory? {
        val available = deck.filter { it.remaining.isNotEmpty() && !it.category.isUltra }
        if (available.isEmpty()) return null
        val totalWeight = available.sumOf { it.weight }
        var roll = (0 until totalWeight).random()
        for (item in available) {
            roll -= item.weight
            if (roll < 0) return item
        }
        return available.last()
    }

    /** Replace "Player X/Y/Z" with real names, shuffled fresh for each draw. */
    private fun substituteNames(message: String): String {
        if (players.isEmpty()) return message
        val shuffled = players.shuffled()
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
