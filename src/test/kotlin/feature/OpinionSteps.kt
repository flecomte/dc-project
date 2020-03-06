package feature

import fr.dcproject.entity.OpinionArticle
import fr.dcproject.utils.toUUID
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import org.koin.test.KoinTest
import org.koin.test.get
import fr.dcproject.repository.Article as ArticleRepository
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.OpinionArticle as OpinionRepository
import fr.dcproject.repository.OpinionChoice as OpinionChoiceRepository

class OpinionSteps : En, KoinTest {
    init {
        Given("I have the opinion {string} on article {string} created by {string}:") { opinionChoice: String, article: String, citizen: String, extraInfo: DataTable ->
            extraInfo.asMap<String, String>(String::class.java, String::class.java).let {
                val opinion = OpinionArticle(
                    choice = get<OpinionChoiceRepository>().findOpinionsChoiceByName(opinionChoice)
                        ?: error("Opinion Choice not exist"),
                    target = get<ArticleRepository>().findById(article.toUUID()) ?: error("Article not exist"),
                    createdBy = get<CitizenRepository>().findById(citizen.toUUID()) ?: error("Citizen not exist")
                )
                get<OpinionRepository>().opinion(opinion)
            }
        }

        Given("I have an opinion") { extraInfo: DataTable ->
            extraInfo.asMap<String, String>(String::class.java, String::class.java)?.let { params ->
                val username = params["createdBy"]?.toLowerCase()?.replace(' ', '-')
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
    }
}