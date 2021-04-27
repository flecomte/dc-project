package fr.dcproject.component.notification.email.content

import fr.dcproject.component.article.database.ArticleWithTitleI
import fr.dcproject.component.citizen.database.Citizen

class ArticleNotificationEmailContent(
    private val citizen: Citizen,
    private val target: ArticleWithTitleI,
    private val domain: String,
) : NotificationEmailContent {
    override val subject: String
        get() = """New version for article "${target.title}""""

    override val contentHtml
        get() = run {
            """
                Hello ${citizen.name.getFullName()},<br/>
                The article "${target.title}" was updated, check it <a href="http://$domain/articles/${target.id}">here</a>
            """.trimIndent()
        }

    override val content
        get() = run {
            """
                Hello ${citizen.name.getFullName()},
                The article "${target.title}" was updated, check it here: http://$domain/articles/${target.id}
            """.trimIndent()
        }
}
