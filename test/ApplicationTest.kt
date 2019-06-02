package com.mkl

import api.module
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ module() }) {
            handleRequest(HttpMethod.Get, "/").apply {
                //assertEquals(HttpStatusCode.OK, response.status())
                //assertEquals("HELLO WORLD!", response.content)
            }
        }
    }
}
