package fr.dcproject.component.comment.generic.routes

import fr.dcproject.component.comment.generic.routes.CreateComment.createCommentChildren
import fr.dcproject.component.comment.generic.routes.EditComment.editComment
import fr.dcproject.component.comment.generic.routes.GetCommentChildren.getChildrenComments
import fr.dcproject.component.comment.generic.routes.GetOneComment.getOneComment
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installCommentRoutes() {
    authenticate(optional = true) {
        editComment(get(), get())
        getOneComment(get(), get())
        createCommentChildren(get(), get())
        getChildrenComments(get(), get())
    }
}
