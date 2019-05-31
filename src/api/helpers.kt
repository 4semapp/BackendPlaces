package api

import com.google.gson.GsonBuilder
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

val gson = GsonBuilder().setPrettyPrinting().create()

suspend fun PipelineContext<Unit, ApplicationCall>.error(message: String, httpCode: HttpStatusCode) {
    val error = ErrorMessage(message, httpCode.value)
    call.response.status(httpCode)
    call.respond(gson.toJson(error))
}

private data class ErrorMessage(val message: String, val httpCode: Int)

fun PipelineContext<Unit, ApplicationCall>.getParameter(parameterName: String): String? {
    val parameterValue = call.parameters[parameterName]
    if (parameterValue == null) {
        error("Missing parameter $parameterName.")
        return null
    }

    return parameterValue
}