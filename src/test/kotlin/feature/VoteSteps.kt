package feature

import fr.dcproject.entity.Vote
import fr.dcproject.utils.toUUID
import io.cucumber.java8.En
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*
import fr.dcproject.repository.Article as ArticleRepository
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.VoteArticle as VoteRepository

class VoteSteps : En, KoinTest {
    init {
        Given("I have an vote {int} on article {string} created by {word} {word}") { note: Int, articleId: String, firstName: String, lastName: String ->
            createVote(note, articleId, firstName, lastName)
        }

        Given("I have an vote {int} on article {string} created by {word} {word} with ID {string}") { note: Int, articleId: String, firstName: String, lastName: String, id: String ->
            createVote(note, articleId, firstName, lastName, id)
        }
    }

    private fun createVote(note: Int, articleId: String, firstName: String, lastName: String, id: String? = null) {
        val vote = Vote(
            id = id?.toUUID() ?: UUID.randomUUID(),
            note = note,
            target = get<ArticleRepository>().findById(articleId.toUUID()) ?: error("Article not exist"),
            createdBy = get<CitizenRepository>().findByUsername("$firstName-$lastName".toLowerCase().replace(' ', '-')) ?: error("Citizen not exist")
        )
        get<VoteRepository>().vote(vote)
    }
}