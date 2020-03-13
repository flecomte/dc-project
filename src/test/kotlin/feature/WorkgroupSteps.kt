package feature

import fr.dcproject.entity.*
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import org.joda.time.DateTime
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.Workgroup as WorkgroupRepository

class WorkgroupSteps : En, KoinTest {
    init {
        When("I have workgroup:") { body: DataTable ->
            val data = body.asMap<String, String>(String::class.java, String::class.java)

            val creator = if (data["created_by"] != null) {
                CitizenRef(UUID.fromString(data["created_by"]))
            } else {
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
            val owner = if (data["owner"] != null) {
                CitizenRef(UUID.fromString(data["owner"]))
            } else {
                creator
            }

            val workgroup = WorkgroupSimple(
                id = UUID.fromString(data["id"] ?: UUID.randomUUID().toString()),
                name = data["name"] ?: "Les Incoruptible",
                description = data["description"] ?: "La vie est notre jeux",
                createdBy = creator,
                owner = owner,
                anonymous = (data["anonymous"] ?: false) == true
            )

            get<WorkgroupRepository>().upsert(workgroup)
        }
    }
}