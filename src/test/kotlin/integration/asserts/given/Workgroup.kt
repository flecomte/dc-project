package integration.asserts.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.auth.UserForCreate
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
import org.koin.test.get
import java.util.UUID

fun TestApplicationEngine.`Given I have workgroup`(
    id: String? = null,
    name: String? = null,
    description: String? = null,
    anonymous: Boolean? = null,
    createdByUsername: String? = null
) {
    createWorkgroup(id?.toUUID(), name, description, anonymous, createdByUsername)
}

private fun createWorkgroup(
    id: UUID? = null,
    name: String? = null,
    description: String? = null,
    anonymous: Boolean? = null,
    createdByUsername: String? = null
): Workgroup<CitizenBasic> {
    val username = (createdByUsername ?: "username" + UUID.randomUUID().toString())
        .toLowerCase().replace(' ', '-')

    val citizenRepository: CitizenRepository by lazy<CitizenRepository> { GlobalContext.get().koin.get() }
    val workgroupRepository: WorkgroupRepository by lazy<WorkgroupRepository> { GlobalContext.get().koin.get() }

    val creator = citizenRepository.findByUsername(username.toLowerCase().replace(' ', '-'))
        ?: run {
            val user = UserForCreate(
                username = username,
                password = "azerty",
            )
            CitizenForCreate(
                name = CitizenI.Name("Paul", "Langevin"),
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
