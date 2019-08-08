import fr.dcproject.Module
import fr.dcproject.entity.Article
import fr.dcproject.entity.Constitution
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication
import org.koin.test.KoinTest
import org.koin.test.get
import fr.dcproject.repository.Article as RepositoryArticle
import fr.dcproject.repository.Constitution as RepositoryConstitution

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@TestInstance(PER_CLASS)
class RepositoryTest: KoinTest {
    @BeforeAll
    fun beforeAll() {
        startKoin { modules(Module) }
    }

    @Test
    fun `test get repository`() {
        koinApplication {
            val repoArticle = get<RepositoryArticle>()
            (repoArticle is RepositoryArticle) shouldBe true
            repoArticle.entityName `should equal` Article::class

            val repoConstitution = get<RepositoryConstitution>()
            (repoConstitution is RepositoryConstitution) shouldBe true
            repoConstitution.entityName `should equal` Constitution::class
        }
    }
}
