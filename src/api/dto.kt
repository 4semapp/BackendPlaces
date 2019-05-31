package api

import com.mkl.Picture
import com.mkl.Place

data class InPicture(
    var fullData: String,
    var thumbData: String
)

data class OutPicture(
    var id: Int,
    var fullData: String?, // nullable
    var thumbData: String
)

fun Picture.toDTO(): OutPicture {
    return OutPicture(
        id.value,
        thumbData,
        fullData
    )
}

fun Iterable<Picture>.toDTO(): Array<OutPicture> {
    return map(Picture::toDTO).toTypedArray()
}

data class InPlace(
    var title: String,
    var description: String,
    var lat: Float,
    var lon: Float,
    var pictures: Array<InPicture>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InPlace

        if (lat != other.lat) return false
        if (lon != other.lon) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (!pictures.contentEquals(other.pictures)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lat.hashCode()
        result = 31 * result + lon.hashCode()
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
    var lat: Float,
    var lon: Float,
    var pictures: Array<OutPicture>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OutPlace

        if (id != other.id) return false
        if (lat != other.lat) return false
        if (lon != other.lon) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (!pictures.contentEquals(other.pictures)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + lat.hashCode()
        result = 31 * result + lon.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + pictures.contentHashCode()
        return result
    }
}

fun Place.toDTO(pictures: Array<Picture>): OutPlace {
    return OutPlace(
        id.value,
        title,
        description,
        lat,
        lon,
        pictures.map(Picture::toDTO).toTypedArray()
    )
}