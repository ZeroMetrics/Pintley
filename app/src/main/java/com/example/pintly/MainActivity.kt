package com.example.pintly

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.pintly.databinding.ActivityMainBinding
import com.example.pintly.databinding.DialogInplayBinding
import com.example.pintly.databinding.DialogPlayersBinding
import com.example.pintly.databinding.ItemPlayerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private companion object {
        /** Chance of an Ultra Challenge on any given card (~once per game). */
        const val ULTRA_CHANCE = 0.005

        /** How many cards after a bird first appears it comes back for a second player. */
        const val REPEAT_AFTER = 25

        /** Give up looking for a different player rather than spin in a one-player game. */
        const val REPEAT_PLAYER_TRIES = 12
    }

    private lateinit var binding: ActivityMainBinding

    /**
     * A single drawn tile: the resolved prompt plus the category it came from.
     *
     * Bird cards also carry the photo's *filename* — never the Bitmap, since [history]
     * lives for the whole game and holding every decoded photo there would run to
     * hundreds of megabytes.
     */
    private data class Tile(
        val message: String,
        val category: Category,
        val clears: List<String> = emptyList(),
        val transient: Boolean = false,
        /** This card's [Prompt.ghosts] effect, kept so the totals can be replayed. */
        val ghosts: String? = null,
        /** The players that effect was resolved against, for the same reason. */
        val ghostTargets: List<String> = emptyList(),
        /** Asset filename of the bird photo, or null on an ordinary card. */
        val birdImage: String? = null,
        /** The answer, worked out at draw time. */
        val birdName: String? = null,
        /**
         * Mutable on purpose: flipping it on the history entry is what makes a reveal
         * survive Back/Next while [display] stays a pure function of the tile.
         */
        var revealed: Boolean = false
    )

    /** A bird waiting to come back round to quiz someone else. */
    private data class PendingBird(
        /** The unresolved prompt, so names can be drawn again for a different player. */
        val prompt: String,
        val image: String,
        val category: Category,
        val originalPlayer: String?,
        val dueAtDraw: Int
    )

    private val pendingBirds = ArrayDeque<PendingBird>()

    /** Photos not yet used this game, drawn from the end so no bird repeats. */
    private val birdPool = mutableListOf<String>()

    /**
     * Freshly drawn cards only. Identical to `history.size` today, but scheduling off
     * it makes it plain that stepping back and forward cannot shift a repeat.
     */
    private var drawCount = 0

    /** Working copy of the deck. Prompts are removed as they are drawn (no repeats). */
    private data class MutableCategory(
        val category: Category,
        val remaining: MutableList<Prompt>,
        val weight: Int
    )
    private lateinit var deck: MutableList<MutableCategory>

    /** Categories that have already come up this game, for [Prompt.requires] gating. */
    private val seenCategories = mutableSetOf<String>()

    /** History of everything shown so far, so Back/Next can move through it. */
    private val history = mutableListOf<Tile>()
    private var historyIndex = -1

    /** Live player list — can be edited mid-game without resetting the deck. */
    private val players = mutableListOf<String>()

    /**
     * How many ghosts each player is carrying as of the card on screen — each one adds
     * +1 to their drinks. Worked out by [refreshGhosts] rather than kept as a running
     * total, so Back and Next move it in step with everything else.
     */
    private var ghostCounts: Map<String, Int> = emptyMap()

    /** Corrections made by hand with the +/- buttons, in the order they were made. */
    private val ghostAdjustments = mutableListOf<Ghosts.Adjustment>()

    /** A resolved prompt: the text with real names in it, plus who was named. */
    private data class Resolved(val text: String, val named: List<String>)

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
        binding.inPlayButton.setOnClickListener { SoundManager.playPop(); showInPlayDialog() }
    }

    private fun buildDeck() {
        // Honour the player's category choices; distinct() drops exact duplicate
        // prompts so no card is ever seen twice.
        deck = GameData.categories(this)
            .filter { CategorySettings.isEnabled(this, it.name) }
            .map {
                MutableCategory(
                    it,
                    it.prompts.distinct().toMutableList(),
                    CategorySettings.weight(this, it.name, it.defaultWeight)
                )
            }
            .toMutableList()

        birdPool.clear()
        birdPool.addAll(BirdImages.list(this).shuffled())
    }

    private fun resetToStart() {
        history.clear()
        historyIndex = -1
        seenCategories.clear()
        ghostAdjustments.clear()
        pendingBirds.clear()
        drawCount = 0
        clearBirdLayout()
        refreshGhosts()
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

        // On an unrevealed bird this tap is the reveal, not an advance. Keying off
        // what is *on screen* rather than how we got there covers the fresh-draw and
        // Back/Next paths in one. getOrNull(-1) is null on the opening screen.
        val shown = history.getOrNull(historyIndex)
        if (shown?.birdImage != null && !shown.revealed) {
            SoundManager.playPop()
            shown.revealed = true
            display(shown, isNew = false)
            return
        }

        // If we have stepped back, move forward through existing history first.
        if (historyIndex < history.lastIndex) {
            SoundManager.playPop()
            historyIndex++
            refreshGhosts()
            display(history[historyIndex], isNew = false)
            return
        }

        // A due repeat bypasses the weighted pick entirely — at 2% a chance of being
        // picked would turn "25 cards later" into "somewhere in the next 50".
        val due = pendingBirds.firstOrNull()?.takeIf { drawCount + 1 >= it.dueAtDraw }
        if (due != null) {
            pendingBirds.removeFirst()
            SoundManager.playPop()
            showBirdRepeat(due)
            return
        }

        val category = pickNextCategory()
        if (category == null) {
            // Deck's done, but a bird drawn near the end still deserves its second showing.
            val leftover = pendingBirds.removeFirstOrNull()
            if (leftover != null) {
                SoundManager.playPop()
                showBirdRepeat(leftover)
                return
            }
            SoundManager.playPop()
            clearBirdLayout()
            binding.tileTypeText.text = ""
            binding.tileTypeIcon.visibility = View.GONE
            binding.messageText.text = getString(R.string.completed)
            return
        }

        // Ultra plays its own grander sound in the reveal.
        if (!category.category.isUltra) SoundManager.playPop()

        val prompt = availablePrompts(category).random()
        category.remaining.remove(prompt)
        // Mark the category seen only after choosing, so a category's own gated
        // cards can't be unlocked by the very card being drawn.
        seenCategories.add(category.category.name)

        val resolved = substituteNames(prompt.text, prompt.haunted)
        // Ghost maths uses the counts as they stand *before* this card's own effect, so
        // a card that grants ghosts doesn't also inflate its own numbers. That falls out
        // of the ordering here: the effect only lands once the tile is in history and
        // refreshGhosts() replays it.
        val text = applyGhostMath(resolved.text, resolved.named.firstOrNull())

        val bird = if (category.category.isBird) birdPool.removeLastOrNull() else null
        val tile = Tile(
            text, category.category, prompt.clears, prompt.transient,
            ghosts = prompt.ghosts, ghostTargets = resolved.named,
            birdImage = bird, birdName = bird?.let { BirdImages.displayName(it) }
        )
        drawCount++
        history.add(tile)
        historyIndex = history.lastIndex
        refreshGhosts()

        // Scheduling lives only here, in the first-draw path, so a repeat can never
        // reschedule itself.
        if (bird != null) {
            pendingBirds.add(
                PendingBird(
                    prompt.text, bird, category.category,
                    resolved.named.firstOrNull(), drawCount + REPEAT_AFTER
                )
            )
        }

        display(tile, isNew = true)
    }

    /**
     * Show a bird again, unrevealed, for someone other than the player who had it
     * first. It reuses the stored prompt and photo, so it costs the deck nothing.
     */
    private fun showBirdRepeat(pending: PendingBird) {
        var resolved = substituteNames(pending.prompt)
        var tries = 0
        while (pending.originalPlayer != null &&
            resolved.named.firstOrNull() == pending.originalPlayer &&
            tries < REPEAT_PLAYER_TRIES
        ) {
            resolved = substituteNames(pending.prompt)
            tries++
        }

        val tile = Tile(
            applyGhostMath(resolved.text, resolved.named.firstOrNull()),
            pending.category,
            birdImage = pending.image,
            birdName = BirdImages.displayName(pending.image)
        )
        drawCount++
        history.add(tile)
        historyIndex = history.lastIndex
        refreshGhosts()
        display(tile, isNew = true)
    }

    private fun onBack() {
        if (historyIndex > 0) {
            SoundManager.playPop()
            historyIndex--
            refreshGhosts()
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
        // Unconditional, every render: applyBirdLayout writes the hidden case too, so
        // no route through display() can leave a photo stranded on an ordinary card.
        // Never guard this with `if (isBird)` — that is exactly the shape of bug the
        // "Fix Ultra gradient lost on back/forward navigation" commit was about.
        applyBirdLayout(tile)
        binding.messageText.text =
            if (tile.birdImage != null && tile.revealed) tile.birdName else tile.message

        if (tile.category.isUltra) {
            showUltraBackground(isNew)
        } else {
            showNormalBackground(ContextCompat.getColor(this, tile.category.colorRes))
        }
    }

    /**
     * Put the photo, the credit line and the text padding into the right state for
     * [tile] — including the ordinary-card state, where everything is cleared away.
     */
    private fun applyBirdLayout(tile: Tile) {
        val file = tile.birdImage ?: run { clearBirdLayout(); return }

        val metrics = resources.displayMetrics
        // The photo gets half the screen; ask for that so the decode downsamples.
        binding.birdImage.setImageBitmap(
            BirdImages.bitmap(this, file, metrics.widthPixels / 2, metrics.heightPixels)
        )
        binding.birdImage.visibility = View.VISIBLE

        // 64dp each side of a half-width column would squeeze autoSize to its 18sp floor.
        binding.messageText.setPaddingRelative(dp(24), dp(56), dp(40), dp(56))

        // The credit waits for the reveal: tidier, and it can't leak a hint.
        val credit = BirdImages.credit(this, file)
        if (tile.revealed && credit != null) {
            binding.birdCredit.text = credit.toString()
            binding.birdCredit.visibility = View.VISIBLE
        } else {
            binding.birdCredit.text = ""
            binding.birdCredit.visibility = View.GONE
        }
    }

    /** Ordinary-card state: no photo, no credit, the original padding. */
    private fun clearBirdLayout() {
        binding.birdImage.visibility = View.GONE
        binding.birdImage.setImageDrawable(null)
        binding.birdCredit.text = ""
        binding.birdCredit.visibility = View.GONE
        binding.messageText.setPaddingRelative(dp(64), dp(80), dp(64), dp(80))
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

    // ---- What's in play -----------------------------------------------------

    /**
     * Walks the cards played so far and works out which lasting effects are still
     * active — adding persistent cards, and dropping whatever an elimination cleared.
     */
    private fun activeEffects(): Map<Category, List<String>> {
        val active = mutableListOf<Pair<Category, String>>()
        for (i in 0..historyIndex) {
            val tile = history[i]
            if (tile.clears.isNotEmpty()) {
                active.removeAll { (cat, _) -> tile.clears.contains(cat.name) }
                continue
            }
            if (tile.category.isPersistent && !tile.transient) {
                active.add(tile.category to tile.message)
            }
        }
        return active.groupBy({ it.first }, { it.second })
    }

    /** A "Name  -  n  +" row so counts can be corrected by hand. */
    private fun addGhostRow(container: LinearLayout, player: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val label = TextView(this).apply {
            text = player
            textSize = 15f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.ink))
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        }
        val count = TextView(this).apply {
            text = (ghostCounts[player] ?: 0).toString()
            textSize = 16f
            gravity = Gravity.CENTER
            minWidth = dp(36)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.Spectral))
        }
        fun stepper(symbol: String, delta: Int) = TextView(this).apply {
            text = symbol
            textSize = 22f
            gravity = Gravity.CENTER
            minWidth = dp(44)
            setPadding(0, dp(2), 0, dp(6))
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.brand_magenta))
            setOnClickListener {
                // Recorded against the current card rather than applied on the spot, so
                // the replay keeps it in the right place in the running order.
                if ((ghostCounts[player] ?: 0) + delta < 0) return@setOnClickListener
                ghostAdjustments.add(Ghosts.Adjustment(historyIndex + 1, player, delta))
                refreshGhosts()
                count.text = (ghostCounts[player] ?: 0).toString()
            }
        }
        row.addView(label)
        row.addView(stepper("−", -1))
        row.addView(count)
        row.addView(stepper("+", +1))
        container.addView(row)
    }

    private fun showInPlayDialog() {
        val dialogBinding = DialogInplayBinding.inflate(layoutInflater)
        val container = dialogBinding.inPlayContainer
        val effects = activeEffects()

        // Ghost counts, adjustable by hand for the cards the app can't read.
        if (players.isNotEmpty()) {
            container.addView(TextView(this).apply {
                text = getString(R.string.ghosts_heading)
                textSize = 17f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.Spectral))
                setPadding(0, dp(6), 0, dp(2))
            })
            players.forEach { addGhostRow(container, it) }
        }

        if (effects.isEmpty()) {
            container.addView(TextView(this).apply {
                text = getString(R.string.in_play_empty)
                textSize = 15f
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.ink_muted))
            })
        } else {
            effects.forEach { (category, cards) ->
                container.addView(TextView(this).apply {
                    text = category.name
                    textSize = 17f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(this@MainActivity, category.colorRes))
                    setPadding(0, dp(14), 0, dp(4))
                })
                cards.forEach { card ->
                    container.addView(TextView(this).apply {
                        text = "•  $card"
                        textSize = 15f
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.ink))
                        setPadding(0, dp(3), 0, dp(3))
                    })
                }
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.in_play_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.close, null)
            .show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

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
                    // Someone just removed or renamed shouldn't be left haunting the tally.
                    refreshGhosts()
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

    /**
     * Prompts in [mc] that can be drawn right now — i.e. ungated, or gated on a
     * category that has already come up this game.
     *
     * `[haunted]` is checked against live state rather than against [seenCategories],
     * which only ever latches on: after a card banishes every ghost, "lose your ghosts"
     * and "pass a ghost to" have nothing to work with and must drop out again.
     */
    private fun availablePrompts(mc: MutableCategory): List<Prompt> {
        val anyoneHaunted = hauntedPlayers().isNotEmpty()
        return mc.remaining.filter { p ->
            (p.requires.isEmpty() || p.requires.any { seenCategories.contains(it) }) &&
                (!p.haunted || anyoneHaunted)
        }
    }

    /** Roll for a rare Ultra Challenge first, otherwise a normal weighted card. */
    private fun pickNextCategory(): MutableCategory? {
        val ultra = deck.firstOrNull { it.category.isUltra && availablePrompts(it).isNotEmpty() }
        if (ultra != null && Random.nextDouble() < ULTRA_CHANCE) return ultra
        return pickWeightedCategory()
    }

    /**
     * Weighted pick among ordinary (non-Ultra) categories with a drawable prompt.
     *
     * Birds bow out once their photos are used up. That is gated here rather than in
     * [availablePrompts], which should go on meaning "passes the `requires` gate".
     */
    private fun pickWeightedCategory(): MutableCategory? {
        val available = deck.filter {
            !it.category.isUltra &&
                availablePrompts(it).isNotEmpty() &&
                (!it.category.isBird || birdPool.isNotEmpty())
        }
        if (available.isEmpty()) return null
        val totalWeight = available.sumOf { it.weight }
        var roll = (0 until totalWeight).random()
        for (item in available) {
            roll -= item.weight
            if (roll < 0) return item
        }
        return available.last()
    }

    /**
     * Replace "Player X/Y/Z" with real names, shuffled fresh for each draw.
     *
     * [preferHaunted] puts the players carrying ghosts at the front of the queue, so a
     * card that banishes, passes or swaps ghosts lands on someone who has some. Player X
     * is the one that matters — Y and Z fall through to everybody else, which is what
     * "pass a ghost to Player Y" wants.
     */
    private fun substituteNames(message: String, preferHaunted: Boolean = false): Resolved {
        if (players.isEmpty()) return Resolved(message, emptyList())
        val pool = if (preferHaunted) {
            val (haunted, rest) = players.partition { (ghostCounts[it] ?: 0) > 0 }
            haunted.shuffled() + rest.shuffled()
        } else {
            players.shuffled()
        }
        val placeholders = listOf("Player X", "Player Y", "Player Z")
        val named = mutableListOf<String>()
        var result = message
        placeholders.forEachIndexed { index, placeholder ->
            if (result.contains(placeholder)) {
                val name = pool[index % pool.size]
                result = result.replace(placeholder, name)
                named.add(name)
            }
        }
        return Resolved(result, named)
    }

    /** [text] with [player]'s ghosts added to its {n} numbers — see [Ghosts.applyMath]. */
    private fun applyGhostMath(text: String, player: String?): String =
        Ghosts.applyMath(text, player?.let { ghostCounts[it] } ?: 0)

    /**
     * Work the ghost totals out again for the card on screen and redraw the tally. Call
     * this after anything that moves [historyIndex] or adds a correction.
     */
    private fun refreshGhosts() {
        ghostCounts = Ghosts.replay(
            history.map { Ghosts.Effect(it.ghosts, it.ghostTargets) },
            ghostAdjustments,
            historyIndex + 1
        )
        updateGhostTally()
    }

    /**
     * Players still in the game carrying at least one ghost. Someone dropped from the
     * player list keeps their count in [ghostCounts] — so re-adding them brings it back
     * — but they stop showing up anywhere until they do.
     */
    private fun hauntedPlayers(): List<String> = players.filter { (ghostCounts[it] ?: 0) > 0 }

    private fun updateGhostTally() {
        val haunted = hauntedPlayers()
        binding.ghostTally.text = if (haunted.isEmpty()) "" else
            haunted.joinToString("   ") { "👻 $it ${ghostCounts[it]}" }
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
