package fr.dcproject.component.opinion.routes

import fr.dcproject.component.opinion.routes.GetCitizenOpinions.getCitizenOpinions
import fr.dcproject.component.opinion.routes.GetMyOpinionsArticle.getMyOpinionsArticle
import fr.dcproject.component.opinion.routes.GetOpinionChoice.getOpinionChoice
import fr.dcproject.component.opinion.routes.GetOpinionChoices.getOpinionChoices
import fr.dcproject.component.opinion.routes.OpinionArticle.setOpinionOnArticle
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installOpinionRoutes() {
    authenticate(optional = true) {
        getCitizenOpinions(get(), get())
        getMyOpinionsArticle(get(), get())
        setOpinionOnArticle(get(), get())
        getOpinionChoice(get(), get())
        getOpinionChoices(get(), get())
    }
}
