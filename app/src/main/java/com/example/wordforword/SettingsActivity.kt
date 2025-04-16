package com.example.wordforword

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        val spinner = findViewById<Spinner>(R.id.errorThresholdSpinner)
        val options = resources.getStringArray(R.array.error_threshold_options)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set initial selection based on saved settings
        val currentThreshold = (UserSettingsManager.getErrorThreshold(this) * 100).toInt()
        spinner.setSelection((currentThreshold / 5) - 1)

        // Save selection when changed
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedText = parent.getItemAtPosition(position).toString().replace("%", "")
                val selectedValue = selectedText.toFloat() / 100f
                UserSettingsManager.saveErrorThreshold(this@SettingsActivity, selectedValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
