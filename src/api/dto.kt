package api

import com.mkl.Picture
import com.mkl.Place
import com.mkl.User

data class InPicture(
    var fullData: String,
    var thumbData: String
)

data class OutPicture(
    var id: Int,
    var fullData: String?,
    var thumbData: String?
)

fun Picture.toDTO(fullPictureData: Boolean = false): OutPicture {
    return OutPicture(
        id.value,
        if (fullPictureData) fullData else null,
        thumbData
    )
}

data class InPlace(
    var title: String,
    var description: String,
    var latitude: Float,
    var longitude: Float,
    var pictures: Array<InPicture>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InPlace

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (!pictures.contentEquals(other.pictures)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + pictures.contentHashCode()
        return result
    }
}


data class OutPlace(
    var id: Int,
    var title: String,
    var description: String,
    var latitude: Float,
    var longitude: Float,
    var pictures: Array<OutPicture>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OutPlace

        if (id != other.id) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (!pictures.contentEquals(other.pictures)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + pictures.contentHashCode()
        return result
    }
}

fun Place.toDTO(fullPictureData: Boolean = false): OutPlace {
    return OutPlace(
        id.value,
        title,
        description,
        latitude,
        longitude,
        pictures.map { it.toDTO(fullPictureData) }.toTypedArray()
    )
}

data class OutUser(
    var id: Int,
    var googleId: String,
    var name: String,
    var email: String,
    var picture: String,
    var locale: String
)

fun User.toDTO(): OutUser {
    return OutUser(
        this.id.value,
        this.googleId,
        this.name,
        this.email,
        this.picture,
        this.locale
    )
}