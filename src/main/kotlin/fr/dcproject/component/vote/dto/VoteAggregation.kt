package fr.dcproject.component.vote.dto

import fr.dcproject.component.vote.entity.Votable

data class VoteAggregation(
    val up: Int,
    val neutral: Int,
    val down: Int,
    val total: Int,
    val score: Int
) {
    constructor(parent: Votable) : this(
        up = parent.votes.up,
        neutral = parent.votes.neutral,
        down = parent.votes.down,
        total = parent.votes.total,
        score = parent.votes.score
    )
}
