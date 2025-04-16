package com.example.wordforword

import android.graphics.Color

data class WordState(
    val text: String,
    var isVisible: Boolean,
    var isCorrect: Boolean? = null, // null = untested, true = correct, false = incorrect
    var opacity: Float,
    var color: Int = Color.BLACK
)
