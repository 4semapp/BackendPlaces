package api

import com.mkl.User
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.request.header
import io.ktor.util.pipeline.PipelineContext

const val prefix = "Bearer "

suspend fun PipelineContext<Unit, ApplicationCall>.authenticate(onSuccess: suspend (call: ApplicationCall, user: User) -> Unit) {

    var token = call.request.header("Authorization")
    if (token == null) {
        error("You must provide an Authorization token.", Unauthorized)
        return
    }

    val user = verify(token.removePrefix(prefix))
    if (user == null) {
        this.error("The provided token is invalid.", Unauthorized)
        return
    }

    return onSuccess(call, user)
}
