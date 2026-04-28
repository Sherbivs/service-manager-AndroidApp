package com.servicemanager.app.ui.logs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.servicemanager.app.R
import com.servicemanager.app.databinding.DialogArchiveSearchBinding
import com.servicemanager.app.databinding.FragmentLogsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class LogsFragment : Fragment(R.layout.fragment_logs) {
    private var bindingNullable: FragmentLogsBinding? = null
    private val binding get() = bindingNullable!!

    private val viewModel: LogsViewModel by viewModels()
    private val logsAdapter = LogLineAdapter()

    private var currentLogs: List<String> = emptyList()
    private var filterJob: Job? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        bindingNullable = FragmentLogsBinding.bind(view)

        setupRecycler()
        setupChips()
        setupLocalSearch()
        setupArchiveFab()
        setupCopyFab()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadLogs(selectedLineCount())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun setupRecycler() {
        binding.recyclerLogs.apply {
            adapter = logsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupChips() {
        binding.chipGroupLines.setOnCheckedStateChangeListener { _, _ ->
            viewModel.loadLogs(selectedLineCount())
        }
    }

    private fun setupLocalSearch() {
        binding.searchLocal.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    scheduleFilter(newText)
                    return true
                }
            },
        )
    }

    private fun scheduleFilter(query: String?) {
        filterJob?.cancel()
        filterJob =
            viewLifecycleOwner.lifecycleScope.launch {
                delay(120)
                filterLogs(query)
            }
    }

    private fun filterLogs(query: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val filtered =
                withContext(Dispatchers.Default) {
                    if (query.isNullOrBlank()) {
                        currentLogs
                    } else {
                        currentLogs.filter { it.contains(query, ignoreCase = true) }
                    }
                }
            logsAdapter.submitLines(filtered)
        }
    }

    private fun setupArchiveFab() {
        binding.fabArchive.setOnClickListener { showArchiveDialog() }
    }

    private fun showArchiveDialog() {
        val dialogBinding = DialogArchiveSearchBinding.inflate(LayoutInflater.from(requireContext()))
        val archiveAdapter = LogLineAdapter()

        dialogBinding.recyclerArchive.apply {
            adapter = archiveAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Setup Dropdown for Service ID
        val serviceIds = viewModel.availableServices.value
        val dropdownAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, serviceIds)
        dialogBinding.editServiceId.setAdapter(dropdownAdapter)

        // Setup Dropdown for Log Level
        val levelOptions = listOf(
            getString(R.string.level_all),
            "error",
            "warn",
            "info",
            "debug",
        )
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levelOptions)
        dialogBinding.editLogLevel.setAdapter(levelAdapter)
        dialogBinding.editLogLevel.setText(getString(R.string.level_all), false)

        val projectOptions = listOf(getString(R.string.level_all)) + viewModel.availableProjects.value
        val projectAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, projectOptions)
        dialogBinding.editProject.setAdapter(projectAdapter)
        dialogBinding.editProject.setText(getString(R.string.level_all), false)

        // Automatically trigger search when an item is selected from dropdown
        dialogBinding.editServiceId.setOnItemClickListener { _, _, position, _ ->
            val selectedId = dropdownAdapter.getItem(position)
            if (!selectedId.isNullOrEmpty()) {
                viewModel.searchArchive(selectedId, "")
            }
        }

        val dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.root)
                .create()

        dialogBinding.btnSearchArchive.setOnClickListener {
            val serviceId =
                dialogBinding.editServiceId.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()
            val query =
                dialogBinding.editArchiveQuery.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()
            val rawLevel = dialogBinding.editLogLevel.text?.toString()?.trim().orEmpty()
            val level = if (rawLevel == getString(R.string.level_all)) "" else rawLevel
            val rawProject = dialogBinding.editProject.text?.toString()?.trim().orEmpty()
            val project = if (rawProject == getString(R.string.level_all)) "" else rawProject

            if (serviceId.isNotEmpty()) {
                viewModel.searchArchive(serviceId, query, level)
            } else {
                // Global cross-tenant search when no service is selected
                viewModel.searchGlobalArchive(query = query, project = project, level = level)
            }
        }

        dialogBinding.btnArchivePrev.setOnClickListener {
            viewModel.loadPreviousArchivePage()
        }

        dialogBinding.btnArchiveNext.setOnClickListener {
            viewModel.loadNextArchivePage()
        }

        val archiveCollectorJob: Job =
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.archiveState.collect { state ->
                    when (state) {
                        is ArchiveUiState.Loading -> {
                            dialogBinding.textArchiveStatus.visibility = View.VISIBLE
                            dialogBinding.textArchiveStatus.text = getString(R.string.archive_searching)
                            dialogBinding.layoutArchivePagination.visibility = View.GONE
                            dialogBinding.recyclerArchive.visibility = View.GONE
                        }
                        is ArchiveUiState.Success -> {
                            if (state.rows.isEmpty()) {
                                dialogBinding.textArchiveStatus.visibility = View.VISIBLE
                                dialogBinding.textArchiveStatus.text = getString(R.string.archive_no_results)
                                dialogBinding.layoutArchivePagination.visibility = View.GONE
                                dialogBinding.recyclerArchive.visibility = View.GONE
                            } else {
                                dialogBinding.textArchiveStatus.visibility = View.VISIBLE
                                dialogBinding.textArchiveStatus.text =
                                resources.getQuantityString(
                                    R.plurals.archive_results_count,
                                    state.rows.size,
                                    state.rows.size
                                )
                                dialogBinding.layoutArchivePagination.visibility = View.VISIBLE
                                val start = state.offset + 1
                                val end = (state.offset + state.rows.size).coerceAtMost(state.total)
                                dialogBinding.textArchivePageInfo.text =
                                    getString(R.string.archive_page_info, start, end, state.total)
                                dialogBinding.btnArchivePrev.isEnabled = state.offset > 0
                                dialogBinding.btnArchiveNext.isEnabled = (state.offset + state.limit) < state.total
                                dialogBinding.recyclerArchive.visibility = View.VISIBLE
                                val lines = state.rows.map { row ->
                                    val prefix = buildString {
                                        append("[${row.logLevel.uppercase()}]")
                                        if (!row.project.isNullOrBlank()) append(" [${row.project}]")
                                        else if (!row.serviceId.isNullOrBlank()) append(" [${row.serviceId}]")
                                    }
                                    "$prefix ${row.line}"
                                }
                                archiveAdapter.submitLines(lines)
                            }
                        }
                        is ArchiveUiState.Error -> {
                            dialogBinding.textArchiveStatus.visibility = View.VISIBLE
                            dialogBinding.textArchiveStatus.text = state.message
                            dialogBinding.layoutArchivePagination.visibility = View.GONE
                            dialogBinding.recyclerArchive.visibility = View.GONE
                        }
                        else -> {}
                    }
                }
            }

        dialog.setOnDismissListener {
            archiveCollectorJob.cancel()
        }

        dialog.show()
    }

    private fun setupCopyFab() {
        binding.fabCopyLogs.setOnClickListener {
            val lines = currentLogs
            if (lines.isNotEmpty()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("logs", lines.joinToString("\n"))
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
                binding.textError.visibility = View.GONE
            }
            is LogsUiState.Success -> {
                binding.textError.visibility = View.GONE
                currentLogs = state.lines
                filterLogs(binding.searchLocal.query?.toString())
                if (state.lines.isNotEmpty()) {
                    binding.recyclerLogs.post {
                        binding.recyclerLogs.scrollToPosition(logsAdapter.itemCount - 1)
                    }
                }
            }
            is LogsUiState.Error -> {
                binding.textError.visibility = View.VISIBLE
                binding.textError.text = state.message
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        filterJob?.cancel()
        binding.recyclerLogs.adapter = null
        bindingNullable = null
    }
}
