package fr.dcproject.component.constitution.routes

import fr.dcproject.component.constitution.routes.CreateConstitution.createConstitution
import fr.dcproject.component.constitution.routes.FindConstitutions.findConstitutions
import fr.dcproject.component.constitution.routes.GetConstitution.getConstitution
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installConstitutionRoutes() {
    authenticate(optional = true) {
        getConstitution(get())
        findConstitutions(get(), get())
        createConstitution(get(), get())
    }
}
