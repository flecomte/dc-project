package fr.dcproject.component.doc.routes

import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
fun Routing.installDocRoutes() {
    authenticate(optional = true) {
        definition()
    }
}
