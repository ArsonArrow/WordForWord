package com.example.wordforword

import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MemoryPracticeActivity : AppCompatActivity() {
    private lateinit var passage: MemoryItem.Passage
    private lateinit var wordStates: MutableList<WordState>
    private lateinit var adapter: WordAdapter
    private lateinit var keyboardLetters: View
    private lateinit var keyboardNumbers: View
    private lateinit var session: MemoryPracticeSession
    private lateinit var inputToggleSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.memory_game)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            passage = intent.getSerializableExtra("passage", MemoryItem.Passage::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            passage = intent.getSerializableExtra("passage") as MemoryItem.Passage
        }

        wordStates = generateWordStates(passage).toMutableList()

        val recyclerView = findViewById<RecyclerView>(R.id.wordsRecyclerView)
        adapter = WordAdapter(wordStates)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        keyboardLetters = findViewById(R.id.keyboardLetters)
        keyboardNumbers = findViewById(R.id.keyboardNumbers)

        val errorThreshold = UserSettingsManager.getErrorThreshold(this)
        session = MemoryPracticeSession(passage.tokens, errorThreshold = errorThreshold)
        updateKeyboard(session.getNextExpectedChar())

        inputToggleSwitch = findViewById(R.id.inputToggleSwitch)
        val savedMode = UserSettingsManager.getPracticeMode(this)
        inputToggleSwitch.isChecked = savedMode == PracticeMode.VOICE

        inputToggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            val selectedMode = if (isChecked) PracticeMode.VOICE else PracticeMode.FIRST_LETTER
            UserSettingsManager.savePracticeMode(this, selectedMode)
        }
    }

    private fun generateWordStates(passage: MemoryItem.Passage): List<WordState> {
        val words = passage.tokens
        return when (passage.level) {
            1 -> words.map { WordState(it, isVisible = true, opacity = 0.4f) }
            2 -> {
                val halfCount = words.size / 2
                val revealedIndexes = words.indices.shuffled().take(halfCount).toSet()
                words.mapIndexed { index, word ->
                    val isVisible = index in revealedIndexes
                    WordState(word, isVisible = isVisible, opacity = if (isVisible) 0.4f else 0f)
                }
            }
            else -> words.map { WordState(it, isVisible = false, opacity = 0f) }
        }
    }

    private fun updateKeyboard(nextChar: Char) {
        if (nextChar.isDigit()) {
            keyboardLetters.visibility = View.GONE
            keyboardNumbers.visibility = View.VISIBLE
        } else {
            keyboardLetters.visibility = View.VISIBLE
            keyboardNumbers.visibility = View.GONE
        }
    }

    fun onKeyboardInput(view: View) {
        val button = view as Button
        val inputChar = button.text[0]
        handleInput(inputChar)
    }

    private fun handleInput(inputChar: Char) {
        val currentIndex = session.currentIndex
        val expectedChar = session.getNextExpectedChar()

        if (inputChar.equals(expectedChar, ignoreCase = true)) {
            wordStates[currentIndex].isVisible = true
            wordStates[currentIndex].opacity = 1f
            wordStates[currentIndex].isCorrect = true
            wordStates[currentIndex].color = android.graphics.Color.BLACK
            adapter.notifyItemChanged(currentIndex)
            session.advance()

            if (session.isComplete()) {
                goToResults()
            } else {
                updateKeyboard(session.getNextExpectedChar())
            }
        } else {
            wordStates[currentIndex].isCorrect = false
            wordStates[currentIndex].color = android.graphics.Color.RED
            adapter.notifyItemChanged(currentIndex)
            vibrateError()
        }
    }

    private fun vibrateError() {
        val vibrator = getSystemService<Vibrator>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun goToResults() {
        val intent = Intent(this, ResultsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
