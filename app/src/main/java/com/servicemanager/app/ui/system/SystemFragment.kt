package com.servicemanager.app.ui.system

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.servicemanager.app.R
import com.servicemanager.app.data.model.SystemInfoDto
import com.servicemanager.app.databinding.FragmentSystemBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SystemFragment : Fragment(R.layout.fragment_system) {
    private var bindingNullable: FragmentSystemBinding? = null
    private val binding get() = bindingNullable!!

    private val viewModel: SystemViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        bindingNullable = FragmentSystemBinding.bind(view)

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadSystemInfo() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: SystemUiState) {
        binding.swipeRefresh.isRefreshing = state is SystemUiState.Loading
        when (state) {
            is SystemUiState.Loading -> {
                binding.scrollContent.visibility = View.GONE
                binding.textError.visibility = View.GONE
            }
            is SystemUiState.Success -> {
                binding.scrollContent.visibility = View.VISIBLE
                binding.textError.visibility = View.GONE
                bindInfo(state.info)
            }
            is SystemUiState.Error -> {
                binding.scrollContent.visibility = View.GONE
                binding.textError.visibility = View.VISIBLE
                binding.textError.text = state.message
            }
        }
    }

    private fun bindInfo(info: SystemInfoDto) {
        binding.textHostname.text = info.hostname
        binding.textPlatform.text = info.platform
        binding.textIpAddress.text = info.localIp ?: getString(R.string.value_unknown)
        binding.textNodeVersion.text = info.nodeVersion ?: getString(R.string.value_unknown)
        binding.textUptime.text = formatUptime(info.uptime)
        binding.textMemory.text =
            getString(
                R.string.format_memory_usage,
                formatBytes(info.memory.used),
                formatBytes(info.memory.total),
            )
    }

    private fun formatUptime(seconds: Double): String {
        val totalSecs = seconds.toLong()
        val hours = totalSecs / SECONDS_IN_HOUR
        val minutes = (totalSecs % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE
        return getString(R.string.format_uptime, hours, minutes)
    }

    private fun formatBytes(bytes: Long): String {
        val mb = bytes / BYTES_IN_MB
        return getString(R.string.format_bytes_mb, mb)
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadSystemInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingNullable = null
    }

    companion object {
        private const val SECONDS_IN_HOUR = 3600
        private const val SECONDS_IN_MINUTE = 60
        private const val BYTES_IN_MB = 1024 * 1024
    }
}
