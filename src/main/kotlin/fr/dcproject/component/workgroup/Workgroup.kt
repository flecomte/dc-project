package fr.dcproject.component.workgroup

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.UpdatedAt
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.citizen.CitizenBasicI
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenWithUserI
import fr.dcproject.component.workgroup.WorkgroupWithMembersI.Member
import fr.dcproject.component.workgroup.WorkgroupWithMembersI.Member.Role
import fr.postgresjson.entity.Serializable
import java.util.UUID

@Deprecated("")
data class Workgroup <C : CitizenBasicI>(
    override val id: UUID = UUID.randomUUID(),
    override var name: String,
    override var description: String,
    override var logo: String? = null,
    override var anonymous: Boolean = true,
    override val createdBy: C,
    override var members: List<Member<C>> = emptyList()
) : WorkgroupWithAuthI<C>,
    WorkgroupSimple<C>(
        id,
        name,
        description,
        logo,
        anonymous,
        createdBy
    ),
    CreatedAt by CreatedAt.Imp(),
    UpdatedAt by UpdatedAt.Imp()

@Deprecated("")
open class WorkgroupSimple<Z : CitizenI>(
    id: UUID? = null,
    open var name: String,
    open var description: String,
    open var logo: String? = null,
    open var anonymous: Boolean = true,
    createdBy: Z
) : WorkgroupRef(id),
    CreatedBy<Z> by CreatedBy.Imp(createdBy),
    DeletedAt by DeletedAt.Imp()

class WorkgroupCart(
    override val id: UUID,
    override val name: String
) : WorkgroupCartI

interface WorkgroupCartI : EntityI {
    val name: String
}
open class WorkgroupRef(
    id: UUID? = null
) : Entity(id ?: UUID.randomUUID()), WorkgroupI

interface WorkgroupWithAuthI<Z : CitizenWithUserI> : WorkgroupWithMembersI<Z>, CreatedBy<Z>, DeletedAt {
    val anonymous: Boolean

    fun isMember(user: UserI): Boolean = members.isMember(user)
    fun isMember(citizen: CitizenI): Boolean = members.isMember(citizen)

    fun hasRole(expectedRole: Role, user: UserI): Boolean = members.hasRole(expectedRole, user)
    fun hasRole(expectedRole: Role, citizen: CitizenI): Boolean = members.hasRole(expectedRole, citizen)

    fun getRoles(user: UserI): List<Role> = members.getRoles(user)
    fun getRoles(citizen: CitizenI): List<Role> = members.getRoles(citizen)
}

interface WorkgroupWithMembersI<Z : CitizenI> : WorkgroupI {
    var members: List<Member<Z>>

    class Member<C : CitizenI> (
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

fun List<CitizenI>.hasCitizen(citizen: CitizenI): Boolean = this.map { it.id }.contains(citizen.id)

fun <Z : CitizenWithUserI> List<Member<Z>>.isMember(user: UserI): Boolean =
    map { it.citizen.user.id }.contains(user.id)

fun <Z : CitizenI> List<Member<Z>>.isMember(citizen: CitizenI): Boolean =
    map { it.citizen.id }.contains(citizen.id)

fun <Z : CitizenI> List<Member<Z>>.hasRole(expectedRole: Role, citizen: CitizenI): Boolean =
    any { member -> member.citizen.id == citizen.id && member.roles.any { it == expectedRole } }

fun <Z : CitizenWithUserI> List<Member<Z>>.hasRole(expectedRole: Role, user: UserI): Boolean =
    any { member -> member.citizen.user.id == user.id && member.roles.any { it == expectedRole } }

fun <Z : CitizenWithUserI> List<Member<Z>>.getRoles(user: UserI): List<Role> =
    firstOrNull { it.citizen.user.id == user.id }?.roles ?: emptyList()

fun <Z : CitizenWithUserI> List<Member<Z>>.getRoles(citizen: CitizenI): List<Role> =
    firstOrNull { it.citizen.id == citizen.id }?.roles ?: emptyList()

interface WorkgroupI : EntityI
