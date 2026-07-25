package com.example.pintly

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.pintly.databinding.ActivityCategorySettingsBinding
import com.example.pintly.databinding.ItemCategoryBinding
import com.google.android.material.snackbar.Snackbar

class CategorySettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategorySettingsBinding
    private val rows = mutableListOf<Row>()

    private data class Row(val name: String, val isUltra: Boolean, val item: ItemCategoryBinding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategorySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SoundManager.init(this)
        buildRows()
        binding.saveButton.setOnClickListener { SoundManager.playPop(); saveAndFinish() }
        binding.resetButton.setOnClickListener { SoundManager.playPop(); resetDefaults() }
    }

    private fun buildRows() {
        binding.categoriesContainer.removeAllViews()
        rows.clear()

        for (cat in GameData.categories()) {
            val item = ItemCategoryBinding.inflate(layoutInflater, binding.categoriesContainer, false)
            item.categoryName.text = cat.name
            item.colorSwatch.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, cat.colorRes))

            val enabled = CategorySettings.isEnabled(this, cat.name)
            item.categorySwitch.isChecked = enabled
            item.categoryPercent.setText(CategorySettings.weight(this, cat.name, cat.defaultWeight).toString())

            if (cat.isUltra) {
                // Ultra is a rare fixed-chance card, not part of the % pool.
                item.categoryPercent.visibility = View.GONE
                item.percentLabel.visibility = View.GONE
                item.rareLabel.visibility = View.VISIBLE
            } else {
                item.categoryPercent.doAfterTextChanged { updateTotal() }
            }
            applyEnabledState(item, enabled)

            item.categorySwitch.setOnCheckedChangeListener { _, isChecked ->
                applyEnabledState(item, isChecked)
                updateTotal()
            }

            binding.categoriesContainer.addView(item.root)
            rows.add(Row(cat.name, cat.isUltra, item))
        }
        updateTotal()
    }

    private fun applyEnabledState(item: ItemCategoryBinding, enabled: Boolean) {
        item.categoryPercent.isEnabled = enabled
        val alpha = if (enabled) 1f else 0.4f
        item.categoryName.alpha = alpha
        item.categoryPercent.alpha = alpha
        item.colorSwatch.alpha = alpha
        item.rareLabel.alpha = alpha
    }

    private fun weightOf(item: ItemCategoryBinding): Int =
        item.categoryPercent.text.toString().toIntOrNull()?.coerceIn(0, 100) ?: 0

    private fun updateTotal() {
        val total = rows.filter { !it.isUltra && it.item.categorySwitch.isChecked }
            .sumOf { weightOf(it.item) }
        binding.totalText.text = getString(R.string.categories_total, total)
    }

    private fun saveAndFinish() {
        // A playable game needs at least one ordinary (non-Ultra) category on.
        val playable = rows.filter {
            !it.isUltra && it.item.categorySwitch.isChecked && weightOf(it.item) > 0
        }
        if (playable.isEmpty()) {
            Snackbar.make(binding.root, R.string.enable_one_category, Snackbar.LENGTH_SHORT).show()
            return
        }
        val choices = rows.map { row ->
            val enabled = row.item.categorySwitch.isChecked
            var weight = weightOf(row.item)
            if (enabled && weight <= 0) weight = 1
            CategorySettings.Choice(row.name, enabled, weight)
        }
        CategorySettings.save(this, choices)
        finish()
    }

    private fun resetDefaults() {
        CategorySettings.resetToDefaults(this)
        buildRows()
    }
}
