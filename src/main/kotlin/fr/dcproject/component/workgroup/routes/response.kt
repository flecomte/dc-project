package fr.dcproject.component.workgroup.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.component.workgroup.database.WorkgroupForView
import org.joda.time.DateTime
import java.util.UUID

fun WorkgroupForView<*>.toOutput(): Any = this.let { w ->
    object {
        val id: UUID = w.id
        val name: String = w.name
        val description: String = w.description
        val logo: String? = w.logo
        val anonymous: Boolean = w.anonymous
        val createdAt: DateTime = w.createdAt
        val createdBy: Any = w.createdBy.toOutput()
        val members: Any = w.members.map { m ->
            object {
                val citizen: Any = object {
                    val id: UUID = m.citizen.id
                    val name: Any = m.citizen.name.let { n ->
                        object {
                            val firstName: String = n.firstName
                            val lastName: String = n.lastName
                        }
                    }
                }
                val roles: List<String> = m.roles.map { it.toString() }
            }
        }
    }
}

fun WorkgroupForView<*>.toOutputListing(): Any = this.let { w ->
    object {
        val id: UUID = w.id
        val name: String = w.name
        val description: String = w.description
        val logo: String? = w.logo
        val createdAt: DateTime = w.createdAt
    }
}
