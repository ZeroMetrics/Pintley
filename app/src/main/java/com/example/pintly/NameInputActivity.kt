package com.example.pintly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class NameInputActivity : AppCompatActivity() {
    private val nameFields = arrayListOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nameinput)

        val layout: LinearLayout = findViewById(R.id.name_input_layout)
        val addButton: Button = findViewById(R.id.add_button)
        val doneButton: Button = findViewById(R.id.done_button)

        // Initialize with one EditText
        val initialNameField = EditText(this)
        nameFields.add(initialNameField)
        layout.addView(initialNameField)

        addButton.setOnClickListener {
            val newNameField = EditText(this)
            nameFields.add(newNameField)
            layout.addView(newNameField)
        }

        doneButton.setOnClickListener {
            val names = nameFields.map { it.text.toString().trim() }
            // Filter out empty names
            val nonEmptyNames = names.filter { it.isNotBlank() }
            if (nonEmptyNames.isEmpty()) {
                // No names entered, do not proceed
                Toast.makeText(this, "Please enter at least one name", Toast.LENGTH_SHORT).show()
            } else {
                // At least one name was entered, proceed to MainActivity
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("names", ArrayList(nonEmptyNames))
                }
                startActivity(intent)
            }
        }
    }
}