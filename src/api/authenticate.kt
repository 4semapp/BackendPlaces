package api

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.request.header
import io.ktor.util.pipeline.PipelineContext
import logic.AuthenticatedUser
import logic.GoogleAuthentication

val authenticator = GoogleAuthentication()
const val prefix = "Bearer "

suspend fun PipelineContext<Unit, ApplicationCall>.authenticate(onSuccess: suspend (call: ApplicationCall, user: AuthenticatedUser) -> Unit) {

    var token = call.request.header("Authorization")
    if (token == null) {
        error("You must provide an Authorization token.", Unauthorized)
        return
    }

    val user = authenticator.verify(token.removePrefix(prefix))
    if (user == null) {
        error("The provided token is invalid.")
        return
    }

    return onSuccess(call, user)
}
