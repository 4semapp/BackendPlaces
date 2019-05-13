package endpoints

import org.jetbrains.exposed.dao.EntityID

//DAO's
data class PostUser(var email: String)

data class PostPicture(var data: String)

data class PostPlace(var lat: Int, var lon: Int, var title: String, var desc: String, var id: Int?=null)