@file:JvmName("Scraper")

package com.mkl

import com.google.gson.JsonParser
import khttp.get
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.geometry.Positions
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*
import javax.imageio.ImageIO


const val key = "AIzaSyCk7r-QjpVYLg83Pw5_D_jhvg_0XA_0QZQ"

val firstNames = mutableListOf(
        "Emma", "Ida", "Katrine", "Anna", "Sofie", "Julie", "Mille", "Laura", "Mathilde", "Emilie",
        "Mads", "Oliver", "Mathias", "Simon", "Frederik", "Tobias", "Sebastian", "Daniel", "Kristoffer", "Lukas"
)

val lastNames = mutableListOf(
        "Jensen", "Nielsen", "Hansen", "Pedersen", "Andersen", "Christensen", "Larsen", "Sørensen", "Rasmussen",
        "Jørgensen", "Petersen", "Madsen", "Kristensen", "Olsen", "Thomsen", "Christiansen", "Poulsen", "Johansen",
        "Møller", "Mortensen"
)

val cities = mutableListOf("Copenhagen", "Århus", "Odense", "Aalborg", "Frederiksberg", "Esbjerg", "Horsens",
        "Randers", "Roskilde", "Lyngby", "Helsingør", "Fredensborg", "Humlebæk", "Ribe", "Hornbæk", "Skagen")

fun <T> MutableList<T>.randomAndRemove(): T {
    return this.removeAt(Random().nextInt(this.size))
}

data class GooglePlace(val id: String, val name: String, val lat: Float, val lng: Float, val userRatings: Int, val
types: String)

private const val imageDir = "images"

private fun saveThumbnail(filePath: File, outputFile: File) {
    val originalImage = ImageIO.read(filePath)
    Thumbnails.of(originalImage)
            .crop(Positions.CENTER)
            .size(500, 500)
            .toFile(outputFile)
}

fun getPlacesFromGoogle(users: List<User>): List<Place> {
    val results = mutableListOf<Place>()
    var id = 0
    for (city in cities) {
        val response = get("https://maps.googleapis.com/maps/api/place/textsearch/json?query=${city.toLowerCase()}+point+of+interest&language=da&key=$key")

        val parser = JsonParser()
        val jsonResponse = parser.parse(response.text).asJsonObject.getAsJsonArray("results")

        for (result in jsonResponse) {
            id++
            val o = result.asJsonObject

            try {
                val place = GooglePlace(
                        o.getAsJsonPrimitive("place_id").asString,
                        o.getAsJsonPrimitive("name").asString,
                        o.getAsJsonObject("geometry").getAsJsonObject("location").getAsJsonPrimitive("lat").asFloat,
                        o.getAsJsonObject("geometry").getAsJsonObject("location").getAsJsonPrimitive("lng").asFloat,
                        o.getAsJsonPrimitive("user_ratings_total").asInt,
                        o.getAsJsonArray("types").toString())

                if (filter(place)) {

                    println("retrieved ${place.name} - ${place.id}")
                    val reference = o.getAsJsonArray("photos")[0].asJsonObject.get("photo_reference").asString
                    val imageData = get("https://maps.googleapis" +
                            ".com/maps/api/place/photo?maxwidth=1200&photoreference=$reference&key=$key").content


                    var inserted = transaction {
                        val created = Place.new {
                            title = place.name
                            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                            latitude = place.lat
                            longitude = place.lng
                            user = users.random()
                        }

                        created
                    }

                    val _fullName = id.toString() + "_full.jpg"
                    val _thumbName = id.toString() + "_thumb.jpg"


                    val fullFile = File(imageDir, _fullName)
                    fullFile.writeBytes(imageData)
                    println("full image written to ${fullFile.absolutePath}")

                    val thumbFile = File(imageDir, _thumbName)
                    saveThumbnail(fullFile, thumbFile)
                    println("thumb image written to ${thumbFile.absolutePath}")

                    transaction {
                        inserted.pictures = SizedCollection(
                                Picture.new {
                                    fullName = _fullName
                                    thumbName = _thumbName
                                }
                        )
                    }

                    results.add(inserted)

                }

            } catch (e: Exception) {
                println("an error occurred:")
                e.printStackTrace()
            }
        }
    }

    return results
}

fun filter(place: GooglePlace): Boolean {
    return place.userRatings > 75 || (
            place.types.contains("place_of_worship") ||
                    place.types.contains("park") ||
                    place.types.contains("church") ||
                    place.types.contains("museum") ||
                    place.types.contains("city_hall") ||
                    place.types.contains("local_government_office")
            )
}

fun deleteDatabase() {
    File("places.sqlite").delete()
    File(imageDir).deleteRecursively()
    File(imageDir, ".").mkdirs()
}

fun createDatabase() {
    connect()
    transaction {
        // addLogger(StdOutSqlLogger)
        SchemaUtils.create(Places)
        SchemaUtils.create(Pictures)
        SchemaUtils.create(Users)
        SchemaUtils.create(PlacesPicturesReference)
    }
}

fun createUsers(): MutableList<User> {
    val results = mutableListOf<User>()

    var id = 0
    while (firstNames.isNotEmpty()) {
        id++
        val firstName = firstNames.randomAndRemove()
        val lastName = lastNames.randomAndRemove()
        val fullName = "${firstName.toLowerCase()}-${lastName.toLowerCase()}@email.dk"
        val user = transaction {
            User.new {
                googleId = "google_$id"
                name = "$firstName $lastName"
                email = fullName
                picture = "http://lh3.googleusercontent.com/a-/AAuE7mC0Auhrz9x5RegAJjSdhQkGhqXpZrhTKq_vIOuJ=s96"
                locale = "da"
            }
        }

        println("Created user $fullName")
        results.add(user)
    }

    return results
}


fun main(args: Array<String>) {
    deleteDatabase()
    createDatabase()
    val users = createUsers()
    getPlacesFromGoogle(users)
}


