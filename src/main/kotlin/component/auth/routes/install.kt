package fr.dcproject.component.auth.routes

import fr.dcproject.component.auth.routes.Login.authLogin
import fr.dcproject.component.auth.routes.Register.authRegister
import fr.dcproject.component.auth.routes.Sso.authPasswordless
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installAuthRoutes() {
    authenticate(optional = true) {
        authLogin(get())
        authRegister(get())
        authPasswordless(get())
    }
}
