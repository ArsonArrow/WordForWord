package com.example.wordforword

data class MemoryPracticeSession(
    val tokens: List<String>,
    var currentIndex: Int = 0,
    var inputBuffer: StringBuilder = StringBuilder(),
    var errorsMade: Int = 0,
    val errorThreshold: Float
) {

    fun getNextExpectedChar(): Char {
        if (currentIndex >= tokens.size) return ' '
        val word = tokens[currentIndex]
        return if (word.isNotEmpty()) word[0] else ' '
    }

    fun isComplete(): Boolean {
        return currentIndex >= tokens.size
    }

    fun advance() {
        currentIndex++
        inputBuffer.clear()
    }

    fun recordError() {
        errorsMade++
    }

    fun hasFailed(): Boolean {
        val allowedErrors = (tokens.size * errorThreshold).toInt()
        return errorsMade > allowedErrors
    }
}