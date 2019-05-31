package logic

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import java.util.*


const val CLIENT_ID = "361668683148-d5dtbmrdtt5jnnnt4pirh2nod755c3q9.apps.googleusercontent.com"

class GoogleAuthentication {

    private val jacksonFactory = JacksonFactory()
    private val httpTransport = NetHttpTransport.Builder().build()
    private val verifier = GoogleIdTokenVerifier.Builder(httpTransport, jacksonFactory)
        .setAudience(Collections.singletonList(CLIENT_ID))
        .build()

    fun verify(token: String): AuthenticatedUser? {

        val result = verifier.verify(token) ?: return null
        val payload = result.payload

        return AuthenticatedUser(
            payload.subject,
            payload["name"] as String,
            payload.email,
            payload["picture"] as String,
            payload["locale"] as String
        )
    }
}