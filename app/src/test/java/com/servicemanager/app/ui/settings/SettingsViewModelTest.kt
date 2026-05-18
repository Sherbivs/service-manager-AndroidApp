package com.servicemanager.app.ui.settings

import app.cash.turbine.test
import com.servicemanager.app.data.model.MemoryInfoDto
import com.servicemanager.app.data.model.SystemInfoDto
import com.servicemanager.app.data.repository.ServiceRepository
import com.servicemanager.app.util.MainDispatcherRule
import com.servicemanager.app.util.SecurePrefsHelper
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

    private val prefs: SecurePrefsHelper = mockk(relaxed = true)
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
        every { prefs.serverUrl } returns "http://sensaimanager.drip:3500"
        every { prefs.serverScheme } returns "http"
        every { prefs.serverHost } returns "sensaimanager.drip"
        every { prefs.serverPort } returns 3500
        every { prefs.connectTimeoutSeconds } returns 10
        every { prefs.readTimeoutSeconds } returns 10
        viewModel = SettingsViewModel(prefs, repo)
    }

    @Test
    fun `currentUrl delegates to prefs`() {
        assertEquals("http://sensaimanager.drip:3500", viewModel.currentUrl)
    }

    @Test
    fun `current timeouts delegate to prefs`() {
        assertEquals("http", viewModel.currentServerScheme)
        assertEquals("sensaimanager.drip", viewModel.currentServerHost)
        assertEquals(3500, viewModel.currentServerPort)
        assertEquals(10, viewModel.currentConnectTimeoutSeconds)
        assertEquals(10, viewModel.currentReadTimeoutSeconds)
    }

    @Test
    fun `saveNetworkSettings stores trimmed values and emits saved event`() =
        runTest {
            viewModel.saved.test {
                viewModel.saveNetworkSettings(
                    serverScheme = "http",
                    serverHost = "sensaimanager.drip",
                    serverPort = 3500,
                    connectTimeoutSeconds = 15,
                    readTimeoutSeconds = 20,
                )
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
            verify { prefs.serverScheme = "http" }
            verify { prefs.serverHost = "sensaimanager.drip" }
            verify { prefs.serverPort = 3500 }
            verify { prefs.connectTimeoutSeconds = 15 }
            verify { prefs.readTimeoutSeconds = 20 }
        }

    @Test
    fun `saveNetworkSettings strips trailing slash`() =
        runTest {
            viewModel.saved.test {
                viewModel.saveNetworkSettings(
                    url = "http://sensaimanager.drip:3500/",
                    connectTimeoutSeconds = 10,
                    readTimeoutSeconds = 10,
                )
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
            verify { prefs.serverUrl = "http://sensaimanager.drip:3500" }
        }

    @Test
    fun `testConnection reaches Success state on successful ping`() =
        runTest {
            coEvery { repo.getSystemInfo() } returns Result.success(fakeSystemInfo)
            viewModel.testConnection(
                serverScheme = "http",
                serverHost = "sensaimanager.drip",
                serverPort = 3500,
                connectTimeoutSeconds = 10,
                readTimeoutSeconds = 10,
            )
            viewModel.connectionTestStatus.test {
                assertEquals(SettingsViewModel.ConnectionStatus.Success, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `testConnection reaches Error state on failed ping`() =
        runTest {
            coEvery { repo.getSystemInfo() } returns Result.failure(RuntimeException("Connection refused"))
            viewModel.testConnection(
                serverScheme = "http",
                serverHost = "sensaimanager.drip",
                serverPort = 3500,
                connectTimeoutSeconds = 10,
                readTimeoutSeconds = 10,
            )
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
            val originalUrl = "http://sensaimanager.drip:3500"
            every { prefs.serverUrl } returns originalUrl
            every { prefs.connectTimeoutSeconds } returns 10
            every { prefs.readTimeoutSeconds } returns 10
            coEvery { repo.getSystemInfo() } returns Result.failure(RuntimeException("Timeout"))
            viewModel.testConnection(
                url = "http://192.168.23.106:3500",
                connectTimeoutSeconds = 5,
                readTimeoutSeconds = 5,
            )
            verify { prefs.serverUrl = originalUrl }
            verify { prefs.connectTimeoutSeconds = 10 }
            verify { prefs.readTimeoutSeconds = 10 }
        }
}
