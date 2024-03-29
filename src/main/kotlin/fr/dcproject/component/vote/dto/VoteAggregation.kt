package fr.dcproject.component.vote.dto

import fr.dcproject.component.vote.entity.Votable

class VoteAggregation(parent: Votable) {
    val up: Int = parent.votes.up
    val neutral: Int = parent.votes.neutral
    val down: Int = parent.votes.down
    val total: Int = parent.votes.total
    val score: Int = parent.votes.score
}
