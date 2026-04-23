package com.servicemanager.app.ui.system

import app.cash.turbine.test
import com.servicemanager.app.data.model.MemoryInfoDto
import com.servicemanager.app.data.model.SystemInfoDto
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
class SystemViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo: ServiceRepository = mockk()
    private lateinit var viewModel: SystemViewModel

    private val fakeSystemInfo =
        SystemInfoDto(
            hostname = "dev-box",
            platform = "win32",
            localIp = "192.168.23.83",
            uptime = 7380.0,
            nodeVersion = "v20.11.0",
            memory = MemoryInfoDto(used = 512L * 1024 * 1024, total = 2048L * 1024 * 1024, free = 1536L * 1024 * 1024),
        )

    @Before
    fun setUp() {
        coEvery { repo.getSystemInfo() } returns Result.success(fakeSystemInfo)
        viewModel = SystemViewModel(repo)
    }

    @Test
    fun `initial load emits Success with system info`() =
        runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is SystemUiState.Success)
                assertEquals(fakeSystemInfo, (state as SystemUiState.Success).info)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadSystemInfo emits Error on failure`() =
        runTest {
            coEvery { repo.getSystemInfo() } returns Result.failure(RuntimeException("Network error"))
            viewModel.loadSystemInfo()

            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is SystemUiState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadSystemInfo re-fetches and emits updated Success`() =
        runTest {
            val updatedInfo = fakeSystemInfo.copy(hostname = "new-host")
            coEvery { repo.getSystemInfo() } returns Result.success(updatedInfo)
            viewModel.loadSystemInfo()

            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is SystemUiState.Success)
                assertEquals("new-host", (state as SystemUiState.Success).info.hostname)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
