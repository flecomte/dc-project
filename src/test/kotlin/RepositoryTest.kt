import fr.dcproject.Env
import fr.dcproject.module
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.get
import fr.dcproject.repository.Article as RepositoryArticle
import fr.dcproject.repository.Constitution as RepositoryConstitution

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@TestInstance(PER_CLASS)
class RepositoryTest: KoinTest, AutoCloseKoinTest() {
    @Test
    fun `test get repository`() {
        withTestApplication({ module(Env.TEST) }) {
            val repoArticle = get<RepositoryArticle>()
            (repoArticle is RepositoryArticle) shouldBe true

            val repoConstitution = get<RepositoryConstitution>()
            (repoConstitution is RepositoryConstitution) shouldBe true
        }
    }
}
