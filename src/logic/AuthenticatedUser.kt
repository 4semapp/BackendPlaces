package logic

data class AuthenticatedUser(
    val id: String,
    val name: String,
    val email: String,
    val picture: String,
    val locale: String
)