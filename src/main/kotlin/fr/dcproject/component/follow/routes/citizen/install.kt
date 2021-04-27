package fr.dcproject.component.follow.routes.citizen

import fr.dcproject.component.follow.routes.citizen.FollowCitizen.followCitizen
import fr.dcproject.component.follow.routes.citizen.GetFollowCitizen.getFollowCitizen
import fr.dcproject.component.follow.routes.citizen.GetMyFollowsCitizen.getMyFollowsCitizen
import fr.dcproject.component.follow.routes.citizen.UnfollowCitizen.unfollowCitizen
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installFollowCitizenRoutes() {
    authenticate(optional = true) {
        followCitizen(get(), get())
        unfollowCitizen(get(), get())
        getFollowCitizen(get(), get())
        getMyFollowsCitizen(get(), get())
    }
}
