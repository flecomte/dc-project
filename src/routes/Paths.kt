import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location

@KtorExperimentalLocationsAPI
object Paths {
    @Location("/articles") class ArticlesRequest
    @Location("/articles") class PostArticleRequest
}