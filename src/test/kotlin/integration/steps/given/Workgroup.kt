package integration.steps.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.auth.UserForCreate
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenForCreate
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.workgroup.Workgroup
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.WorkgroupWithMembersI.Member
import fr.dcproject.component.workgroup.WorkgroupWithMembersI.Member.Role.MASTER
import io.ktor.server.testing.TestApplicationEngine
import org.joda.time.DateTime
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have workgroup`(
    id: String? = null,
    name: String? = null,
    description: String? = null,
    anonymous: Boolean? = null,
    createdBy: CitizenI.Name? = null,
    callback: Workgroup<CitizenBasic>.() -> Unit = {},
) {
    val workgroup: Workgroup<CitizenBasic> = createWorkgroup(id?.toUUID(), name, description, anonymous, createdBy)
    callback(workgroup)
}
fun Workgroup<CitizenBasic>.`With members`(
    vararg member: CitizenI.Name
) {
    addMemberToWorkgroup(this, *member)
}

fun addMemberToWorkgroup(workgroup: Workgroup<CitizenBasic>, vararg membersNames: CitizenI.Name) {
    val citizenRepository: CitizenRepository by lazy { GlobalContext.get().koin.get() }
    val workgroupRepository: WorkgroupRepository by lazy { GlobalContext.get().koin.get() }

    val newMembers: List<Member<CitizenI>> = membersNames.map { memberName ->
        val member: Citizen = citizenRepository.findByName(memberName) ?: error("Citizen not exist")
        Member(member, listOf(Member.Role.EDITOR))
    }
    workgroupRepository.updateMembers<CitizenI>(
        workgroup,
        workgroup.members as List<Member<CitizenI>> + newMembers
    )
}

private fun createWorkgroup(
    id: UUID? = null,
    name: String? = null,
    description: String? = null,
    anonymous: Boolean? = null,
    createdBy: CitizenI.Name? = null,
): Workgroup<CitizenBasic> {
    val citizenRepository: CitizenRepository by lazy { GlobalContext.get().koin.get() }
    val workgroupRepository: WorkgroupRepository by lazy { GlobalContext.get().koin.get() }

    val createdBy = createdBy ?: CitizenI.Name("Paul", "Langevin")
    val creator = citizenRepository.findByName(createdBy) ?: run {
        val username = ("username" + UUID.randomUUID().toString())
            .toLowerCase().replace(' ', '-')
        val user = UserForCreate(
            username = username,
            password = "azerty",
        )
        CitizenForCreate(
            name = createdBy,
            email = "$username@dc-project.fr",
            birthday = DateTime.now(),
            user = user
        ).let {
            citizenRepository.insertWithUser(it) ?: error("Unable to create User")
        }
    }

    val workgroup = Workgroup(
        id = id ?: UUID.randomUUID(),
        name = name ?: "Les Incoruptible",
        description = description ?: "La vie est notre jeux",
        createdBy = creator,
        anonymous = (anonymous ?: false) == true,
        members = listOf(Member(creator, listOf(MASTER)))
    )

    return workgroupRepository.upsert(workgroup)
}
