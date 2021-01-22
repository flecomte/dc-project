package fr.dcproject.application

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.comment.generic.CommentRef
import fr.dcproject.component.opinion.entity.OpinionChoice
import fr.dcproject.component.workgroup.Workgroup
import fr.dcproject.component.workgroup.WorkgroupRef
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.entity.Constitution
import fr.dcproject.entity.ConstitutionRef
import fr.dcproject.repository.OpinionChoiceRepository
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

    convert<ArticleForView> {
        decode { values, _ ->
            values.singleOrNull()?.let {
                get<ArticleRepository>().findById(UUID.fromString(it))
                    ?: throw NotFoundException("Article $values not found")
            } ?: throw NotFoundException("Article $values not found")
        }
    }
    convert<ArticleRef> {
        decode { values, _ ->
            values.singleOrNull()?.let {
                ArticleRef(UUID.fromString(it))
            } ?: throw NotFoundException("""UUID "$values" is not valid for Article""")
        }
    }

    convert<CommentRef> {
        decode { values, _ ->
            values.singleOrNull()?.let {
                CommentRef(UUID.fromString(it))
            } ?: throw NotFoundException("""UUID "$values" is not valid for Comment""")
        }
    }
    convert<ConstitutionRef> {
        decode { values, _ ->
            values.singleOrNull()?.let {
                ConstitutionRef(UUID.fromString(it))
            } ?: throw NotFoundException("""UUID "$values" is not valid for Constitution""")
        }
    }

    convert<Constitution> {
        decode { values, _ ->
            val id = values.singleOrNull()?.let { UUID.fromString(it) }
                ?: throw InternalError("Cannot convert $values to UUID")
            get<fr.dcproject.repository.Constitution>().findById(id) ?: throw NotFoundException("Constitution $values not found")
        }
    }

    convert<Citizen> {
        decode { values, _ ->
            val id = values.singleOrNull()?.let { UUID.fromString(it) }
                ?: throw InternalError("Cannot convert $values to UUID")
            get<CitizenRepository>().findById(id) ?: throw NotFoundException("Citizen $values not found")
        }
    }

    convert<CitizenRef> {
        decode { values, _ ->
            values.singleOrNull()?.let {
                CitizenRef(UUID.fromString(it))
            } ?: throw NotFoundException("""UUID "$values" is not valid for Citizen""")
        }
    }

    convert<OpinionChoice> {
        decode { values, _ ->
            val id = values.singleOrNull()?.let { UUID.fromString(it) }
                ?: throw InternalError("Cannot convert $values to UUID")
            get<OpinionChoiceRepository>().findOpinionChoiceById(id)
                ?: throw NotFoundException("OpinionChoice $values not found")
        }
    }

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
