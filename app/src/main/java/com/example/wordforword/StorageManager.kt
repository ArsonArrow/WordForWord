package com.example.wordforword

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object StorageManager {
    private const val FILE_NAME = "passages.json"

    fun saveMemoryItems(context: Context, memoryItems: List<MemoryItem>) {
        val jsonArray = JSONArray()
        memoryItems.forEach { item ->
            jsonArray.put(serializeItem(item))
        }
        File(context.filesDir, FILE_NAME).writeText(jsonArray.toString())
    }

    fun loadMemoryItems(context: Context): MutableList<MemoryItem> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return mutableListOf()

        val content = file.readText()
        val jsonArray = JSONArray(content)
        val memoryItems = mutableListOf<MemoryItem>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            memoryItems.add(deserializeItem(obj))
        }

        return memoryItems
    }

    private fun serializeItem(item: MemoryItem): JSONObject {
        return when (item) {
            is MemoryItem.Folder -> {
                val json = JSONObject()
                json.put("type", "folder")
                json.put("name", item.name)
                val childrenArray = JSONArray()
                item.items.forEach { childrenArray.put(serializeItem(it)) }
                json.put("items", childrenArray)
                json
            }
            is MemoryItem.Passage -> {
                val json = JSONObject()
                json.put("type", "passage")
                json.put("title", item.title)
                json.put("content", item.content)
                json.put("level", item.level)
                val tokensArray = JSONArray()
                item.tokens.forEach { tokensArray.put(it) }
                json.put("tokens", tokensArray)
                json
            }
        }
    }

    private fun deserializeItem(obj: JSONObject): MemoryItem {
        return when (obj.getString("type")) {
            "folder" -> {
                val name = obj.getString("name")
                val itemsArray = obj.getJSONArray("items")
                val items = mutableListOf<MemoryItem>()
                for (i in 0 until itemsArray.length()) {
                    items.add(deserializeItem(itemsArray.getJSONObject(i)))
                }
                MemoryItem.Folder(name, items)
            }
            "passage" -> {
                val title = obj.getString("title")
                val content = obj.getString("content")
                val level = obj.getInt("level")
                val tokensArray = obj.getJSONArray("tokens")
                val tokens = mutableListOf<String>()
                for (i in 0 until tokensArray.length()) {
                    tokens.add(tokensArray.getString(i))
                }
                MemoryItem.Passage(title, content, tokens, level)
            }
            else -> throw IllegalArgumentException("Unknown MemoryItem type")
        }
    }

    fun findFolderByPath(memoryItems: List<MemoryItem>, path: String): MemoryItem.Folder? {
        val segments = path.split("/").filter { it.isNotBlank() }
        var current = memoryItems.filterIsInstance<MemoryItem.Folder>().firstOrNull()

        for (segment in segments) {
            current = current?.items?.filterIsInstance<MemoryItem.Folder>()?.find { it.name == segment }
        }

        return current
    }
}