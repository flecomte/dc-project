package fr.dcproject.application

import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.constitution.Constitution
import fr.dcproject.component.constitution.ConstitutionRef
import fr.dcproject.component.constitution.ConstitutionRepository
import fr.dcproject.component.opinion.OpinionChoiceRepository
import fr.dcproject.component.opinion.entity.OpinionChoice
import fr.dcproject.component.workgroup.Workgroup
import fr.dcproject.component.workgroup.WorkgroupRef
import fr.dcproject.component.workgroup.WorkgroupRepository
import io.ktor.features.DataConversion
import io.ktor.features.NotFoundException
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
            values.singleOrNull()?.let { UUID.fromString(it) }
        }

        encode { value ->
            when (value) {
                null -> listOf()
                is UUID -> listOf(value.toString())
                else -> throw InternalError("Cannot convert $value as UUID")
            }
        }
    }

    // TODO remove converters of entities

    convert<WorkgroupRef> {
        decode { values, _ ->
            values.singleOrNull()?.let {
                WorkgroupRef(UUID.fromString(it))
            } ?: throw NotFoundException("""UUID "$values" is not valid for Workgroup""")
        }
    }

    convert<Workgroup<CitizenBasic>> {
        decode { values, _ ->
            val id = values.singleOrNull()?.let { UUID.fromString(it) }
                ?: throw InternalError("Cannot convert $values to UUID")
            get<WorkgroupRepository>().findById(id)
                ?: throw NotFoundException("Workgroup $values not found")
        }
    }
}
