package logic

import api.InPicture
import api.InPlace
import com.mkl.*
import com.sun.org.apache.xml.internal.security.utils.Base64
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun getPlaces(): List<Place> {
    return transaction { Place.all().toList() }
}

fun countPosts(user: User): Int {
    // not an optimal solution, cannot use Places.select {Place.user eq user}
    return transaction { Places.selectAll().filter { Place.wrapRow(it).user.id == user.id }.count() }
}

fun getHomePage(): List<Place> {
    return transaction { Places.selectAll().limit(3).map { Place.wrapRow(it) } }
}

fun search(term: String): List<Place> {
    return transaction {
        Place.find { Places.title like "%$term%" }.toList()
    }
}

fun createUser(googleUser: GoogleUser): User {
    return transaction {
        User.new {
            googleId = googleUser.id
            name = googleUser.name
            email = googleUser.email
            locale = googleUser.locale
            picture = googleUser.picture
        }
    }
}

fun getUser(id: Int): User? {
    return transaction {
        User.findById(id)
    }
}

fun findGoogleUser(googleId: String): User? {
    return transaction {
        val results = User.find { Users.googleId eq googleId }
        if (!results.empty())
            results.first()
        else null
    }
}

private const val imageDir = "images"

fun createPicture(inPicture: InPicture): Picture {
    val picture = Picture.new {
        fullName = ""
        thumbName = ""
    }
    val fullName = "${picture.id}_full.jpg"
    val thumbName = "${picture.id}_thumb.jpg"
    picture.fullName = fullName
    picture.thumbName = thumbName
    File(imageDir, fullName).writeBytes(Base64.decode(inPicture.fullData))
    File(imageDir, thumbName).writeBytes(Base64.decode(inPicture.thumbData))
    return picture
}

fun createPlace(author: User, inPlace: InPlace): Place {

    var pictures = transaction { inPlace.pictures.map { createPicture(it) } }
    var place = transaction {
        val created = Place.new {
            title = inPlace.title
            description = inPlace.description
            latitude = inPlace.latitude
            longitude = inPlace.longitude
            user = author
        }

        created
    }

    transaction {
        place.pictures = SizedCollection(pictures)
    }

    return place
}