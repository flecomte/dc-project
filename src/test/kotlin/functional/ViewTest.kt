package functional

import fr.dcproject.application.Env.TEST
import fr.dcproject.application.module
import fr.dcproject.component.article.ArticleViewManager
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.citizen.database.CitizenRef
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.koin.ktor.ext.get
import java.util.UUID

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@TestInstance(PER_CLASS)
@Tags(Tag("functional"))
class ViewTest {
    @Test
    fun `test View Article`() {
        val article = ArticleForView(
            id = UUID.randomUUID(),
            versionId = UUID.randomUUID(),
            createdBy = CitizenRef(),
            content = "",
            description = "",
            title = ""
        )
        val citizenRef = CitizenRef()

        withTestApplication({ module(TEST) }) {
            val viewManager: ArticleViewManager<ArticleForView> = application.get()

            /* Get view before */
            val startView = viewManager.getViewsCount(article)

            /* Add View */
            viewManager.addView(
                "1.2.3.4",
                article,
                citizenRef
            )

            /* Add View */
            viewManager.addView(
                "10.10.10.10",
                article,
                citizenRef
            )

            /* Add View */
            viewManager.addView(
                "8.8.8.8",
                article
            )

            /* Add View */
            viewManager.addView(
                "1.1.1.1",
                article
            )

            /* Sleep because ES is not sync ! */
            Thread.sleep(1000)

            /* Get view */
            val afterView = viewManager.getViewsCount(article)

            /* Check if view has increment */
            afterView.total `should be equal to` startView.total + 4
            afterView.unique `should be equal to` startView.unique + 3
        }
    }
}
