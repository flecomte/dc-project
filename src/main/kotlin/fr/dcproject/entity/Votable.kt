package fr.dcproject.entity

interface Votable {
    var votes: VoteAggregation
}

class VotableImp : Votable {
    override var votes: VoteAggregation = VoteAggregation()
}