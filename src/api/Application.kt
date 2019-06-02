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
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import logic.*
import org.jetbrains.exposed.sql.transactions.transaction


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

        get("/places") {
            authenticate { call, user ->
                call.respond(getPlaces().map { transaction { it.toDTO() } })
            }
        }

        post("/places") {
            authenticate { call, user ->
                val posted = call.receive<InPlace>()
                val place = transaction { createPlace(user, posted) }
                call.respond(HttpStatusCode.Created, transaction { place.toDTO(false) })
            }
        }

        get("/places/search/{term}") {
            val title = getParameter("term")
            val results = search(title!!)
            val out = results.map { transaction { it.toDTO(false) } }
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
                    val response = createAuthResponse(token, found)
                    call.respond(response)
                    print(response)
                } else {
                    val created = createUser(googleUser)
                    val token = sign(created)
                    val response = createAuthResponse(token, created)
                    call.respond(response)
                    print(response)
                }
            }
        }
    }
}

data class AuthenticationResponse(
    var id: Int,
    var googleId: String,
    var name: String,
    var email: String,
    var picture: String,
    var locale: String,
    var token: String
)

fun createAuthResponse(token: String, user: User): AuthenticationResponse {
    return AuthenticationResponse(
        user.id.value,
        user.googleId,
        user.name,
        user.email,
        user.picture,
        user.locale,
        token
    )
}
