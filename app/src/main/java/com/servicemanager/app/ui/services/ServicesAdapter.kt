package com.servicemanager.app.ui.services

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.servicemanager.app.R
import com.servicemanager.app.data.model.ServiceDto
import com.servicemanager.app.databinding.ItemServiceBinding

class ServicesAdapter(
    private val onStart: (String) -> Unit,
    private val onStop: (String) -> Unit,
    private val onRestart: (String) -> Unit,
) : ListAdapter<ServiceDto, ServicesAdapter.ViewHolder>(DiffCallback) {
    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<ServiceDto>() {
                override fun areItemsTheSame(
                    old: ServiceDto,
                    new: ServiceDto,
                ) = old.id == new.id

                override fun areContentsTheSame(
                    old: ServiceDto,
                    new: ServiceDto,
                ) = old == new
            }
    }

    private var pendingIds: Set<String> = emptySet()

    fun updatePendingActions(ids: Set<String>) {
        if (ids == pendingIds) return
        pendingIds = ids
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) = holder.bind(getItem(position))

    inner class ViewHolder(
        private val binding: ItemServiceBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: ServiceDto) {
            binding.textName.text = service.name
            binding.chipStatus.text = service.status

            val (bgColorRes, textColorRes) =
                when (service.status) {
                    "running" -> R.color.status_running_bg to R.color.status_running_text
                    "stopped" -> R.color.status_stopped_bg to R.color.status_stopped_text
                    else -> R.color.status_error_bg to R.color.status_error_text
                }
            val ctx = binding.root.context
            binding.chipStatus.chipBackgroundColor =
                ColorStateList.valueOf(ctx.getColor(bgColorRes))
            binding.chipStatus.setTextColor(ctx.getColor(textColorRes))

            // Display project
            if (!service.project.isNullOrBlank()) {
                binding.textProject.visibility = android.view.View.VISIBLE
                binding.textProject.text = ctx.getString(R.string.format_project, service.project)
            } else {
                binding.textProject.visibility = android.view.View.GONE
            }

            // Display description
            if (!service.description.isNullOrBlank()) {
                binding.textDescription.visibility = android.view.View.VISIBLE
                binding.textDescription.text = service.description
            } else {
                binding.textDescription.visibility = android.view.View.GONE
            }

            // Display health metrics if running
            if (service.status == "running" && service.healthPercent != null) {
                binding.layoutHealth.visibility = android.view.View.VISIBLE
                binding.textHealth.text = ctx.getString(R.string.format_health_percent, service.healthPercent)
                if (service.avgLatency != null) {
                    binding.textLatency.text = ctx.getString(R.string.format_latency_ms, service.avgLatency)
                } else {
                    binding.textLatency.visibility = android.view.View.GONE
                }
            } else {
                binding.layoutHealth.visibility = android.view.View.GONE
            }

            val isRunning = service.status == "running"
            val isPending = pendingIds.contains(service.id)
            binding.btnStart.isEnabled = !isRunning && !isPending
            binding.btnStop.isEnabled = isRunning && !isPending
            binding.btnRestart.isEnabled = isRunning && !isPending

            binding.btnStart.setOnClickListener { onStart(service.id) }
            binding.btnStop.setOnClickListener { onStop(service.id) }
            binding.btnRestart.setOnClickListener { onRestart(service.id) }
        }
    }
}
