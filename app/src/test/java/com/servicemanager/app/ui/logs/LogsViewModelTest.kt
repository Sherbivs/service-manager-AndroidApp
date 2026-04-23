package com.servicemanager.app.ui.logs

import app.cash.turbine.test
import com.servicemanager.app.data.repository.ServiceRepository
import com.servicemanager.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo: ServiceRepository = mockk()
    private lateinit var viewModel: LogsViewModel

    private val fakeLines = listOf("[INFO] Server started", "[WARN] High memory usage", "[ERROR] Connection refused")

    @Before
    fun setUp() {
        coEvery { repo.getGlobalLogs(any()) } returns Result.success(fakeLines)
        viewModel = LogsViewModel(repo)
    }

    @Test
    fun `initial load emits Success with log lines`() =
        runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is LogsUiState.Success)
                assertEquals(fakeLines, (state as LogsUiState.Success).lines)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadLogs emits Error on failure`() =
        runTest {
            coEvery { repo.getGlobalLogs(any()) } returns Result.failure(RuntimeException("Network error"))
            viewModel.loadLogs()

            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is LogsUiState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadLogs with custom line count passes count to repo`() =
        runTest {
            coEvery { repo.getGlobalLogs(50) } returns Result.success(fakeLines.take(1))
            viewModel.loadLogs(50)

            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is LogsUiState.Success)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `searchArchive emits Success with results`() =
        runTest {
            val archiveLines = listOf("[INFO] archived log entry")
            coEvery { repo.searchArchiveLogs("shopify", "error") } returns Result.success(archiveLines)
            viewModel.searchArchive("shopify", "error")

            viewModel.archiveState.test {
                val state = awaitItem()
                assertTrue(state is ArchiveUiState.Success)
                assertEquals(archiveLines, (state as ArchiveUiState.Success).lines)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `searchArchive emits Error on failure`() =
        runTest {
            coEvery { repo.searchArchiveLogs(any(), any()) } returns Result.failure(RuntimeException("Not found"))
            viewModel.searchArchive("unknown-service", "query")

            viewModel.archiveState.test {
                val state = awaitItem()
                assertTrue(state is ArchiveUiState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `archiveState starts as Idle`() =
        runTest {
            viewModel.archiveState.test {
                assertTrue(awaitItem() is ArchiveUiState.Idle)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
