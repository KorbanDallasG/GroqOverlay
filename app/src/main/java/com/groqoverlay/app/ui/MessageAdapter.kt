package com.groqoverlay.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.groqoverlay.app.R
import com.groqoverlay.app.data.Message
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val onCopy: (String) -> Unit,
    private val onDelete: (Message) -> Unit
) : RecyclerView.Adapter<MessageAdapter.VH>() {

    private val items = mutableListOf<Message>()
    private var markwon: Markwon? = null
    var isStreaming = false
    var fontSize = 14f

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.bubbleText)
        val time: TextView = view.findViewById(R.id.timeText)
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position].role == "user") 1 else 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        if (markwon == null) markwon = Markwon.create(parent.context)
        val layout = if (viewType == 1) R.layout.item_message_user else R.layout.item_message_ai
        return VH(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = items[position]
        holder.text.textSize = fontSize
        val streamingLast = isStreaming && position == items.size - 1 && msg.role == "assistant"
        if (msg.role == "assistant" && !streamingLast) {
            markwon?.setMarkdown(holder.text, msg.content)
        } else {
            holder.text.text = msg.content
        }
        holder.time.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
        holder.itemView.setOnLongClickListener { v ->
            val popup = PopupMenu(v.context, v)
            popup.menu.add(0, 1, 0, "Копировать")
            if (msg.role == "assistant" && !isStreaming) popup.menu.add(0, 2, 1, "Удалить")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> { onCopy(msg.content); true }
                    2 -> { onDelete(msg); true }
                    else -> false
                }
            }
            popup.show()
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(list: List<Message>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    fun addMessage(msg: Message) {
        items.add(msg); notifyItemInserted(items.size - 1)
    }

    fun appendToLast(token: String) {
        if (items.isEmpty()) return
        val last = items[items.size - 1]
        items[items.size - 1] = last.copy(content = last.content + token)
        notifyItemChanged(items.size - 1)
    }

    fun setLastId(id: Long) {
        if (items.isNotEmpty()) {
            val l = items[items.size - 1]
            items[items.size - 1] = l.copy(id = id)
        }
    }

    fun lastContent(): String = items.lastOrNull()?.content ?: ""

    fun removeLastIfEmpty() {
        if (items.isNotEmpty() && items.last().content.isEmpty()) {
            items.removeAt(items.size - 1)
            notifyItemRemoved(items.size)
        }
    }

    fun removeMessage(msg: Message) {
        val i = items.indexOfFirst { it.id == msg.id }
        if (i >= 0) { items.removeAt(i); notifyItemRemoved(i) }
    }

    fun clearAll() { items.clear(); notifyDataSetChanged() }

    fun allText(): String = items.joinToString("\n\n") {
        (if (it.role == "user") "Я: " else "AI: ") + it.content
    }
}
