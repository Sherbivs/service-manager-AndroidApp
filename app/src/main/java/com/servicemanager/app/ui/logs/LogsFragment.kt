package com.servicemanager.app.ui.logs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.servicemanager.app.R
import com.servicemanager.app.databinding.FragmentLogsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogsFragment : Fragment(R.layout.fragment_logs) {
    private var bindingNullable: FragmentLogsBinding? = null
    private val binding get() = bindingNullable!!

    private val viewModel: LogsViewModel by viewModels()

    private val logsAdapter = LogLineAdapter()
    private val archiveAdapter = LogLineAdapter()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        bindingNullable = FragmentLogsBinding.bind(view)

        setupRecyclers()
        setupChips()
        setupArchiveSearch()
        setupCopyFab()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadLogs(selectedLineCount())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.archiveState.collect { renderArchive(it) }
            }
        }
    }

    private fun setupRecyclers() {
        binding.recyclerLogs.apply {
            adapter = logsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.recyclerArchive.apply {
            adapter = archiveAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupChips() {
        binding.chipGroupLines.setOnCheckedStateChangeListener { _, _ ->
            viewModel.loadLogs(selectedLineCount())
        }
    }

    private fun setupArchiveSearch() {
        binding.btnSearchArchive.setOnClickListener { triggerArchiveSearch() }
        binding.editArchiveQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerArchiveSearch()
                true
            } else {
                false
            }
        }
    }

    private fun triggerArchiveSearch() {
        val serviceId =
            binding.editServiceId.text
                ?.toString()
                ?.trim()
                .orEmpty()
        val query =
            binding.editArchiveQuery.text
                ?.toString()
                ?.trim()
                .orEmpty()
        if (serviceId.isBlank() || query.isBlank()) {
            Snackbar
                .make(
                    binding.root,
                    getString(R.string.hint_service_id) + " and query required",
                    Snackbar.LENGTH_SHORT,
                ).show()
            return
        }
        viewModel.searchArchive(serviceId, query)
    }

    private fun setupCopyFab() {
        binding.fabCopyLogs.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is LogsUiState.Success && state.lines.isNotEmpty()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("logs", state.lines.joinToString("\n"))
                clipboard.setPrimaryClip(clip)
                Snackbar.make(binding.root, R.string.logs_copied, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectedLineCount(): Int =
        when (binding.chipGroupLines.checkedChipId) {
            R.id.chip50 -> 50
            R.id.chip200 -> 200
            R.id.chip500 -> 500
            else -> 100
        }

    private fun render(state: LogsUiState) {
        binding.swipeRefresh.isRefreshing = state is LogsUiState.Loading
        when (state) {
            is LogsUiState.Loading -> {
                binding.recyclerLogs.visibility = View.GONE
                binding.textError.visibility = View.GONE
            }
            is LogsUiState.Success -> {
                binding.recyclerLogs.visibility = View.VISIBLE
                binding.textError.visibility = View.GONE
                logsAdapter.submitLines(state.lines)
                if (state.lines.isNotEmpty()) {
                    binding.recyclerLogs.post {
                        binding.recyclerLogs.scrollToPosition(state.lines.size - 1)
                    }
                }
            }
            is LogsUiState.Error -> {
                binding.recyclerLogs.visibility = View.GONE
                binding.textError.visibility = View.VISIBLE
                binding.textError.text = state.message
            }
        }
    }

    private fun renderArchive(state: ArchiveUiState) {
        when (state) {
            is ArchiveUiState.Idle -> {
                binding.recyclerArchive.visibility = View.GONE
                binding.textArchiveStatus.visibility = View.GONE
            }
            is ArchiveUiState.Loading -> {
                binding.recyclerArchive.visibility = View.GONE
                binding.textArchiveStatus.visibility = View.VISIBLE
                binding.textArchiveStatus.text = getString(R.string.btn_testing)
            }
            is ArchiveUiState.Success -> {
                if (state.lines.isEmpty()) {
                    binding.recyclerArchive.visibility = View.GONE
                    binding.textArchiveStatus.visibility = View.VISIBLE
                    binding.textArchiveStatus.text = getString(R.string.archive_no_results)
                } else {
                    binding.textArchiveStatus.visibility = View.GONE
                    binding.recyclerArchive.visibility = View.VISIBLE
                    archiveAdapter.submitLines(state.lines)
                }
            }
            is ArchiveUiState.Error -> {
                binding.recyclerArchive.visibility = View.GONE
                binding.textArchiveStatus.visibility = View.VISIBLE
                binding.textArchiveStatus.text = state.message
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerLogs.adapter = null
        binding.recyclerArchive.adapter = null
        bindingNullable = null
    }
}
