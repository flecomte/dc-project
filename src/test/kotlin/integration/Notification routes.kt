package integration

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.auth.database.UserCreator
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.dcproject.component.notification.ArticleUpdateNotification
import fr.dcproject.component.notification.Notification
import fr.dcproject.component.notification.Publisher
import integration.steps.given.`Given I have article`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have follow on article`
import integration.steps.given.`authenticated in url as`
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.test.get
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("notification"))
class `Notification routes` : BaseTest() {
    @Test
    fun `I can send notification`() {
        withIntegrationApplication {
            `Given I have citizen`("John", "Doe", id = "1a34191a-9cde-45ba-8ac1-230138a102d3")
            `Given I have article`(id = "a06cbfb7-3094-4d64-aaa1-7486c0c292f4", createdBy = Name(firstName = "John", lastName = "Doe"))
            `Given I have follow on article`("John", "Doe", article = "a06cbfb7-3094-4d64-aaa1-7486c0c292f4")
            val notification = ArticleUpdateNotification(
                ArticleForView(
                    id = "a06cbfb7-3094-4d64-aaa1-7486c0c292f4".toUUID(),
                    title = "MyTitle",
                    content = "myContent",
                    description = "myDescription",
                    createdBy = CitizenCreator(
                        id = "1a34191a-9cde-45ba-8ac1-230138a102d3".toUUID(),
                        name = Name(firstName = "John", lastName = "Doe"),
                        email = "john-doe@plop.com",
                        user = UserCreator(username = "john-doe"),
                    )
                )
            )
            val publisher = get<Publisher>()
            launch {
                publisher
                    .publish(notification)
                    .await()
            }

            Thread.sleep(1000)

            handleWebSocketConversation(
                "/notifications",
                {
                    `authenticated in url as`("John", "Doe")
                }
            ) { incoming, outgoing ->
                incoming.receive().let {
                    when (it) {
                        is Frame.Text -> Notification.fromString<ArticleUpdateNotification>(it.readText()).let { notif ->
                            assertEquals(
                                "a06cbfb7-3094-4d64-aaa1-7486c0c292f4",
                                notif.target.id.toString()
                            )
                            outgoing.send(it)
                        }
                        else -> error(it.toString())
                    }
                }
            }
        }
    }
}
