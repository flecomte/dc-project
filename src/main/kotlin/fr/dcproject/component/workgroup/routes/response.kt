package fr.dcproject.component.workgroup.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.workgroup.database.WorkgroupForView
import fr.dcproject.component.workgroup.database.WorkgroupWithMembersI
import org.joda.time.DateTime
import java.util.UUID

fun WorkgroupForView<CitizenCreator>.toOutput(): Any = this.let { w ->
    object {
        val id: UUID = w.id
        val name: String = w.name
        val description: String = w.description
        val logo: String? = w.logo
        val anonymous: Boolean = w.anonymous
        val createdAt: DateTime = w.createdAt
        val createdBy: Any = w.createdBy.toOutput()
        val members: Any = w.members.toOutput()
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

fun List<WorkgroupWithMembersI.Member<CitizenCreator>>.toOutput(): Any {
    return this.map { m ->
        object {
            val citizen: Any = m.citizen.toOutput()
            val roles: List<String> = m.roles.map { it.toString() }
        }
    }
}
