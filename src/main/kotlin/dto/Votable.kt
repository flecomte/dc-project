package fr.dcproject.dto

interface Votable {
    val votes: VoteAggregation

    class Imp(parent: fr.dcproject.entity.Votable) : Votable {
        override val votes: VoteAggregation = VoteAggregation(parent)
    }
}
