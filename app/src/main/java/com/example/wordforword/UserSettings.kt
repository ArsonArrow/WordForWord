package com.example.wordforword


data class UserSettings(
    var practiceMode: PracticeMode,
    val errorThreshold: Float
) {
    companion object {
        var errorThreshold: Float = 0.3f
    }
}

enum class PracticeMode {
    FIRST_LETTER,
    VOICE
}