package fr.dcproject.component.vote.entity

interface Votable {
    val votes: VoteAggregation
}

data class VotableImp(
    override val votes: VoteAggregation = VoteAggregation()
) : Votable
