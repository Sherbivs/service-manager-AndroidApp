package com.servicemanager.app.ui.settings

import app.cash.turbine.test
import com.servicemanager.app.data.model.MemoryInfoDto
import com.servicemanager.app.data.model.SystemInfoDto
import com.servicemanager.app.data.repository.ServiceRepository
import com.servicemanager.app.util.EncryptedPrefsHelper
import com.servicemanager.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val prefs: EncryptedPrefsHelper = mockk(relaxed = true)
    private val repo: ServiceRepository = mockk()
    private lateinit var viewModel: SettingsViewModel

    private val fakeSystemInfo =
        SystemInfoDto(
            hostname = "dev-machine",
            platform = "win32",
            localIp = "192.168.23.83",
            uptime = 3600.0,
            nodeVersion = "v20.0.0",
            memory = MemoryInfoDto(used = 512L, total = 1024L, free = 512L),
        )

    @Before
    fun setUp() {
        every { prefs.serverUrl } returns "http://192.168.23.83:3500"
        viewModel = SettingsViewModel(prefs, repo)
    }

    @Test
    fun `currentUrl delegates to prefs`() {
        assertEquals("http://192.168.23.83:3500", viewModel.currentUrl)
    }

    @Test
    fun `saveServerUrl stores trimmed url and emits saved event`() =
        runTest {
            viewModel.saved.test {
                viewModel.saveServerUrl("  http://192.168.23.83:3500  ")
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
            verify { prefs.serverUrl = "http://192.168.23.83:3500" }
        }

    @Test
    fun `saveServerUrl strips trailing slash`() =
        runTest {
            viewModel.saved.test {
                viewModel.saveServerUrl("http://192.168.23.83:3500/")
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
            verify { prefs.serverUrl = "http://192.168.23.83:3500" }
        }

    @Test
    fun `testConnection reaches Success state on successful ping`() =
        runTest {
            coEvery { repo.getSystemInfo() } returns Result.success(fakeSystemInfo)
            viewModel.testConnection("http://192.168.23.83:3500")
            viewModel.connectionTestStatus.test {
                assertEquals(SettingsViewModel.ConnectionStatus.Success, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `testConnection reaches Error state on failed ping`() =
        runTest {
            coEvery { repo.getSystemInfo() } returns Result.failure(RuntimeException("Connection refused"))
            viewModel.testConnection("http://192.168.23.83:3500")
            viewModel.connectionTestStatus.test {
                val state = awaitItem()
                assertTrue(state is SettingsViewModel.ConnectionStatus.Error)
                assertTrue((state as SettingsViewModel.ConnectionStatus.Error).message.isNotEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `testConnection restores original url on failure`() =
        runTest {
            val originalUrl = "http://192.168.23.83:3500"
            every { prefs.serverUrl } returns originalUrl
            coEvery { repo.getSystemInfo() } returns Result.failure(RuntimeException("Timeout"))
            viewModel.testConnection("http://10.0.0.99:3500")
            verify { prefs.serverUrl = originalUrl }
        }
}
