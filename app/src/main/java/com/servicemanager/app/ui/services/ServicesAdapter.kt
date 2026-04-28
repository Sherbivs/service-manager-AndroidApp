package com.servicemanager.app.ui.services

import android.content.res.ColorStateList
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
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
    private val onVisit: (String) -> Unit,
    private val onResetCb: (String) -> Unit,
) : ListAdapter<ServiceDto, ServicesAdapter.ViewHolder>(DiffCallback) {
    companion object {
        private const val PAYLOAD_PENDING_ACTIONS = "PAYLOAD_PENDING_ACTIONS"

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
        val oldPending = pendingIds
        pendingIds = ids

        // Notify only items that changed their "pending" status
        val affectedIds = (oldPending - ids) + (ids - oldPending)
        for (i in 0 until itemCount) {
            if (getItem(i).id in affectedIds) {
                notifyItemChanged(i, PAYLOAD_PENDING_ACTIONS)
            }
        }
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
        payloads: List<Any>,
    ) {
        if (payloads.contains(PAYLOAD_PENDING_ACTIONS)) {
            holder.updateActions(getItem(position))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
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
                binding.textProject.visibility = View.VISIBLE
                binding.textProject.text = ctx.getString(R.string.format_project, service.project)
            } else {
                binding.textProject.visibility = View.GONE
            }

            // Display description
            if (!service.description.isNullOrBlank()) {
                binding.textDescription.visibility = View.VISIBLE
                binding.textDescription.text = service.description
            } else {
                binding.textDescription.visibility = View.GONE
            }

            // Display visit button if URL exists
            if (!service.url.isNullOrBlank()) {
                binding.btnVisit.visibility = View.VISIBLE
                binding.btnVisit.setOnClickListener { onVisit(service.url) }
            } else {
                binding.btnVisit.visibility = View.GONE
            }

            // Display health metrics if running; show explicit stopped state for clarity.
            if (service.status == "running" && service.healthPercent != null) {
                binding.layoutHealth.visibility = View.VISIBLE
                binding.textHealth.text = ctx.getString(R.string.format_health_percent, service.healthPercent)
                if (service.avgLatency != null) {
                    binding.textLatency.visibility = View.VISIBLE
                    binding.textLatency.text = ctx.getString(R.string.format_latency_ms, service.avgLatency)
                } else {
                    binding.textLatency.visibility = View.GONE
                }
            } else if (service.status == "stopped") {
                binding.layoutHealth.visibility = View.VISIBLE
                binding.textHealth.text = ctx.getString(R.string.status_stopped_label)
                binding.textLatency.visibility = View.GONE
            } else {
                binding.layoutHealth.visibility = View.GONE
            }

            // Display circuit breaker state
            if (service.circuitBreakerTripped) {
                binding.chipCircuitBreaker.visibility = View.VISIBLE
                binding.btnResetCircuitBreaker.visibility = View.VISIBLE
                binding.btnResetCircuitBreaker.setOnClickListener { onResetCb(service.id) }
            } else {
                binding.chipCircuitBreaker.visibility = View.GONE
                binding.btnResetCircuitBreaker.visibility = View.GONE
            }

            updateActions(service)

            binding.btnStart.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onStart(service.id)
            }
            binding.btnStop.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onStop(service.id)
            }
            binding.btnRestart.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onRestart(service.id)
            }
        }

        fun updateActions(service: ServiceDto) {
            val isRunning = service.status == "running"
            val isPending = pendingIds.contains(service.id)
            binding.btnStart.isEnabled = !isRunning && !isPending
            binding.btnStop.isEnabled = isRunning && !isPending
            binding.btnRestart.isEnabled = isRunning && !isPending
            binding.btnResetCircuitBreaker.isEnabled = !isPending
        }
    }
}
