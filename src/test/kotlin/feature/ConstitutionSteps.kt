package feature

import fr.dcproject.entity.*
import fr.dcproject.repository.CommentConstitution
import fr.dcproject.utils.toUUID
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import org.joda.time.DateTime
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.entity.User as UserEntity
import fr.dcproject.repository.Constitution as ConstitutionRepository
import fr.dcproject.repository.Citizen as CitizenRepository

class ConstitutionSteps : En, KoinTest {
    init {
        Given("I have {int} constitution") { nb: Int ->
            repeat(nb) {
                createConstitution()
            }
        }

        Given("I have constitution") { extraData: DataTable? ->
            createConstitution(extraData)
        }

        Given("I have comment created by {word} {word} on constitution {string}:") { firstName: String, lastName: String, constitutionId: String, extraData: DataTable? ->
            commentConstitution(constitutionId, firstName, lastName, extraData)
        }
        Given("I have comment created by {word} {word} on constitution {string}") { firstName: String, lastName: String, constitutionId: String ->
            commentConstitution(constitutionId, firstName, lastName)
        }
    }

    private fun createConstitution(extraData: DataTable? = null) {
        val params = extraData?.asMap<String, String>(String::class.java, String::class.java)
        val createdByUsername = params?.get("createdBy")
        val username = (createdByUsername ?: "username"+UUID.randomUUID().toString())
            .toLowerCase().replace(' ', '-')

        val createdBy = if (createdByUsername != null) {
            get<CitizenRepository>().findByUsername(username) ?: error("Citizen not exist")
        } else {
            val first = "firstName"+UUID.randomUUID().toString()
            val last = "lastName"+UUID.randomUUID().toString()
            Citizen(
                birthday = DateTime.now(),
                name = CitizenI.Name(
                    first,
                    last
                ),
                email = "$first@fakeemail.com",
                user = UserEntity(username = username, plainPassword = "azerty")
            ).also {
                get<CitizenRepository>().insertWithUser(it)
            }
        }

        val title1 = ConstitutionSimple.TitleSimple<ArticleRef>(
            name = "My Title"
        )

        val constitution = ConstitutionSimple<CitizenSimple, ConstitutionSimple.TitleSimple<ArticleRef>>(
            id = params?.get("id")?.toUUID() ?: UUID.randomUUID(),
            title = "hello",
            titles = mutableListOf(title1),
            anonymous = false,
            createdBy = createdBy
        )
        get<ConstitutionRepository>().upsert(constitution)
    }

    private fun commentConstitution(constitutionId: String, firstName: String, lastName: String, extraData: DataTable? = null) {
        val params = extraData?.asMap<String, String>(String::class.java, String::class.java)

        val constitution = get<ConstitutionRepository>().findById(UUID.fromString(constitutionId)) ?: error("Constitution not exist")

        val citizen = get<CitizenRepository>().findByUsername(("$firstName-$lastName".toLowerCase()).toLowerCase().replace(' ', '-')) ?: error("Citizen not exist")

        val comment: CommentEntity<ConstitutionRef> = CommentEntity(
            id = params?.get("id")?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
            createdBy = citizen,
            target = constitution,
            content = params?.get("content") ?: "hello"
        )
        get<CommentConstitution>().comment(comment)
    }
}