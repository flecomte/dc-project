package integration.steps.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.dcproject.component.citizen.database.CitizenRepository
import fr.dcproject.component.vote.database.VoteArticleRepository
import fr.dcproject.component.vote.database.VoteForUpdate
import fr.dcproject.component.vote.entity.VoteAggregation
import io.ktor.server.testing.TestApplicationEngine
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have vote +1 on article`(
    article: String,
    name: Name,
    id: String? = null,
) {
    createVote(article, name, 1, id?.toUUID())
}

fun TestApplicationEngine.`Given I have vote -1 on article`(
    article: String,
    name: Name,
    id: String? = null,
) {
    createVote(article, name, -1, id?.toUUID())
}

fun createVote(
    article: String,
    name: Name,
    note: Int,
    id: UUID? = null,
): VoteAggregation {
    val voteArticleRepository: VoteArticleRepository by lazy { GlobalContext.get().get() }
    val articleRepository: ArticleRepository by lazy { GlobalContext.get().get() }
    val citizenRepository: CitizenRepository by lazy { GlobalContext.get().get() }
    val vote = VoteForUpdate(
        id = id ?: UUID.randomUUID(),
        note = note,
        target = articleRepository.findById(article.toUUID()) ?: error("Article not exist"),
        createdBy = citizenRepository.findByName(name) ?: error("Citizen not exist")
    )
    return voteArticleRepository.vote(vote)
}
