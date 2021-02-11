package fr.dcproject.component.vote.dto

interface Votable {
    val votes: VoteAggregation

    class Imp(parent: fr.dcproject.component.vote.entity.Votable) : Votable {
        override val votes: VoteAggregation = VoteAggregation(parent)
    }
}
