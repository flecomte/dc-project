package fr.dcproject.component.vote.routes

import fr.dcproject.component.vote.routes.GetCitizenVotes.getCitizenVote
import fr.dcproject.component.vote.routes.GetCitizenVotesOnArticle.getCitizenVotesOnArticle
import fr.dcproject.component.vote.routes.PutVoteOnArticle.putVoteOnArticle
import fr.dcproject.component.vote.routes.PutVoteOnComment.putVoteOnComment
import fr.dcproject.component.vote.routes.VoteConstitution.voteConstitution
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installVoteRoutes() {
    authenticate(optional = true) {
        getCitizenVote(get(), get())
        getCitizenVotesOnArticle(get(), get())
        putVoteOnArticle(get(), get(), get())
        putVoteOnComment(get(), get(), get())
        voteConstitution(get(), get())
    }
}
