package com.servicemanager.app.ui.services

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.servicemanager.app.R
import com.servicemanager.app.databinding.FragmentServicesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ServicesFragment : Fragment(R.layout.fragment_services) {
    private var bindingNullable: FragmentServicesBinding? = null
    private val binding get() = bindingNullable!!

    private val viewModel: ServicesViewModel by viewModels()

    private val adapter =
        ServicesAdapter(
            onStart = { id -> viewModel.startService(id) },
            onStop = { id -> viewModel.stopService(id) },
            onRestart = { id -> viewModel.restartService(id) },
        )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        bindingNullable = FragmentServicesBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadServices() }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pendingActions.collect { ids -> adapter.updatePendingActions(ids) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionSuccess.collect { message ->
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionError.collect { message ->
                    Snackbar
                        .make(binding.root, message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry) { viewModel.retryLastAction() }
                        .show()
                }
            }
        }
    }

    private fun render(state: ServicesUiState) {
        binding.swipeRefresh.isRefreshing = state is ServicesUiState.Loading
        when (state) {
            is ServicesUiState.Loading -> {
                binding.textError.visibility = View.GONE
            }
            is ServicesUiState.Success -> {
                binding.textError.visibility = View.GONE
                adapter.submitList(state.services)
            }
            is ServicesUiState.Error -> {
                binding.textError.visibility = View.VISIBLE
                binding.textError.text = state.message
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startPolling()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopPolling()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingNullable = null
    }
}
