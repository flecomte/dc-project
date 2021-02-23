package integration.steps.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.citizen.CitizenI.Name
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.vote.VoteArticleRepository
import fr.dcproject.component.vote.entity.VoteAggregation
import fr.dcproject.component.vote.entity.VoteForUpdate
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
    val voteArticleRepository: VoteArticleRepository by lazy { GlobalContext.get().koin.get() }
    val articleRepository: ArticleRepository by lazy { GlobalContext.get().koin.get() }
    val citizenRepository: CitizenRepository by lazy { GlobalContext.get().koin.get() }
    val vote = VoteForUpdate(
        id = id ?: UUID.randomUUID(),
        note = note,
        target = articleRepository.findById(article.toUUID()) ?: error("Article not exist"),
        createdBy = citizenRepository.findByName(name) ?: error("Citizen not exist")
    )
    return voteArticleRepository.vote(vote)
}
