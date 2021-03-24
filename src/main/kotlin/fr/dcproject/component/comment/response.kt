package fr.dcproject.component.comment

import fr.dcproject.common.response.toOutput
import fr.dcproject.component.citizen.database.CitizenCreatorI
import fr.dcproject.component.comment.generic.database.CommentForUpdate
import fr.dcproject.component.comment.generic.database.CommentForView
import org.joda.time.DateTime
import java.util.UUID

fun CommentForView<*, *>.toOutput(): Any = this.let { c ->
    object {
        val id: UUID = c.id
        val content: String = c.content
        val childrenCount: Int = c.childrenCount ?: 0
        val createdAt: DateTime = c.createdAt
        val updatedAt: DateTime = c.updatedAt
        val parent: Any? = c.parent?.let { p ->
            object {
                val id: UUID = p.id
                val reference: String = p.reference
            }
        }
        val target: Any = c.target.let { t ->
            object {
                val id: UUID = t.id
                val reference: String = t.reference
            }
        }
        val createdBy: Any = c.createdBy.toOutput()
        val votes: Any = c.votes.let { v ->
            object {
                val up: Int = v.up
                val neutral: Int = v.neutral
                val down: Int = v.down
                val total: Int = v.total
                val score: Int = v.score
            }
        }
    }
}

fun <C : CitizenCreatorI> CommentForUpdate<*, C>.toOutput(): Any = this.let { c ->
    object {
        val id: UUID = c.id
        val content: String = c.content
        val childrenCount: Int = 0
        val createdAt: DateTime = c.createdAt
        val updatedAt: DateTime = c.createdAt
        val parent: Any? = c.parent?.let { p ->
            object {
                val id: UUID = p.id
                val reference: String = p.reference
            }
        }
        val target: Any = c.target.let { t ->
            object {
                val id: UUID = t.id
                val reference: String = t.reference
            }
        }
        val createdBy: Any = c.createdBy.toOutput()
        val votes: Any = object {
            val up: Int = 0
            val neutral: Int = 0
            val down: Int = 0
            val total: Int = 0
            val score: Int = 0
        }
    }
}
