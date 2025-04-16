package com.example.wordforword

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FolderContentsActivity : AppCompatActivity() {

    private lateinit var memoryAdapter: MemoryItemAdapter
    private lateinit var currentFolder: MemoryItem.Folder
    private lateinit var memoryItems: MutableList<MemoryItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_main)

        setSupportActionBar(findViewById(R.id.mainMenuToolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val path = intent.getStringArrayListExtra("folderPath") ?: run {
            Toast.makeText(this, "Folder path missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val rootItems = StorageManager.loadMemoryItems(this)
        currentFolder = findFolderByPath(rootItems, path) ?: run {
            Toast.makeText(this, "Folder not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        memoryItems = currentFolder.items
        title = currentFolder.name

        val recyclerView = findViewById<RecyclerView>(R.id.itemRecyclerView)
        memoryAdapter = MemoryItemAdapter(
            memoryItems,
            onItemClick = { item ->
                when (item) {
                    is MemoryItem.Passage -> showPlaceholder("Open passage in game view")
                    is MemoryItem.Folder -> showPlaceholder("Open nested folder not supported yet")
                }
            },
            onItemLongClick = { position ->
                showItemOptions(position)
            }
        )

        recyclerView.adapter = memoryAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun findFolderByPath(items: List<MemoryItem>, path: List<String>): MemoryItem.Folder? {
        var currentItems = items
        var currentFolder: MemoryItem.Folder? = null

        for (name in path) {
            currentFolder = currentItems
                .filterIsInstance<MemoryItem.Folder>()
                .find { it.name == name }
            if (currentFolder == null) return null
            currentItems = currentFolder.items
        }

        return currentFolder
    }

    private fun showItemOptions(position: Int) {
        val recyclerView = findViewById<RecyclerView>(R.id.itemRecyclerView)
        val itemView = recyclerView.findViewHolderForAdapterPosition(position)?.itemView ?: return

        val popup = androidx.appcompat.widget.PopupMenu(this, itemView)
        popup.menuInflater.inflate(R.menu.passage_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.option_delete -> {
                    showDeletionPopup(position)
                    true
                }
                R.id.option_move -> {
                    showPlaceholder("Move from subfolder not implemented yet")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDeletionPopup(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ -> deleteItem(position) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteItem(position: Int) {
        memoryItems.removeAt(position)
        memoryAdapter.notifyItemRemoved(position)
        StorageManager.saveMemoryItems(this, memoryItems)
        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
    }


    private fun showPlaceholder(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
