package com.mkl.api

import com.mkl.User
import com.mkl.Users
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.jetbrains.exposed.dao.EntityID
import java.security.Key


val secret: Key = Keys.hmacShaKeyFor(byteArrayOf(1, 2, 3, 4, 5))

fun encode(user: User): String {

    val claims = mapOf(
        "id" to user.id,
        "googleId" to user.googleId,
        "name" to user.name,
        "email" to user.email,
        "locale" to user.locale
    )

    return Jwts.builder()
        .setClaims(claims)
        .signWith(secret)
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