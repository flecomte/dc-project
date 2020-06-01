package fr.dcproject.entity

import fr.dcproject.entity.WorkgroupWithMembersI.Member
import fr.dcproject.entity.WorkgroupWithMembersI.Member.Role
import fr.postgresjson.entity.EntityI
import fr.postgresjson.entity.immutable.*
import fr.postgresjson.entity.mutable.EntityDeletedAt
import fr.postgresjson.entity.mutable.EntityDeletedAtImp
import java.util.*

class Workgroup(
    id: UUID? = null,
    name: String,
    description: String,
    logo: String? = null,
    anonymous: Boolean = true,
    createdBy: CitizenBasic,
    override var members: List<Member<CitizenBasic>> = emptyList()
) : WorkgroupWithAuthI<CitizenBasic>,
    WorkgroupSimple<CitizenBasic>(
        id,
        name,
        description,
        logo,
        anonymous,
        createdBy
    ),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityUpdatedAt by EntityUpdatedAtImp()

open class WorkgroupSimple<Z : CitizenRef>(
    id: UUID? = null,
    var name: String,
    var description: String,
    var logo: String? = null,
    var anonymous: Boolean = true,
    createdBy: Z
) : WorkgroupRef(id),
    EntityCreatedBy<Z> by EntityCreatedByImp(createdBy),
    EntityDeletedAt by EntityDeletedAtImp()

open class WorkgroupRef(
    id: UUID? = null
) : UuidEntity(id ?: UUID.randomUUID()), WorkgroupI

interface WorkgroupWithAuthI<Z : CitizenWithUserI> : WorkgroupWithMembersI<Z>, EntityCreatedBy<Z>, EntityDeletedAt {
    val anonymous: Boolean

    fun isMember(user: UserI): Boolean = members.isMember(user)
    fun isMember(citizen: CitizenWithUserI): Boolean = members.isMember(citizen)

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
    ) : EntityI {
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

interface WorkgroupI : UuidEntityI