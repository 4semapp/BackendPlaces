package com.mkl

import endpoints.PostPicture
import endpoints.PostPlace
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

        get("/search/{title}") {
            val title :String? = call.parameters["title"]
            var searchResults: List<Place>? = null
            val placeResults: ArrayList<PostPlace> = ArrayList()
            if(title == null) {
                call.respond("no title was provided")
            }
            StartTransaction()
            transaction {
                searchResults = Place.find { Places.title eq title!! }.asSequence().toList()
            }
            if (searchResults == null) {
                call.respond("no titles were found in db")
            }

            searchResults!!.forEach {
                val tmpRes = PostPlace(it.lat, it.lon, it.title, it.desc, it.id.value)
                placeResults.add(tmpRes)
            }
            call.respond(placeResults)
        }

        post("/user") {
            val postUser = call.receive<PostUser>()
            call.respond(CreateUser(postUser))
        }

        post("/picture") {
            val postPicture = call.receive<PostPicture>()
            call.respond(CreatePicture(postPicture))
        }

        post("/place") {
            val postPlace = call.receive<PostPlace>()
            call.respond(CreatePlace(postPlace))
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
        } //get Pictures.id
    }
    return postPicture
}

fun CreatePlace(postPlace: PostPlace): PostPlace {
    StartTransaction()
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create( Places )

        Places.insert {
            it[lat] = postPlace.lat
            it[lon] = postPlace.lat
            it[title] = postPlace.title
            it[description] = postPlace.desc
        }
    }
    return postPlace
}

fun CreateUser(postUser: PostUser): PostUser {
    StartTransaction()
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create( Users )

        val pictureTest = Users.insert {
            it[email] = postUser.email
        } //get Users.id
    }
    return postUser
}

fun StartTransaction() {
    Database.connect("jdbc:sqlite:/home/mkl/Dropbox/Datamatiker/4semester/kotlin/places_db/Places.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED
}

