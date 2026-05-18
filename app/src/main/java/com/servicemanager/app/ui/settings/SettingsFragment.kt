package com.servicemanager.app.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.widget.doAfterTextChanged
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
        binding.editServerScheme.setText(viewModel.currentServerScheme)
        binding.editServerHost.setText(viewModel.currentServerHost)
        binding.editServerPort.setText(viewModel.currentServerPort.toString())
        binding.editConnectTimeout.setText(viewModel.currentConnectTimeoutSeconds.toString())
        binding.editReadTimeout.setText(viewModel.currentReadTimeoutSeconds.toString())
        updateComposedUrlPreview()

        binding.editServerScheme.doAfterTextChanged { updateComposedUrlPreview() }
        binding.editServerHost.doAfterTextChanged { updateComposedUrlPreview() }
        binding.editServerPort.doAfterTextChanged { updateComposedUrlPreview() }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val settings = validateNetworkSettings() ?: return@setOnClickListener
            viewModel.saveNetworkSettings(
                serverScheme = settings.serverScheme,
                serverHost = settings.serverHost,
                serverPort = settings.serverPort,
                connectTimeoutSeconds = settings.connectTimeoutSeconds,
                readTimeoutSeconds = settings.readTimeoutSeconds,
            )
        }

        binding.btnTestConnection.setOnClickListener {
            val settings = validateNetworkSettings() ?: return@setOnClickListener
            viewModel.testConnection(
                serverScheme = settings.serverScheme,
                serverHost = settings.serverHost,
                serverPort = settings.serverPort,
                connectTimeoutSeconds = settings.connectTimeoutSeconds,
                readTimeoutSeconds = settings.readTimeoutSeconds,
            )
        }

        binding.btnPing.setOnClickListener {
            val settings = validateNetworkSettings() ?: return@setOnClickListener
            viewModel.pingServer(
                serverScheme = settings.serverScheme,
                serverHost = settings.serverHost,
                serverPort = settings.serverPort,
                connectTimeoutSeconds = settings.connectTimeoutSeconds,
                readTimeoutSeconds = settings.readTimeoutSeconds,
            )
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

    private fun validateNetworkSettings(): NetworkSettingsInput? {
        val serverScheme =
            binding.editServerScheme.text
                ?.toString()
                ?.trim()
                ?.lowercase() ?: return null
        val serverHost =
            binding.editServerHost.text
                ?.toString()
                ?.trim() ?: return null
        val serverPort =
            binding.editServerPort.text
                ?.toString()
                ?.trim()
                ?.toIntOrNull()

        val connectTimeout =
            binding.editConnectTimeout.text
                ?.toString()
                ?.trim()
                ?.toIntOrNull()
        val readTimeout =
            binding.editReadTimeout.text
                ?.toString()
                ?.trim()
                ?.toIntOrNull()

        val isSchemeValid =
            when {
            serverScheme.isBlank() -> {
                binding.layoutServerScheme.error = getString(R.string.error_scheme_required)
                false
            }
            serverScheme != "http" && serverScheme != "https" -> {
                binding.layoutServerScheme.error = getString(R.string.error_scheme_invalid)
                false
            }
            else -> {
                binding.layoutServerScheme.error = null
                true
            }
        }

        val isHostValid =
            when {
            serverHost.isBlank() -> {
                binding.layoutServerHost.error = getString(R.string.error_host_required)
                false
            }
            !serverHost.matches(Regex("^[a-zA-Z0-9.-]+$")) -> {
                binding.layoutServerHost.error = getString(R.string.error_host_invalid)
                false
            }
            else -> {
                binding.layoutServerHost.error = null
                true
            }
        }

        val isPortValid =
            if (serverPort == null || serverPort !in 1..65535) {
                binding.layoutServerPort.error = getString(R.string.error_port_range)
                false
            } else {
                binding.layoutServerPort.error = null
                true
            }

        val isConnectTimeoutValid =
            if (connectTimeout == null || connectTimeout !in 1..120) {
                binding.layoutConnectTimeout.error = getString(R.string.error_timeout_range)
                false
            } else {
                binding.layoutConnectTimeout.error = null
                true
            }

        val isReadTimeoutValid =
            if (readTimeout == null || readTimeout !in 1..120) {
                binding.layoutReadTimeout.error = getString(R.string.error_timeout_range)
                false
            } else {
                binding.layoutReadTimeout.error = null
                true
            }

        if (!isSchemeValid || !isHostValid || !isPortValid || !isConnectTimeoutValid || !isReadTimeoutValid) {
            return null
        }

        return NetworkSettingsInput(
            serverScheme = serverScheme,
            serverHost = serverHost,
            serverPort = serverPort!!,
            connectTimeoutSeconds = connectTimeout!!,
            readTimeoutSeconds = readTimeout!!,
        )
    }

    private fun updateComposedUrlPreview() {
        val scheme = binding.editServerScheme.text?.toString()?.trim()?.lowercase().orEmpty()
        val host = binding.editServerHost.text?.toString()?.trim().orEmpty()
        val port = binding.editServerPort.text?.toString()?.trim().orEmpty()
        val composed =
            if ((scheme == "http" || scheme == "https") && host.isNotBlank() && port.isNotBlank()) {
                "$scheme://$host:$port"
            } else {
                ""
            }
        binding.editServerUrl.setText(composed)
    }

    private data class NetworkSettingsInput(
        val serverScheme: String,
        val serverHost: String,
        val serverPort: Int,
        val connectTimeoutSeconds: Int,
        val readTimeoutSeconds: Int,
    )

    override fun onDestroyView() {
        super.onDestroyView()
        bindingNullable = null
    }
}
