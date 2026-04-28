package com.servicemanager.app.ui.logs

import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
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
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
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
            binding.textLogLine.text = getHighlightedLine(line)
            if (expanded) {
                binding.textLogLine.maxLines = Int.MAX_VALUE
                binding.textLogLine.ellipsize = null
            } else {
                binding.textLogLine.maxLines = 1
                binding.textLogLine.ellipsize = TextUtils.TruncateAt.END
            }
        }

        private fun getHighlightedLine(line: String): CharSequence {
            val spannable = SpannableString(line)
            val color =
                when {
                    line.contains("ERROR", ignoreCase = true) -> "#FF5252".toColorInt() // Red
                    line.contains("WARN", ignoreCase = true) -> "#FFD740".toColorInt() // Yellow/Amber
                    line.contains("INFO", ignoreCase = true) -> "#69F0AE".toColorInt() // Green
                    line.contains("DEBUG", ignoreCase = true) -> "#40C4FF".toColorInt() // Blue
                    else -> return line
                }

            val levelStart =
                listOf("ERROR", "WARN", "INFO", "DEBUG")
                    .firstOrNull {
                        line.contains(it, ignoreCase = true)
                    }?.let { line.indexOf(it, ignoreCase = true) } ?: -1

            if (levelStart != -1) {
                val levelEnd = line.indexOf(' ', levelStart).let { if (it == -1) line.length else it }
                spannable.setSpan(
                    ForegroundColorSpan(color),
                    0,
                    levelEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
            return spannable
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
