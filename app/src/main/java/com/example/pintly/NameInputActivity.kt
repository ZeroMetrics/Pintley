package com.example.pintly

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.pintly.databinding.ActivityNameinputBinding
import com.example.pintly.databinding.ItemPlayerBinding
import com.google.android.material.snackbar.Snackbar

class NameInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNameinputBinding

    private companion object {
        const val STARTING_FIELDS = 2
        const val MIN_FIELDS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNameinputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repeat(STARTING_FIELDS) { addPlayerField(focus = false) }

        binding.addButton.setOnClickListener { addPlayerField(focus = true) }
        binding.doneButton.setOnClickListener { startGame() }
    }

    private fun addPlayerField(focus: Boolean) {
        val row = ItemPlayerBinding.inflate(layoutInflater, binding.nameInputLayout, false)
        row.removeButton.setOnClickListener {
            if (binding.nameInputLayout.childCount > MIN_FIELDS) {
                binding.nameInputLayout.removeView(row.root)
            } else {
                row.playerName.text = null
            }
        }
        binding.nameInputLayout.addView(row.root)
        if (focus) row.playerName.requestFocus()
    }

    private fun collectNames(): List<String> {
        val names = mutableListOf<String>()
        for (i in 0 until binding.nameInputLayout.childCount) {
            val field = binding.nameInputLayout.getChildAt(i)
                .findViewById<EditText>(R.id.player_name)
            val name = field.text.toString().trim()
            if (name.isNotBlank()) names.add(name)
        }
        return names
    }

    private fun startGame() {
        val names = collectNames()
        if (names.isEmpty()) {
            Snackbar.make(binding.root, R.string.need_one_name, Snackbar.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            putStringArrayListExtra("names", ArrayList(names))
        }
        startActivity(intent)
    }
}
