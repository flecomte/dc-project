package integration.steps.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.dcproject.component.citizen.database.CitizenRepository
import fr.dcproject.component.opinion.database.OpinionChoice
import fr.dcproject.component.opinion.database.OpinionChoiceRepository
import fr.dcproject.component.opinion.database.OpinionForUpdate
import fr.dcproject.component.opinion.database.OpinionRepositoryArticle
import io.ktor.server.testing.TestApplicationEngine
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have an opinion choice`(
    name: String,
    id: String? = null,
) {
    createOpinionChoice(id?.toUUID(), name)
}

fun createOpinionChoice(
    id: UUID? = null,
    name: String,
): OpinionChoice {
    val opinionChoiceRepository: OpinionChoiceRepository by lazy { GlobalContext.get().get() }
    val opinionChoice = OpinionChoice(
        id = id,
        name = name,
        target = listOf()
    )
    return opinionChoiceRepository.upsertOpinionChoice(opinionChoice)
}

fun `Given I have opinion on article`(
    name: String,
    article: String,
    citizenName: Name,
    id: String? = null
) {
    val citizenRepository: CitizenRepository by lazy { GlobalContext.get().get() }
    val opinionRepositoryArticle: OpinionRepositoryArticle by lazy { GlobalContext.get().get() }
    val opinionChoiceRepository: OpinionChoiceRepository by lazy { GlobalContext.get().get() }
    val opinion = OpinionForUpdate(
        id = id?.toUUID() ?: UUID.randomUUID(),
        choice = opinionChoiceRepository.findOpinionsChoiceByName(name)
            ?: error("Opinion Choice not exist"),
        target = ArticleRef(article.toUUID()),
        createdBy = citizenRepository.findByName(citizenName)
            ?: error("Citizen not exist")
    )
    opinionRepositoryArticle.addOpinion(opinion)
}
