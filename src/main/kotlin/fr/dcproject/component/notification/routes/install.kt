package fr.dcproject.component.notification.routes

import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.ktor.ext.get

@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
fun Routing.installNotificationsRoutes() {
    authenticate("url") {
        notificationArticle(get())
    }
}
