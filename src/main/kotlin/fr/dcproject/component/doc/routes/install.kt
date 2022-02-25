package fr.dcproject.component.doc.routes

import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
fun Routing.installDocRoutes() {
    authenticate(optional = true) {
        definition()
    }
}
