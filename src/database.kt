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
    val thumbName = text("thumbName")
    val fullName = text("fullName")
}

class Picture(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Picture>(Pictures)

    var thumbName by Pictures.thumbName
    var fullName by Pictures.fullName
}

object PlacesPicturesReference : Table() {
    val place = reference("place", Places).primaryKey(0)
    val picture = reference("picture", Pictures).primaryKey(1)
}

object Places : IntIdTable() {
    val title = varchar("title", 255)
    val description = text("description")
    val latitude = float("latitude")
    val longitude = float("longitude")
    val user = reference("user", Users)
}

class Place(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Place>(Places)

    var title by Places.title
    var description by Places.description
    var latitude by Places.latitude
    var longitude by Places.longitude
    var user by User referencedOn Places.user
    var pictures by Picture via PlacesPicturesReference
}

object Users : IntIdTable() {
    val googleId: Column<String> = varchar("googleId", 255).uniqueIndex()
    val name: Column<String> = varchar("name", 255)
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val picture: Column<String> = varchar("picture", 255)
    val locale: Column<String> = varchar("locale", 5)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var googleId by Users.googleId
    var name by Users.name
    var email by Users.email
    var picture by Users.picture
    var locale by Users.locale
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
        SchemaUtils.create(Users)
        SchemaUtils.create(PlacesPicturesReference)
    }
}