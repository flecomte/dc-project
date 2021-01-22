package unit.security

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.opinion.OpinionChoiceAccessControl
import fr.dcproject.component.opinion.entity.OpinionChoice
import fr.dcproject.security.AccessDecision.GRANTED
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
@Tag("security")
internal class OpinionChoiceAccessControlTest {
    private val tesla = CitizenBasic(
        id = UUID.fromString("e6efc288-4283-4729-a268-6debb18de1a0"),
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla"),
        followAnonymous = false
    )

    private val tesla2 = CitizenCart(
        id = UUID.fromString("e6efc288-4283-4729-a268-6debb18de1a0"),
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Nicolas", "Tesla")
    )

    private val article1 = ArticleForView(
        content = "Hi",
        createdBy = tesla2,
        description = "blablabla",
        title = "Super article"
    )

    private val choice1 = OpinionChoice(
        name = "Opinion1",
        target = listOf()
    )

    @Test
    fun `can be view the opinion choice`() {
        OpinionChoiceAccessControl()
            .canView(choice1, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be view the opinion choice list`() {
        OpinionChoiceAccessControl()
            .canView(listOf(choice1), tesla)
            .decision `should be` GRANTED
    }
}
