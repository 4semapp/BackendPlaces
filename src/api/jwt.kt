package com.mkl.api

import api.OutUser
import com.google.api.client.util.Base64
import com.mkl.User
import com.mkl.Users
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.jetbrains.exposed.dao.EntityID
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

fun verify(token: String): User {

    try {
        val claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .body

        val user = User(EntityID(claims.get("id", Int::class.java), Users))
        user.email = claims["email"] as String
        user.googleId = claims["googleId"] as String
        user.name = claims["name"] as String
        user.locale = claims["locale"] as String
        user.picture = claims["picture"] as String

        return user

    } catch (e: Exception) {
        throw JwtException(e.message!!)
    }
}

class JwtException(message: String) : Exception(message)