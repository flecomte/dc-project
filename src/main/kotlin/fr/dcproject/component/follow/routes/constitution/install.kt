package fr.dcproject.component.follow.routes.constitution

import fr.dcproject.component.follow.routes.constitution.FollowConstitution.followConstitution
import fr.dcproject.component.follow.routes.constitution.GetFollowConstitution.getFollowConstitution
import fr.dcproject.component.follow.routes.constitution.GetMyFollowsConstitution.getMyFollowsConstitution
import fr.dcproject.component.follow.routes.constitution.UnfollowConstitution.unfollowConstitution
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installFollowConstitutionRoutes() {
    authenticate(optional = true) {
        followConstitution(get(), get())
        unfollowConstitution(get(), get())
        getFollowConstitution(get(), get())
        getMyFollowsConstitution(get(), get())
    }
}
