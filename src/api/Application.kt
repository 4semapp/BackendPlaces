package com.mkl

import api.*
import com.mkl.api.sign
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import logic.*


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

        post("/authenticate/google/{token}") {

            val service = GoogleAuthentication()
            val tokenHeader = getParameter("token")
            val googleUser = service.verify(tokenHeader!!)
            if (googleUser == null) {
                error("You could not be authenticated through google services.", HttpStatusCode.Unauthorized)
            } else {
                val found = findGoogleUser(googleUser.id)
                if (found != null) {
                    val token = sign(found)
                    val response = AuthenticationResponse(token, found.toDTO())
                    call.respond(response)
                } else {
                    val created = createUser(googleUser)
                    val token = sign(created)
                    val response = AuthenticationResponse(token, created.toDTO())
                    call.respond(response)
                }
            }
        }
    }
}

data class AuthenticationResponse(val token: String, val user: OutUser)
