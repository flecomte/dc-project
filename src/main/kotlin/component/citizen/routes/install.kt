package fr.dcproject.component.citizen.routes

import fr.dcproject.component.citizen.routes.ChangeMyPassword.changeMyPassword
import fr.dcproject.component.citizen.routes.FindCitizens.findCitizen
import fr.dcproject.component.citizen.routes.GetCurrentCitizen.getCurrentCitizen
import fr.dcproject.component.citizen.routes.GetOneCitizen.getOneCitizen
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installCitizenRoutes() {
    authenticate(optional = true) {
        findCitizen(get(), get())
        getOneCitizen(get(), get())
        getCurrentCitizen(get())
        changeMyPassword(get(), get())
    }
}
