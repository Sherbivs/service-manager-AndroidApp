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

    private fun validateUrl(): String? {
        val url =
            binding.editServerUrl.text
                ?.toString()
                ?.trim() ?: return null
        if (url.isBlank()) {
            binding.layoutServerUrl.error = getString(R.string.error_url_required)
            return null
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            binding.layoutServerUrl.error = getString(R.string.error_invalid_url)
            return null
        }
        binding.layoutServerUrl.error = null
        return url
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingNullable = null
    }
}
