package logic

import api.InPicture
import api.InPlace
import com.mkl.Picture
import com.mkl.Place
import com.mkl.Places
import org.jetbrains.exposed.sql.transactions.transaction

fun search(term: String): List<Place> {
    return transaction {
        Place.find { Places.title like "%$term%" }.asSequence().toList()
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