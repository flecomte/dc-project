package fr.dcproject.component.opinion.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.component.opinion.database.Opinion
import fr.dcproject.component.opinion.database.OpinionChoice
import org.joda.time.DateTime
import java.util.UUID

fun OpinionChoice.toOutput(): Any = this.let { o ->
    object {
        val id: UUID = o.id
        val name: String = o.name
        val target: List<String>? = o.target
    }
}

fun Opinion<*>.toOutput(): Any = this.let { o ->
    object {
        val id: UUID = o.id
        val name: String = o.getName()
        val target: Any = o.target.let { t ->
            val id: UUID = t.id
            val reference: String = t.reference
        }
        val choice: Any = o.choice.toOutput()
        val reference: String = o.reference
        val createdBy: Any = o.createdBy.toOutput()
        val createdAt: DateTime = o.createdAt
    }
}
