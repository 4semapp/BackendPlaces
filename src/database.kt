package com.mkl

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection


object Pictures : IntIdTable() {
    val thumbData: Column<String> = text("thumbData")
    val fullData: Column<String> = text("fullData")
}

class Picture(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Picture>(Pictures)

    var thumbData by Pictures.thumbData
    var fullData by Pictures.fullData
}

object Places : IntIdTable() {
    val title: Column<String> = varchar("title", 255)
    val description: Column<String> = text("description")
    val lat: Column<Float> = float("latitude")
    val lon: Column<Float> = float("longitude")
}

class Place(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Place>(Places)

    var title by Places.title
    var description by Places.description
    var lat by Places.lat
    var lon by Places.lon
}

fun connect(): Database {
    val connection = Database.connect("jdbc:sqlite:places.sqlite", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel =
        Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED
    return connection
}

fun main() {
    connect()
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Places)
        SchemaUtils.create(Pictures)
    }
}