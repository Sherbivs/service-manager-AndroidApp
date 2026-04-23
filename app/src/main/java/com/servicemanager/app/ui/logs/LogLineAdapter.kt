package com.servicemanager.app.ui.logs

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.servicemanager.app.databinding.ItemLogLineBinding

class LogLineAdapter : ListAdapter<String, LogLineAdapter.ViewHolder>(DIFF) {
    private val expandedPositions = mutableSetOf<Int>()

    fun submitLines(newLines: List<String>) {
        expandedPositions.clear()
        submitList(newLines)
    }

    inner class ViewHolder(
        private val binding: ItemLogLineBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
                if (expandedPositions.contains(pos)) {
                    expandedPositions.remove(pos)
                } else {
                    expandedPositions.add(pos)
                }
                notifyItemChanged(pos)
            }
        }

        fun bind(
            line: String,
            expanded: Boolean,
        ) {
            binding.textLogLine.text = line
            if (expanded) {
                binding.textLogLine.maxLines = Int.MAX_VALUE
                binding.textLogLine.ellipsize = null
            } else {
                binding.textLogLine.maxLines = 1
                binding.textLogLine.ellipsize = TextUtils.TruncateAt.END
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding = ItemLogLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position), expandedPositions.contains(position))
    }

    companion object {
        private val DIFF =
            object : DiffUtil.ItemCallback<String>() {
                override fun areItemsTheSame(
                    oldItem: String,
                    newItem: String,
                ) = oldItem == newItem

                override fun areContentsTheSame(
                    oldItem: String,
                    newItem: String,
                ) = oldItem == newItem
            }
    }
}
