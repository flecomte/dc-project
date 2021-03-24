package fr.dcproject.component.vote.entity

interface Votable {
    val votes: VoteAggregation
}

class VotableImp : Votable {
    override val votes: VoteAggregation = VoteAggregation()
}
