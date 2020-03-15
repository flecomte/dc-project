package fr.dcproject.entity

import fr.postgresjson.entity.immutable.*
import fr.postgresjson.entity.mutable.EntityDeletedAt
import fr.postgresjson.entity.mutable.EntityDeletedAtImp
import java.util.*

class Workgroup(
    id: UUID?,
    name: String,
    description: String,
    logo: String? = null,
    anonymous: Boolean = true,
    owner: CitizenBasic,
    createdBy: CitizenBasic,
    override var members: List<CitizenBasic> = emptyList()
) : WorkgroupWithAuthI<CitizenBasic>,
    WorkgroupSimple<CitizenBasic>(
        id,
        name,
        description,
        logo,
        anonymous,
        owner,
        createdBy
    ),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityUpdatedAt by EntityUpdatedAtImp()

open class WorkgroupSimple<Z : CitizenRef>(
    id: UUID?,
    var name: String,
    var description: String,
    var logo: String? = null,
    var anonymous: Boolean = true,
    var owner: Z,
    createdBy: Z
) : WorkgroupRef(id),
    EntityCreatedBy<Z> by EntityCreatedByImp(createdBy),
    EntityDeletedAt by EntityDeletedAtImp()

open class WorkgroupRef(
    id: UUID?
) : UuidEntity(id ?: UUID.randomUUID()), WorkgroupI

interface WorkgroupWithAuthI<Z : CitizenWithUserI> : WorkgroupWithMembersI<Z>, EntityCreatedBy<Z>, EntityDeletedAt {
    val anonymous: Boolean
    val owner: Z

    fun isMember(user: UserI): Boolean =
        members.map { it.user.id }.contains(user.id) || owner.user.id == user.id

    fun isMember(citizen: CitizenWithUserI): Boolean =
        isMember(citizen.user)
}

interface WorkgroupWithMembersI<Z : CitizenI> : WorkgroupI {
    var members: List<Z>
}

interface WorkgroupI : UuidEntityI