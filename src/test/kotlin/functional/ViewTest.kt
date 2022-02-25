package functional

import fr.dcproject.application.Env.TEST
import fr.dcproject.application.module
import fr.dcproject.common.utils.retry
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.article.database.ArticleViewRepository
import fr.dcproject.component.auth.database.UserCreator
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRef
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.koin.ktor.ext.get
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@KtorExperimentalLocationsAPI
@ExperimentalCoroutinesApi
@TestInstance(PER_CLASS)
@Tags(Tag("functional"), Tag("view"))
class ViewTest {
    @ExperimentalTime
    @Test
    fun `test View Article`() {
        val article = ArticleForView(
            versionId = UUID.randomUUID(),
            createdBy = CitizenCreator(
                id = UUID.randomUUID(),
                name = CitizenI.Name(firstName = "", lastName = ""),
                email = "",
                user = UserCreator(username = ""),
            ),
            content = "",
            description = "",
            title = ""
        )
        val citizenRef = CitizenRef()

        withTestApplication({ module(TEST) }) {
            val viewRepository: ArticleViewRepository<ArticleForView> = application.get()

            /* Get view before */
            val startView = viewRepository.getViewsCount(article)

            /* Add View */
            viewRepository.addView(
                "1.2.3.4",
                article,
                citizenRef
            )

            /* Add View */
            viewRepository.addView(
                "10.10.10.10",
                article,
                citizenRef
            )

            /* Add View */
            viewRepository.addView(
                "8.8.8.8",
                article
            )

            /* Add View */
            viewRepository.addView(
                "1.1.1.1",
                article
            )

            /* Retry because ES is not sync ! */
            retry(10, 0.3.seconds) {
                /* Get view */
                val afterView = viewRepository.getViewsCount(article)

                /* Check if view has increment */
                afterView.total `should be equal to` startView.total + 4
                afterView.unique `should be equal to` startView.unique + 3
            }
        }
    }
}
