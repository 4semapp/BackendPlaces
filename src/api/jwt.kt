package api

import com.google.api.client.util.Base64
import com.mkl.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import logic.getUser
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import java.io.File
import java.security.Key
import java.security.SecureRandom


private val saveFile = File("secret.key")
private var secret: Key? = null

private fun getSecret(): Key {
    if (secret == null) {
        secret = if (saveFile.exists()) {
            val random = Base64.decodeBase64(saveFile.readText())
            Keys.hmacShaKeyFor(random)
        } else {
            val random = generateBytes()
            val base64 = Base64.encodeBase64String(random)
            saveFile.writeText(base64)
            Keys.hmacShaKeyFor(random)
        }
    }

    return secret!!
}

private fun generateBytes(): ByteArray {
    val array = ByteArray(256) { 0.toByte() }
    SecureRandom().nextBytes(array)
    return array
}

fun sign(user: User): String {

    val claims = mapOf(
        "id" to user.id.value,
        "googleId" to user.googleId,
        "name" to user.name,
        "email" to user.email,
        "locale" to user.locale
    )

    return Jwts.builder()
        .setClaims(claims)
        .signWith(getSecret())
        .compact()
}

suspend fun verify(token: String): User? {

    return try {
        val claims = Jwts.parser()
            .setSigningKey(getSecret())
            .parseClaimsJws(token)
            .body

        val id = claims.get("id", Integer::class.java).toInt()
        transaction {
            getUser(id)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

class JwtException(message: String) : Exception(message)