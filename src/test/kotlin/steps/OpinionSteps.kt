package steps

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.opinion.entity.OpinionChoice
import fr.dcproject.component.opinion.entity.OpinionForUpdate
import fr.dcproject.utils.toUUID
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.UUID
import fr.dcproject.repository.OpinionChoiceRepository as OpinionChoiceRepository
import fr.dcproject.repository.OpinionRepositoryArticle as OpinionRepository

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

    private fun createOpinion(
        opinionChoiceName: String,
        articleId: String,
        firstName: String,
        lastName: String,
        id: String? = null
    ) {
        val opinion = OpinionForUpdate(
            id = id?.toUUID() ?: UUID.randomUUID(),
            choice = get<OpinionChoiceRepository>().findOpinionsChoiceByName(opinionChoiceName)
                ?: error("Opinion Choice not exist"),
            target = ArticleRef(articleId.toUUID()),
            createdBy = get<CitizenRepository>().findByUsername("$firstName-$lastName".toLowerCase().replace(' ', '-'))
                ?: error("Citizen not exist")
        )
        get<OpinionRepository>().addOpinion(opinion)
    }

    private fun createOpinionOnArticle(extraInfo: DataTable? = null) {
        val params = extraInfo?.asMap<String, String>(String::class.java, String::class.java)
        val username = params?.get("createdBy")?.toLowerCase()?.replace(' ', '-')
            ?: error("You must provide the 'createdBy' parameter")
        val opinion = OpinionForUpdate(
            choice = params["opinion"]?.let {
                get<OpinionChoiceRepository>().findOpinionsChoiceByName(it) ?: error("Opinion Choice not exist")
            } ?: error("You must provide the 'opinion' parameter"),
            target = params["article"]?.let {
                get<ArticleRepository>().findById(it.toUUID()) ?: error("Article not exist")
            } ?: error("You must provide the 'article' parameter"),
            createdBy = get<CitizenRepository>().findByUsername(username) ?: error("Citizen not exist")
        )
        get<OpinionRepository>().updateOpinions(opinion)
    }
}
