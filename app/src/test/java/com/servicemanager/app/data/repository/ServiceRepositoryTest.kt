package com.servicemanager.app.data.repository

import com.servicemanager.app.data.api.ApiService
import com.servicemanager.app.util.ResourceProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repo: ServiceRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val resourceProvider = mockk<ResourceProvider>()
        every { resourceProvider.getString(any()) } returns "Test error"
        every {
            resourceProvider.getString(any(), *anyVararg())
        } returns "Test error"

        repo =
            ServiceRepository(
                retrofit.create(ApiService::class.java),
                resourceProvider,
            )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getServices returns Success with parsed list`() =
        runTest {
            val json =
                """
                [{
                    "id":"tcb-shopify-dev","name":"Shopify Dev Server","project":"TCB Party Rental","description":"Local Shopify theme dev server",
                    "type":"web","url":"http://192.168.23.83:9292","healthCheck":"http://127.0.0.1:9292",
                    "autoRestart":true,"status":"running","managed":true,"pid":1234,
                    "startedAt":"2026-04-23T10:00:00Z","restartCount":0,"lastCheck":null,
                    "healthPercent":100,"avgLatency":50,"consecutiveFailures":0
                }]
                """.trimIndent()
            server.enqueue(
                MockResponse()
                    .setBody(json)
                    .setResponseCode(200),
            )
            val result = repo.getServices()
            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().size)
            assertEquals("tcb-shopify-dev", result.getOrThrow().first().id)
        }

    @Test
    fun `getServices returns Failure on HTTP 500`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500))
            val result = repo.getServices()
            assertTrue(result.isFailure)
        }

    @Test
    fun `getSystemInfo returns Success with parsed info`() =
        runTest {
            val json =
                """
                {
                    "hostname":"dev-box","platform":"win32","localIp":"192.168.1.100",
                    "uptime":7200.5,"nodeVersion":"v22.0.0",
                    "memory":{"used":512000000,"total":16000000000,"free":15488000000}
                }
                """.trimIndent()
            server.enqueue(
                MockResponse()
                    .setBody(json)
                    .setResponseCode(200),
            )
            val result = repo.getSystemInfo()
            assertTrue(result.isSuccess)
            assertEquals("dev-box", result.getOrThrow().hostname)
        }

    @Test
    fun `stopService returns Failure when success=false`() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setBody("""{"success":false,"message":"Service not running"}""")
                    .setResponseCode(200),
            )
            val result = repo.stopService("api")
            assertTrue(result.isFailure)
        }
}
