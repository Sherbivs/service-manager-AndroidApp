package com.servicemanager.app.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.servicemanager.app.R
import com.servicemanager.app.databinding.FragmentOnboardingBinding
import com.servicemanager.app.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {
    private var bindingNullable: FragmentOnboardingBinding? = null
    private val binding get() = bindingNullable!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        bindingNullable = FragmentOnboardingBinding.bind(view)

        setupUI()
        setupListeners()
        setupObservers()
    }

    private fun setupUI() {
        binding.editServerUrl.setText(viewModel.currentUrl)
    }

    private fun setupListeners() {
        binding.btnConnect.setOnClickListener {
            val url =
                binding.editServerUrl.text
                    ?.toString()
                    ?.trim() ?: return@setOnClickListener
            if (url.isBlank()) {
                binding.layoutServerUrl.error = getString(R.string.error_url_required)
                return@setOnClickListener
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                binding.layoutServerUrl.error = getString(R.string.error_invalid_url)
                return@setOnClickListener
            }
            binding.layoutServerUrl.error = null
            viewModel.testConnection(url)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.connectionTestStatus.collect { status ->
                    handleConnectionStatus(status)
                }
            }
        }
    }

    private fun handleConnectionStatus(status: SettingsViewModel.ConnectionStatus) {
        when (status) {
            is SettingsViewModel.ConnectionStatus.Loading -> {
                binding.btnConnect.isEnabled = false
                binding.btnConnect.setText(R.string.btn_testing)
            }
            is SettingsViewModel.ConnectionStatus.Success -> {
                // Save and move to main
                viewModel.saveServerUrl(binding.editServerUrl.text.toString())
                findNavController().navigate(R.id.action_onboarding_to_services)
            }
            is SettingsViewModel.ConnectionStatus.Error -> {
                binding.btnConnect.isEnabled = true
                binding.btnConnect.setText(R.string.btn_test_connection)
                Snackbar
                    .make(
                        binding.root,
                        getString(R.string.connection_failed, status.message),
                        Snackbar.LENGTH_LONG,
                    ).show()
            }
            is SettingsViewModel.ConnectionStatus.Idle -> {
                binding.btnConnect.isEnabled = true
                binding.btnConnect.setText(R.string.btn_test_connection)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingNullable = null
    }
}
