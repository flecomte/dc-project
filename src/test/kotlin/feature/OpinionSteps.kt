package feature

import fr.dcproject.entity.OpinionArticle
import fr.dcproject.entity.OpinionChoice
import fr.dcproject.utils.toUUID
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*
import fr.dcproject.repository.Article as ArticleRepository
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.OpinionArticle as OpinionRepository
import fr.dcproject.repository.OpinionChoice as OpinionChoiceRepository

class OpinionSteps : En, KoinTest {
    init {
        Given("I have an opinion choice {string}") { name: String ->
            val opinionChoice = OpinionChoice(
                name = name,
                target = listOf()
            )
            get<OpinionRepository>().upsertOpinionChoice(opinionChoice)
        }

        Given("I have an opinion choice {string} with ID {string}") { name: String, id: String ->
            val opinionChoice = OpinionChoice(
                id = id.toUUID(),
                name = name,
                target = listOf()
            )
            get<OpinionRepository>().upsertOpinionChoice(opinionChoice)
        }

        Given("I have an opinion {string} on article {string} created by {word} {word}") { opinionChoiceName: String, articleId: String, firstName: String, lastName: String ->
            createOpinion(opinionChoiceName, articleId, firstName, lastName)
        }

        Given("I have an opinion {string} on article {string} created by {word} {word} with ID {string}") { opinionChoiceName: String, articleId: String, firstName: String, lastName: String, id: String ->
            createOpinion(opinionChoiceName, articleId, firstName, lastName, id)
        }

        Given("I have an opinion") { extraInfo: DataTable ->
            createOpinionOnArticle(extraInfo)
        }
    }

    private fun createOpinion(opinionChoiceName: String, articleId: String, firstName: String, lastName: String, id: String? = null) {
        val opinion = OpinionArticle(
            id = id?.toUUID() ?: UUID.randomUUID(),
            choice = get<OpinionChoiceRepository>().findOpinionsChoiceByName(opinionChoiceName)
                ?: error("Opinion Choice not exist"),
            target = get<ArticleRepository>().findById(articleId.toUUID()) ?: error("Article not exist"),
            createdBy = get<CitizenRepository>().findByUsername("$firstName-$lastName".toLowerCase().replace(' ', '-')) ?: error("Citizen not exist")
        )
        get<OpinionRepository>().opinion(opinion)
    }

    private fun createOpinionOnArticle(extraInfo: DataTable? = null) {
        val params = extraInfo?.asMap<String, String>(String::class.java, String::class.java)
        val username = params?.get("createdBy")?.toLowerCase()?.replace(' ', '-')
            ?: error("You must provide the 'createdBy' parameter")
        val opinion = OpinionArticle(
            choice = params["opinion"]?.let {
                get<OpinionChoiceRepository>().findOpinionsChoiceByName(it) ?: error("Opinion Choice not exist")
            } ?: error("You must provide the 'opinion' parameter"),
            target = params["article"]?.let {
                get<ArticleRepository>().findById(it.toUUID()) ?: error("Article not exist")
            } ?: error("You must provide the 'article' parameter"),
            createdBy = get<CitizenRepository>().findByUsername(username) ?: error("Citizen not exist")
        )
        get<OpinionRepository>().opinion(opinion)
    }
}