package fr.dcproject.application

import fr.dcproject.application.http.BadRequestException
import fr.dcproject.application.http.HttpErrorBadRequest
import fr.dcproject.application.http.HttpErrorBadRequest.InvalidParam
import io.ktor.features.DataConversion
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import java.util.UUID

private typealias ConverterDeclaration = DataConversion.Configuration.() -> Unit

private inline fun <reified T> DataConversion.Configuration.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T = GlobalContext.get().koin.rootScope.get(qualifier, parameters)

@KtorExperimentalAPI
val converters: ConverterDeclaration = {
    convert<UUID> {
        decode { values, _ ->
            try {
                values.singleOrNull()?.let { UUID.fromString(it) }
            } catch (e: Throwable) {
                throw BadRequestException(
                    HttpErrorBadRequest(
                        HttpStatusCode.BadRequest,
                        invalidParams = listOf(
                            InvalidParam(
                                "ID",
                                "must be UUID"
                            )
                        )
                    )
                )
            }
        }

        encode { value ->
            when (value) {
                null -> listOf()
                is UUID -> listOf(value.toString())
                else -> throw InternalError("Cannot convert $value as UUID")
            }
        }
    }
}
