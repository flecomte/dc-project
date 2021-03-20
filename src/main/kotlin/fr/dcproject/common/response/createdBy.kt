package fr.dcproject.common.response

import fr.dcproject.component.citizen.database.CitizenCreatorI
import java.util.UUID

fun CitizenCreatorI.toOutput(): Any = this.let { c ->
    object {
        val id: UUID = c.id
        val name: Any = c.name.let { n ->
            object {
                val firstName: String = n.firstName
                val lastName: String = n.lastName
            }
        }
        val user: Any = c.user.let { u ->
            object {
                val username: String = u.username
            }
        }
    }
}
