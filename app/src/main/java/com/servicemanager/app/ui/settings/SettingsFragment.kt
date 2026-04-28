package com.servicemanager.app.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.servicemanager.app.R
import com.servicemanager.app.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var bindingNullable: FragmentSettingsBinding? = null
    private val binding get() = bindingNullable!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        bindingNullable = FragmentSettingsBinding.bind(view)

        setupUI()
        setupListeners()
        setupObservers()
    }

    private fun setupUI() {
        binding.editServerUrl.setText(viewModel.currentUrl)
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val url = validateUrl() ?: return@setOnClickListener
            viewModel.saveServerUrl(url)
        }

        binding.btnTestConnection.setOnClickListener {
            val url = validateUrl() ?: return@setOnClickListener
            viewModel.testConnection(url)
        }

        binding.btnPing.setOnClickListener {
            val url = validateUrl() ?: return@setOnClickListener
            viewModel.pingServer(url)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.saved.collect {
                        Snackbar.make(binding.root, R.string.settings_saved, Snackbar.LENGTH_SHORT).show()
                    }
                }
                launch {
                    viewModel.connectionTestStatus.collect { status ->
                        handleConnectionStatus(status)
                    }
                }
                launch {
                    viewModel.pingResult.collect { status ->
                        handlePingStatus(status)
                    }
                }
            }
        }
    }

    private fun handleConnectionStatus(status: SettingsViewModel.ConnectionStatus) {
        when (status) {
            is SettingsViewModel.ConnectionStatus.Loading -> {
                binding.btnTestConnection.isEnabled = false
                binding.btnTestConnection.setText(R.string.btn_testing)
            }
            is SettingsViewModel.ConnectionStatus.Success -> {
                binding.btnTestConnection.isEnabled = true
                binding.btnTestConnection.setText(R.string.btn_test_connection)
                Snackbar.make(binding.root, R.string.connection_success, Snackbar.LENGTH_SHORT).show()
            }
            is SettingsViewModel.ConnectionStatus.Error -> {
                binding.btnTestConnection.isEnabled = true
                binding.btnTestConnection.setText(R.string.btn_test_connection)
                Snackbar
                    .make(
                        binding.root,
                        getString(R.string.connection_failed, status.message),
                        Snackbar.LENGTH_LONG,
                    ).show()
            }
            is SettingsViewModel.ConnectionStatus.Idle -> {
                binding.btnTestConnection.isEnabled = true
                binding.btnTestConnection.setText(R.string.btn_test_connection)
            }
        }
    }

    private fun handlePingStatus(status: SettingsViewModel.PingStatus) {
        when (status) {
            is SettingsViewModel.PingStatus.Loading -> {
                binding.btnPing.isEnabled = false
                binding.btnPing.setText(R.string.pinging)
                binding.textPingResult.visibility = View.VISIBLE
                binding.textPingResult.text = getString(R.string.pinging)
            }
            is SettingsViewModel.PingStatus.Result -> {
                binding.btnPing.isEnabled = true
                binding.btnPing.setText(R.string.btn_ping)
                binding.textPingResult.text =
                    getString(
                        R.string.format_ping_result,
                        status.min,
                        status.max,
                        status.avg,
                        status.successRate,
                    )
            }
            is SettingsViewModel.PingStatus.Error -> {
                binding.btnPing.isEnabled = true
                binding.btnPing.setText(R.string.btn_ping)
                binding.textPingResult.text = status.message
            }
            is SettingsViewModel.PingStatus.Idle -> {
                binding.btnPing.isEnabled = true
                binding.btnPing.setText(R.string.btn_ping)
                binding.textPingResult.visibility = View.GONE
            }
        }
    }

    private fun validateUrl(): String? {
        val url =
            binding.editServerUrl.text
                ?.toString()
                ?.trim() ?: return null
        return when {
            url.isBlank() -> {
                binding.layoutServerUrl.error = getString(R.string.error_url_required)
                null
            }
            !url.startsWith("http://") && !url.startsWith("https://") -> {
                binding.layoutServerUrl.error = getString(R.string.error_invalid_url)
                null
            }
            else -> {
                binding.layoutServerUrl.error = null
                url
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingNullable = null
    }
}
