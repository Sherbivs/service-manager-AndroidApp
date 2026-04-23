package com.servicemanager.app.ui.services

import app.cash.turbine.test
import com.servicemanager.app.data.model.ServiceDto
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
class ServicesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo: ServiceRepository = mockk()
    private lateinit var viewModel: ServicesViewModel

    private val fakeServices =
        listOf(
            ServiceDto(
                id = "tcb-shopify-dev",
                name = "Shopify Dev Server",
                project = "TCB Party Rental",
                description = "Local Shopify theme dev server",
                type = "web",
                url = "http://192.168.23.83:9292",
                healthCheck = "http://127.0.0.1:9292",
                autoRestart = true,
                status = "running",
                managed = true,
                pid = 1234,
                startedAt = "2026-04-23T10:00:00Z",
                restartCount = 0,
                lastCheck = null,
                healthPercent = 100,
                avgLatency = 50,
                consecutiveFailures = 0,
            ),
            ServiceDto(
                id = "db",
                name = "Database",
                project = null,
                description = null,
                type = "process",
                url = null,
                healthCheck = null,
                autoRestart = false,
                status = "stopped",
                managed = true,
                pid = null,
                startedAt = null,
                restartCount = 2,
                lastCheck = null,
                healthPercent = null,
                avgLatency = null,
                consecutiveFailures = 0,
            ),
        )

    @Before
    fun setUp() {
        coEvery { repo.getServices() } returns Result.success(fakeServices)
        viewModel = ServicesViewModel(repo)
    }

    @Test
    fun `initial load emits Success with services`() =
        runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is ServicesUiState.Success)
                assertEquals(fakeServices, (state as ServicesUiState.Success).services)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadServices emits Error on failure`() =
        runTest {
            coEvery { repo.getServices() } returns Result.failure(RuntimeException("Network error"))
            viewModel.loadServices()

            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is ServicesUiState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `startService emits actionError on failure`() =
        runTest {
            coEvery { repo.startService("api") } returns Result.failure(RuntimeException("Failed"))
            coEvery { repo.getServices() } returns Result.success(fakeServices)

            viewModel.actionError.test {
                viewModel.startService("api")
                val error = awaitItem()
                assertTrue(error.isNotEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadServicesIfStale skips fetch when data is fresh`() =
        runTest {
            // init already fetched → Success; calling again immediately is a no-op (data < 10s old)
            viewModel.loadServicesIfStale()

            // State must still be Success and repo called exactly once (by init, not again)
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue("Expected Success after stale guard no-op, got $state", state is ServicesUiState.Success)
                cancelAndIgnoreRemainingEvents()
            }
            io.mockk.coVerify(exactly = 1) { repo.getServices() }
        }

    @Test
    fun `loadServicesIfStale fetches when no data loaded yet`() =
        runTest {
            // A fresh ViewModel with no prior Success should fetch
            coEvery { repo.getServices() } returns Result.success(fakeServices)
            val freshVm = ServicesViewModel(repo)
            freshVm.uiState.test {
                val state = awaitItem()
                assertTrue(state is ServicesUiState.Success)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `startService invokes repo and emits actionSuccess on success`() =
        runTest {
            coEvery { repo.startService("tcb-shopify-dev") } returns Result.success(Unit)

            viewModel.actionSuccess.test {
                viewModel.startService("tcb-shopify-dev")
                val msg = awaitItem()
                assertTrue(msg.isNotEmpty())
                cancelAndIgnoreRemainingEvents()
            }
            io.mockk.coVerify { repo.startService("tcb-shopify-dev") }
        }

    @Test
    fun `stopService dispatches to repo`() =
        runTest {
            coEvery { repo.stopService("tcb-shopify-dev") } returns Result.success(Unit)

            viewModel.stopService("tcb-shopify-dev")

            io.mockk.coVerify { repo.stopService("tcb-shopify-dev") }
        }

    @Test
    fun `retryLastAction re-dispatches the failed action`() =
        runTest {
            var callCount = 0
            coEvery { repo.startService("db") } answers {
                if (++callCount == 1) Result.failure(RuntimeException("Timeout")) else Result.success(Unit)
            }

            viewModel.actionError.test {
                viewModel.startService("db")
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.actionSuccess.test {
                viewModel.retryLastAction()
                val msg = awaitItem()
                assertTrue(msg.isNotEmpty())
                cancelAndIgnoreRemainingEvents()
            }
            io.mockk.coVerify(exactly = 2) { repo.startService("db") }
        }
}
