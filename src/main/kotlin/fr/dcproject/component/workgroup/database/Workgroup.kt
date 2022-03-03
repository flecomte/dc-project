package fr.dcproject.component.workgroup.database

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.UpdatedAt
import fr.dcproject.component.auth.database.UserI
import fr.dcproject.component.citizen.database.CitizenCreatorI
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenWithUserI
import fr.dcproject.component.workgroup.database.WorkgroupWithMembersI.Member
import fr.dcproject.component.workgroup.database.WorkgroupWithMembersI.Member.Role
import org.joda.time.DateTime
import java.util.UUID

data class WorkgroupForView<C : CitizenCreatorI>(
    override val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val logo: String? = null,
    override var anonymous: Boolean = true,
    override val createdBy: C,
    override var members: List<Member<C>> = emptyList()
) : WorkgroupWithAuthI<C>,
    WorkgroupRef(id),
    CreatedBy<C> by CreatedBy.Imp(createdBy),
    CreatedAt by CreatedAt.Imp(),
    UpdatedAt by UpdatedAt.Imp(),
    DeletedAt by DeletedAt.Imp()

data class WorkgroupForUpdate<C : CitizenWithUserI>(
    override val id: UUID,
    override val name: String,
    override val description: String,
    override val createdBy: C,
    override val logo: String? = null,
    override val anonymous: Boolean = true,
    override val members: List<Member<C>> = listOf(),
    override val deletedAt: DateTime? = null,
) : WorkgroupRef(id),
    WorkgroupForUpdateI<C>,
    CreatedBy<C> by CreatedBy.Imp(createdBy)

sealed interface WorkgroupForUpdateI<C : CitizenWithUserI> : WorkgroupWithAuthI<C>, WorkgroupCartI, CreatedBy<C> {
    val description: String
    val logo: String?
}

data class WorkgroupCart(
    override val id: UUID,
    override val name: String
) : WorkgroupCartI

sealed interface WorkgroupCartI : EntityI {
    val name: String
}

open class WorkgroupRef(
    id: UUID? = null
) : Entity(id ?: UUID.randomUUID()), WorkgroupI

sealed interface WorkgroupWithAuthI<Z : CitizenWithUserI> : WorkgroupWithMembersI<Z>, CreatedBy<Z>, DeletedAt {
    val anonymous: Boolean

    fun isMember(user: UserI): Boolean = members.isMember(user)
    fun isMember(citizen: CitizenI): Boolean = members.isMember(citizen)

    fun hasRole(expectedRole: Role, user: UserI): Boolean = members.hasRole(expectedRole, user)
    fun hasRole(expectedRole: Role, citizen: CitizenI): Boolean = members.hasRole(expectedRole, citizen)

    fun getRoles(user: UserI): Collection<Role> = members.getRoles(user)
    fun getRoles(citizen: CitizenI): Collection<Role> = members.getRoles(citizen)
}

sealed interface WorkgroupWithMembersI<Z : CitizenI> : WorkgroupI {
    val members: List<Member<Z>>

    class Member<C : CitizenI>(
        val citizen: C,
        val roles: List<Role> = emptyList()
    ) : fr.postgresjson.entity.EntityI {
        enum class Role {
            MASTER,
            MANAGER,
            EDITOR,
            REPORTER
        }
    }
}

fun Collection<CitizenI>.hasCitizen(citizen: CitizenI): Boolean = this.map { it.id }.contains(citizen.id)

fun <Z : CitizenWithUserI> Collection<Member<Z>>.isMember(user: UserI): Boolean =
    map { it.citizen.user.id }.contains(user.id)

fun <Z : CitizenI> Collection<Member<Z>>.isMember(citizen: CitizenI): Boolean =
    map { it.citizen.id }.contains(citizen.id)

fun <Z : CitizenI> Collection<Member<Z>>.hasRole(expectedRole: Role, citizen: CitizenI): Boolean =
    any { member -> member.citizen.id == citizen.id && member.roles.any { it == expectedRole } }

fun <Z : CitizenWithUserI> Collection<Member<Z>>.hasRole(expectedRole: Role, user: UserI): Boolean =
    any { member -> member.citizen.user.id == user.id && member.roles.any { it == expectedRole } }

fun <Z : CitizenWithUserI> Collection<Member<Z>>.getRoles(user: UserI): Collection<Role> =
    firstOrNull { it.citizen.user.id == user.id }?.roles ?: emptyList()

fun <Z : CitizenWithUserI> Collection<Member<Z>>.getRoles(citizen: CitizenI): Collection<Role> =
    firstOrNull { it.citizen.id == citizen.id }?.roles ?: emptyList()

interface WorkgroupI : EntityI
