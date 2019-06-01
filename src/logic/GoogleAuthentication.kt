package logic

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import java.util.*


const val CLIENT_ID = "361668683148-casfe6p1qcgpf8s5aa2cg2tr6qvstdg0.apps.googleusercontent.com"

class GoogleAuthentication {

    private val jacksonFactory = JacksonFactory()
    private val httpTransport = NetHttpTransport.Builder().build()
    private val verifier = GoogleIdTokenVerifier.Builder(httpTransport, jacksonFactory)
        .setAudience(Collections.singletonList(CLIENT_ID))
        .setIssuer("https://accounts.google.com")
        .build()

    fun verify(token: String): GoogleUser? {

        val result = verifier.verify(token) ?: return null
        val payload = result.payload

        return GoogleUser(
            payload["sub"] as String,
            payload["name"] as String,
            payload.email,
            payload["picture"] as String,
            payload["locale"] as String
        )
    }
}