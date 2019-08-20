import fr.dcproject.entity.Article
import fr.dcproject.entity.Citizen
import fr.dcproject.entity.Constitution
import fr.dcproject.entity.Follow
import fr.postgresjson.repository.RepositoryI.Direction
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location

@KtorExperimentalLocationsAPI
object Paths {
    @Location("/articles") class ArticlesRequest(page: Int = 1, limit: Int = 50, val sort: String? = null, val direction: Direction? = null, val search: String? = null) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }
    @Location("/articles/{article}") class ArticleRequest(val article: Article)
    @Location("/articles/{article}/follow") class ArticleFollowRequest(val article: Article)
    @Location("/articles") class PostArticleRequest


    @Location("/constitutions") class ConstitutionsRequest(page: Int = 1, limit: Int = 50, val sort: String? = null, val direction: Direction? = null, val search: String? = null) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }
    @Location("/constitutions/{constitution}") class ConstitutionRequest(val constitution: Constitution)
    @Location("/constitutions/{constitution}/follow") class ConstitutionFollowRequest(val constitution: Constitution)
    @Location("/constitutions") class PostConstitutionRequest


    @Location("/citizens") class CitizensRequest(page: Int = 1, limit: Int = 50, val sort: String? = null, val direction: Direction? = null, val search: String? = null) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }
    @Location("/citizens/{citizen}") class CitizenRequest(val citizen: Citizen)
}