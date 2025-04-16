package com.example.wordforword

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var memoryAdapter: MemoryItemAdapter
    private val memoryItems = mutableListOf<MemoryItem>()

    companion object {
        const val ADD_ITEM_REQUEST_CODE = 1
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_main)

        // Load the full tree of memory items (folders + passages)
        memoryItems.clear()
        memoryItems.addAll(StorageManager.loadMemoryItems(this))

        val recyclerView = findViewById<RecyclerView>(R.id.itemRecyclerView)
        memoryAdapter = MemoryItemAdapter(memoryItems,
            onItemClick = { item ->
                when (item) {
                    is MemoryItem.Passage -> openMemoryPractice(item)
                    is MemoryItem.Folder -> openFolder(item)
                }
            },
            onItemLongClick = { item, view ->
                showItemOptionsDialog(item, view)
            }
        )

        recyclerView.adapter = memoryAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val addButton = findViewById<FloatingActionButton>(R.id.addItemFab)
        addButton.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            startActivityForResult(intent, ADD_ITEM_REQUEST_CODE)
        }
    }

    private fun openMemoryPractice(passage: MemoryItem.Passage) {
        val intent = Intent(this, MemoryPracticeActivity::class.java)
        intent.putExtra("passage", passage)
        startActivity(intent)
    }

    private fun openFolder(folder: MemoryItem.Folder) {
        val intent = Intent(this, FolderContentsActivity::class.java)
        intent.putExtra("folder", folder)
        startActivity(intent)
    }

    private fun showItemOptionsDialog(item: MemoryItem, anchorView: View) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.passage_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.option_delete -> {
                    val index = memoryItems.indexOf(item)
                    if (index != -1) {
                        memoryItems.removeAt(index)
                        memoryAdapter.notifyItemRemoved(index)
                        StorageManager.saveMemoryItems(this, memoryItems)
                    }
                    true
                }
                R.id.option_move -> {
                    val folders = memoryItems.filterIsInstance<MemoryItem.Folder>()
                    if (folders.isEmpty()) {
                        showPlaceholder("No folders available.")
                        return@setOnMenuItemClickListener true
                    }
                    val index = memoryItems.indexOf(item)
                    if (index != -1) showMoveDialog(index, folders)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_ITEM_REQUEST_CODE && resultCode == RESULT_OK) {
            val newItem = data?.getSerializableExtra("newItem") as? MemoryItem
            if (newItem != null) {
                memoryItems.add(newItem)
                StorageManager.saveMemoryItems(this, memoryItems)
                memoryAdapter.notifyItemInserted(memoryItems.size - 1)
            } else {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMemoryItemOptions(position: Int) {
        val view = findViewById<RecyclerView>(R.id.itemRecyclerView)
            .findViewHolderForAdapterPosition(position)?.itemView ?: return

        val popup = androidx.appcompat.widget.PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.passage_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.option_delete -> {
                    memoryItems.removeAt(position)
                    memoryAdapter.notifyItemRemoved(position)
                    StorageManager.saveMemoryItems(this, memoryItems)
                    true
                }
                R.id.option_move -> {
                    val folders = memoryItems.filterIsInstance<MemoryItem.Folder>()
                    if (folders.isEmpty()) {
                        showPlaceholder("No folders available.")
                        return@setOnMenuItemClickListener true
                    }

                    showMoveDialog(position, folders)
                    true
                }

                else -> false
            }
        }

        popup.show()
    }

    private fun showAddFolderDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("New Folder")

        val input = android.widget.EditText(this).apply {
            hint = "Folder name"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }

        builder.setView(input)

        builder.setPositiveButton("Create") { _, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                val newFolder = MemoryItem.Folder(name)
                memoryItems.add(newFolder)
                memoryAdapter.notifyItemInserted(memoryItems.size - 1)
                StorageManager.saveMemoryItems(this, memoryItems)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showMoveDialog(passageIndex: Int, folders: List<MemoryItem.Folder>) {
        val folderNames = folders.map { it.name }.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("Move to Folder")
            .setItems(folderNames) { _, selectedIndex ->
                val passage = memoryItems[passageIndex] as? MemoryItem.Passage ?: return@setItems
                folders[selectedIndex].items.add(passage)
                memoryItems.removeAt(passageIndex)
                memoryAdapter.notifyItemRemoved(passageIndex)
                StorageManager.saveMemoryItems(this, memoryItems)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun openFolderContents(folder: MemoryItem.Folder, rootItems: List<MemoryItem>) {
        val path = buildFolderPath(folder, rootItems)

        if (path == null) {
            Toast.makeText(this, "Could not locate folder path", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, FolderContentsActivity::class.java)
        intent.putStringArrayListExtra("folderPath", ArrayList(path))
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun buildFolderPath(
        target: MemoryItem.Folder,
        currentList: List<MemoryItem>,
        path: MutableList<String> = mutableListOf()
    ): List<String>? {
        for (item in currentList) {
            if (item is MemoryItem.Folder) {
                path.add(item.name)
                if (item === target) {
                    return path.toList()
                }
                val result = buildFolderPath(target, item.items, path)
                if (result != null) return result
                path.removeLast()
            }
        }
        return null
    }

    fun openSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showPlaceholder(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
