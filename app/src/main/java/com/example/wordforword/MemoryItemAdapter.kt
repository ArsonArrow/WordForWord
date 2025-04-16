package com.example.wordforword

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MemoryItemAdapter(
    private val memoryItems: MutableList<MemoryItem>,
    private val onItemClick: (MemoryItem) -> Unit,
    private val onItemLongClick: (Int, Any?) -> Unit
) : RecyclerView.Adapter<MemoryItemAdapter.MemoryItemViewHolder>() {

    class MemoryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView = itemView.findViewById(R.id.memoryTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.memory_game, parent, false)
        return MemoryItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryItemViewHolder, position: Int) {
        val item = memoryItems[position]
        val displayText = when (item) {
            is MemoryItem.Passage -> "${item.title}\n\n${item.content}\n\n${item.title}"
            is MemoryItem.Folder -> "📁 ${item.name}"
        }

        holder.titleView.text = displayText

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(position)
            true
        }
    }

    override fun getItemCount(): Int = memoryItems.size
}
