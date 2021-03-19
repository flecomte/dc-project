package fr.dcproject.common.utils

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.ktor.application.ApplicationCall
import io.ktor.application.log
import io.ktor.features.BadRequestException
import io.ktor.request.receive
import kotlin.reflect.typeOf

/**
 * Receives content for this request.
 * @param type instance of `KClass` specifying type to be received.
 * @return instance of [T] received from this call, or `null` if content cannot be transformed to the requested type..
 */
@OptIn(ExperimentalStdlibApi::class)
public suspend inline fun <reified T : Any> ApplicationCall.receiveOrBadRequest(message: String = "Bad Request, wrong body request"): T {
    return try {
        receive<T>(typeOf<T>())
    } catch (cause: MissingKotlinParameterException) {
        application.log.debug("Conversion failed, throw bad exception", cause)
        throw BadRequestException(message, cause)
    } catch (cause: UnrecognizedPropertyException) {
        application.log.debug("Conversion failed, throw bad exception", cause)
        throw BadRequestException(message, cause)
    }
}
