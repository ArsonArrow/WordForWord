package com.example.wordforword

import java.io.Serializable

sealed class MemoryItem : Serializable {
    data class Folder(
        val name: String,
        val items: MutableList<MemoryItem> = mutableListOf()
    ) : MemoryItem()

    data class Passage(
        val title: String,
        val content: String,
        val tokens: List<String>,
        val level: Int
    ) : MemoryItem()
}