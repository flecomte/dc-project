import fr.dcproject.entity.Article
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location

@KtorExperimentalLocationsAPI
object Paths {
    @Location("/articles") class ArticlesRequest
    @Location("/articles/{article}") class ArticleRequest(val article: Article)
    @Location("/articles") class PostArticleRequest
}