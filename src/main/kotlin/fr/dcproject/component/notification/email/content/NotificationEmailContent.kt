package fr.dcproject.component.notification.email.content

interface NotificationEmailContent {
    val subject: String
    val content: String
    val contentHtml: String
}
