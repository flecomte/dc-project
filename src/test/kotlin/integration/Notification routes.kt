package integration

import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.dcproject.component.notification.ArticleUpdateNotificationMessage
import fr.dcproject.component.notification.NotificationMessage
import integration.steps.given.`And follow citizen`
import integration.steps.given.`Given I have article update notification`
import integration.steps.given.`Given I have article`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have follow on article`
import integration.steps.given.`authenticated in url as`
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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
            `Given I have article update notification`("a06cbfb7-3094-4d64-aaa1-7486c0c292f4")

            Thread.sleep(1000)

            handleWebSocketConversation(
                "/notifications",
                {
                    `authenticated in url as`("John", "Doe")
                }
            ) { incoming, outgoing ->
                incoming.receive().let {
                    when (it) {
                        is Frame.Text -> NotificationMessage.fromString<ArticleUpdateNotificationMessage>(it.readText()).let { notif ->
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

    @Test
    fun `I can receive article update notification when follow the creator`() {
        withIntegrationApplication {
            `Given I have citizen`("Thomas", "Pesquet", id = "1a34191a-9cde-45ba-8ac1-230138a102d3")
            `Given I have article`(id = "a06cbfb7-3094-4d64-aaa1-7486c0c292f4", createdBy = Name(firstName = "Thomas", lastName = "Pesquet"))
            `Given I have citizen`("Alan", "Bean") {
                `And follow citizen`(Name("Thomas", "Pesquet"))
            }
            `Given I have article update notification`("a06cbfb7-3094-4d64-aaa1-7486c0c292f4")

            Thread.sleep(1000)

            handleWebSocketConversation(
                "/notifications",
                {
                    `authenticated in url as`("Alan", "Bean")
                }
            ) { incoming, outgoing ->
                incoming.receive().let {
                    when (it) {
                        is Frame.Text -> NotificationMessage.fromString<ArticleUpdateNotificationMessage>(it.readText()).let { notif ->
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
