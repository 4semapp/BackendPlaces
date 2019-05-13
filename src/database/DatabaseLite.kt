package com.mkl

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date


object Pictures: IntIdTable() {
    val data: Column<String> = text("data")
}

class Picture(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Picture>(Pictures)
    var data by Pictures.data
}

object Users: IntIdTable() {
    val email: Column<String> = varchar("email", 100).primaryKey()
}

//should have string as ID
class User(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<User>(Users)
    var email by Users.email
}

object Places: IntIdTable() {
    val lat: Column<Int> = integer("latitude")
    val lon: Column<Int> = integer("longitude")
    val title: Column<String> = varchar("title", 255)
    val description: Column<String> = text("description")
}

class Place(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Place>(Places)
    var lat by Places.lat
    var lon by Places.lon
    var title by Places.title
    var desc by Places.description
}

fun main(args: Array<String>) {
    Database.connect("jdbc:sqlite:/home/mkl/Dropbox/Datamatiker/4semester/kotlin/places_db/Places.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.create( Users )

        val pictureTest = Users.insert {
            it[email] = "testboi@wopsi.com"
        } get Users.id

        println("Cities: ${Users.selectAll()}")
    }
    // https://github.com/JetBrains/Exposed/wiki/DSL#overview
    //
}