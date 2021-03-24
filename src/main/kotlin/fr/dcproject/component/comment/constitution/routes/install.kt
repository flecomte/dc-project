package fr.dcproject.component.comment.constitution.routes

import fr.dcproject.component.comment.constitution.routes.CreateConstitutionComment.createConstitutionComment
import fr.dcproject.component.comment.constitution.routes.GetCitizenCommentConstitution.getCitizenCommentConstitution
import fr.dcproject.component.comment.constitution.routes.GetConstitutionComment.getConstitutionComment
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installCommentConstitutionRoutes() {
    authenticate(optional = true) {
        createConstitutionComment(get(), get())
        getCitizenCommentConstitution(get(), get())
        getConstitutionComment(get(), get())
    }
}
