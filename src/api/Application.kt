package com.mkl

import api.*
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import logic.createPlace
import logic.search


fun main(args: Array<String>) {
    connect()
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {

    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter())
    }

    routing {
        get("/") {
            authenticate { call, user ->
                call.respondText("Hello ${user.name}", contentType = ContentType.Text.Plain)
            }
        }

        post("/places") {
            val posted = call.receive<InPlace>()
            call.respond(createPlace(posted).toDTO(arrayOf()))
        }

        get("/places/{title}") {
            val title = getParameter("title")
            val results = search(title!!)
            val out =
                results.map { OutPlace(it.id.value, it.title, it.description, it.lat, it.lon, arrayOf()) }
            call.respond(out)
        }
    }
}
