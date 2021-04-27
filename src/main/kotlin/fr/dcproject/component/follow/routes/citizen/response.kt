package fr.dcproject.component.follow.routes.citizen

import fr.dcproject.common.response.toOutput
import fr.dcproject.component.follow.database.FollowForView
import org.joda.time.DateTime
import java.util.UUID

fun FollowForView<*>.toOutput(): Any = this.let { f ->
    object {
        val id: UUID = f.id
        val createdBy: Any = f.createdBy.toOutput()
        val target: Any = f.target.let { t ->
            object {
                val id: UUID = t.id
                val reference: String = f.target.reference
            }
        }
        val createdAt: DateTime = f.createdAt
    }
}
