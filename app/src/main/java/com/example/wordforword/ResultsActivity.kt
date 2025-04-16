package com.example.wordforword

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.results_panel)

        val totalWords = intent.getIntExtra("totalWords", 0)
        val errorsMade = intent.getIntExtra("errorsMade", 0)
        val correct = totalWords - errorsMade
        val errorThreshold = UserSettings.errorThreshold // to be defined later
        val maxErrors = (totalWords * errorThreshold).toInt()

        val passed = errorsMade <= maxErrors

        val responseText = findViewById<TextView>(R.id.response)
        val accuracyText = findViewById<TextView>(R.id.accuracy)

        responseText.text = if (passed) "Well done!" else "Try again."
        accuracyText.text = getString(R.string.accuracy_format, correct * 100 / totalWords, correct, totalWords)


        findViewById<Button>(R.id.tryAgainButton).setOnClickListener {
            val intent = intent
            finish()
            startActivity(intent) // Restart the same MemoryPracticeActivity session
        }

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            val folderPath = intent.getStringExtra("folderPath")

            if (folderPath.isNullOrEmpty()) {
                finish() // Can't continue without a valid path
                return@setOnClickListener
            }

            val currentIndex = intent.getIntExtra("currentIndex", 0)
            val memoryItems = StorageManager.loadMemoryItems(this)
            val folder = StorageManager.findFolderByPath(memoryItems, folderPath)

            val passages = folder?.children?.filterIsInstance<MemoryItem.Passage>()
            val nextPassage = passages?.getOrNull(currentIndex + 1)

            if (nextPassage != null) {
                val nextIntent = Intent(this, MemoryPracticeActivity::class.java)
                nextIntent.putExtra("passage", nextPassage) // Must implement Serializable
                nextIntent.putExtra("folderPath", folderPath)
                nextIntent.putExtra("currentIndex", currentIndex + 1)
                startActivity(nextIntent)
                finish()
            } else {
                finish() // No more passages in this folder
            }
        }

    }
}
