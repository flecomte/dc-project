package steps

import fr.dcproject.component.auth.User
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.entity.*
import fr.dcproject.entity.WorkgroupWithMembersI.Member
import fr.dcproject.utils.toUUID
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import org.joda.time.DateTime
import org.junit.Assert
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*
import fr.dcproject.repository.Workgroup as WorkgroupRepository

class WorkgroupSteps : En, KoinTest {
    init {
        When("I have members in workgroup {string}:") { workgroupId: String, members: DataTable ->
            val membersRefs = members.asList()
                .map { Member(
                    citizen = CitizenRef(it.toUUID()),
                    roles = listOf(Member.Role.MASTER)
                ) }

            get<WorkgroupRepository>().addMembers(WorkgroupRef(workgroupId.toUUID()), membersRefs)
        }

        When("I have workgroup:") { body: DataTable ->
            val data = body.asMap<String, String>(String::class.java, String::class.java)

            val creator = data["created_by"]?.let {
                get<CitizenRepository>().findByUsername(it.toLowerCase().replace(' ', '-'))
            } ?: run {
                val username = "paul-langevin".toLowerCase() + UUID.randomUUID()
                val user = User(
                    username = username,
                    plainPassword = "azerty"
                )
                Citizen(
                    name = CitizenI.Name("Paul", "Langevin"),
                    email = "$username@dc-project.fr",
                    birthday = DateTime.now(),
                    user = user
                ).also {
                    get<CitizenRepository>().insertWithUser(it)
                }
            }

            val workgroup = Workgroup(
                id = UUID.fromString(data["id"] ?: UUID.randomUUID().toString()),
                name = data["name"] ?: "Les Incoruptible",
                description = data["description"] ?: "La vie est notre jeux",
                createdBy = creator,
                anonymous = (data["anonymous"] ?: false) == true,
                members = listOf(Member(creator, listOf(Member.Role.MASTER)))
            )

            get<WorkgroupRepository>().upsert(workgroup)
        }

        Then("The workgroup {string} exists") { id: String ->
            Assert.assertNotNull(get<WorkgroupRepository>().findById(id.toUUID()))
        }

        Then("The workgroup {string} not exists") { id: String ->
            Assert.assertNull(get<WorkgroupRepository>().findById(id.toUUID()))
        }
    }
}