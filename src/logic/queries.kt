package logic

import api.InPicture
import api.InPlace
import com.mkl.*
import org.jetbrains.exposed.sql.transactions.transaction

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

fun findGoogleUser(googleId: String): User? {
    return transaction {
        val results = User.find { Users.googleId eq googleId }
        if (!results.empty())
            results.first()
        else null
    }
}

fun createPicture(inPicture: InPicture): Picture {
    return Picture.new {
        thumbData = inPicture.thumbData
        fullData = inPicture.fullData
    }
}

fun createPlace(inPlace: InPlace): Place {
    return transaction {
        Place.new {
            title = inPlace.title
            description = inPlace.description
            lat = inPlace.lat
            lon = inPlace.lon
        }
    }
}