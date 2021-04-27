package integration.steps.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.auth.database.UserCreator
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.notification.ArticleUpdateNotificationMessage
import fr.dcproject.component.notification.NotificationPublisherAsync
import io.ktor.server.testing.TestApplicationEngine
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools
import java.util.UUID

fun TestApplicationEngine.`Given I have article update notification`(
    id: String = UUID.randomUUID().toString()
) {
    val notification = ArticleUpdateNotificationMessage(
        ArticleForView(
            id = id.toUUID(),
            title = "MyTitle",
            content = "myContent",
            description = "myDescription",
            createdBy = CitizenCreator(
                id = "1a34191a-9cde-45ba-8ac1-230138a102d3".toUUID(),
                name = CitizenI.Name(firstName = "John", lastName = "Doe"),
                email = "john-doe@plop.com",
                user = UserCreator(username = "john-doe"),
            )
        )
    )
    launch {
        KoinPlatformTools
            .defaultContext()
            .get()
            .get<NotificationPublisherAsync>()
            .publishAsync(notification)
            .await()
    }
}
