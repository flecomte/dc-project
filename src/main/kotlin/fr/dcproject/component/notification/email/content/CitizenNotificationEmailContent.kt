package fr.dcproject.component.notification.email.content

import fr.dcproject.component.citizen.database.Citizen

class CitizenNotificationEmailContent(
    private val citizen: Citizen,
    private val target: Citizen,
    private val domain: String,
) : NotificationEmailContent {
    override val subject: String
        get() = """New activity for the citizen "${target.name}""""

    override val contentHtml
        get() = run {
            """
                Hello ${citizen.name.getFullName()},
                The citizen "${target.name}" was new activity, check it here: <a href="http://$domain/citizens/${target.id}">here</a>
            """.trimIndent()
        }

    override val content
        get() = run {
            """
                Hello ${citizen.name.getFullName()},
                The citizen "${target.name}" was new activity, check it here: http://$domain/citizens/${target.id}
            """.trimIndent()
        }
}
