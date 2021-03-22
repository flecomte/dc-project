package fr.dcproject.component.vote.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.component.vote.database.VoteForView
import fr.dcproject.component.vote.entity.VoteAggregation
import org.joda.time.DateTime
import java.util.UUID

fun VoteForView<*>.toOutput(): Any = this.let { v ->
    object {
        val id: UUID = v.id
        val note: Int = v.note
        val createdAt: DateTime = v.createdAt
        val createdBy: Any = v.createdBy.toOutput()
        val target: Any = object {
            val id: UUID = v.target.id
            val reference: String = v.target.reference
        }
    }
}

fun VoteAggregation.toOutput(): Any = this.let { v ->
    object {
        val up: Int = v.up
        val neutral: Int = v.neutral
        val down: Int = v.down
        val total: Int = v.total
        val score: Int = v.score
        val updatedAt: DateTime = v.updatedAt
    }
}
