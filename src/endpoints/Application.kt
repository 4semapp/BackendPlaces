package com.mkl

import endpoints.PostPicture
import endpoints.PostUser
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val client = HttpClient(Apache) {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, GsonConverter())
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }


        post("/user") {
            val postUser = call.receive<PostUser>()
            call.respond(CreateUser(postUser))
        }

        post("/picture") {
            val postPicture = call.receive<PostPicture>()
            call.respond(CreatePicture(postPicture))
        }
    }
}

fun CreatePicture(postPicture: PostPicture): PostPicture {
    StartTransaction()
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create( Pictures )

        Pictures.insert {
            it[data] = postPicture.data
        } get Pictures.id
    }
    return postPicture
}

fun CreateUser(postUser: PostUser): PostUser {
    StartTransaction()
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create( Users )

        val pictureTest = Users.insert {
            it[email] = postUser.email
        } get Users.id
    }
    return postUser
}

fun StartTransaction() {
    Database.connect("jdbc:sqlite:/home/mkl/Dropbox/Datamatiker/4semester/kotlin/places_db/Places.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED
}

