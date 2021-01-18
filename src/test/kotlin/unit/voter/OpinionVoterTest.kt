package unit.voter

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.entity.Opinion
import fr.dcproject.entity.OpinionChoice
import fr.dcproject.security.voter.OpinionVoter
import fr.dcproject.voter.Vote.DENIED
import fr.dcproject.voter.Vote.GRANTED
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tag("voter")
internal class OpinionVoterTest {
    private val tesla = CitizenBasic(
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla"),
        followAnonymous = false
    )

    private val einstein = CitizenBasic(
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "einstein@best.com",
        name = CitizenI.Name("Albert", "Einstein"),
        followAnonymous = true
    )

    private val einstein2 = CitizenCart(
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Albert", "Einstein")
    )

    private val article1 = ArticleForView(
        content = "Hi",
        createdBy = einstein2,
        description = "blablabla",
        title = "Super article"
    )

    private val opinion1 = Opinion(
        createdBy = tesla,
        target = article1,
        choice = OpinionChoice(
            name = "Opinion1",
            target = listOf("article")
        )
    )

    init {
        mockkStatic("fr.dcproject.component.auth.CitizenContextKt")
    }

    @Test
    fun `can be view the opinion`() {
        OpinionVoter()
            .canView(opinion1, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can be view the opinion list`() {
        OpinionVoter()
            .canView(listOf(opinion1), tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can be opinion an article`() {
        OpinionVoter()
            .canCreate(opinion1, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be opinion if not connected`() {
        OpinionVoter()
            .canCreate(opinion1, null)
            .vote `should be` DENIED
    }

    @Test
    fun `can be remove opinion`() {
        OpinionVoter()
            .canDelete(opinion1, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be remove opinion if not connected`() {
        OpinionVoter()
            .canDelete(opinion1, null)
            .vote `should be` DENIED
    }

    @Test
    fun `can not be remove opinion of other user`() {
        OpinionVoter()
            .canDelete(opinion1, einstein)
            .vote `should be` DENIED
    }
}
