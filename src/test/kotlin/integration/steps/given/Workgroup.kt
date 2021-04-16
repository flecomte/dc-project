package integration.steps.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.auth.database.UserForCreate
import fr.dcproject.component.citizen.database.Citizen
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenForCreate
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRepository
import fr.dcproject.component.workgroup.database.WorkgroupForUpdate
import fr.dcproject.component.workgroup.database.WorkgroupForView
import fr.dcproject.component.workgroup.database.WorkgroupRepository
import fr.dcproject.component.workgroup.database.WorkgroupWithMembersI.Member
import fr.dcproject.component.workgroup.database.WorkgroupWithMembersI.Member.Role.MASTER
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
    callback: WorkgroupForView<CitizenCreator>.() -> Unit = {},
) {
    val workgroup: WorkgroupForView<CitizenCreator> = createWorkgroup(id?.toUUID(), name, description, anonymous, createdBy)
    callback(workgroup)
}
fun WorkgroupForView<CitizenCreator>.`With members`(
    vararg member: CitizenI.Name
) {
    addMemberToWorkgroup(this, *member)
}

@Suppress("UNCHECKED_CAST")
fun addMemberToWorkgroup(workgroup: WorkgroupForView<CitizenCreator>, vararg membersNames: CitizenI.Name) {
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
): WorkgroupForView<CitizenCreator> {
    val citizenRepository: CitizenRepository by lazy { GlobalContext.get().koin.get() }
    val workgroupRepository: WorkgroupRepository by lazy { GlobalContext.get().koin.get() }

    val creatorName = createdBy ?: CitizenI.Name("Paul", "Langevin")
    val creator = citizenRepository.findByName(creatorName) ?: run {
        val username = ("username" + UUID.randomUUID().toString())
            .toLowerCase().replace(' ', '-')
        val user = UserForCreate(
            username = username,
            password = "Azerty123!",
        )
        CitizenForCreate(
            name = creatorName,
            email = "$username@dc-project.fr",
            birthday = DateTime.now(),
            user = user
        ).let {
            citizenRepository.insertWithUser(it) ?: error("Unable to create User")
        }
    }

    val workgroup = WorkgroupForUpdate(
        id = id ?: UUID.randomUUID(),
        name = name ?: "Les Incoruptible",
        description = description ?: "La vie est notre jeux",
        createdBy = creator,
        anonymous = anonymous ?: false,
        members = listOf(Member(creator, listOf(MASTER)))
    )

    return workgroupRepository.upsert(workgroup)
}
