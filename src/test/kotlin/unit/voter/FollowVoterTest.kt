package unit.voter

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.entity.Follow
import fr.dcproject.security.voter.FollowVoter
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
internal class FollowVoterTest {
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
    private val tesla2 = Citizen(
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

    private val einstein3 = Citizen(
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

    private val article1 = ArticleForView(
        content = "Hi",
        createdBy = einstein2,
        description = "blablabla",
        title = "Super article"
    )

    private val follow1 = Follow(
        createdBy = tesla,
        target = article1
    )

    private val followAnon = Follow(
        createdBy = einstein,
        target = article1
    )

    init {
        mockkStatic("fr.dcproject.component.auth.CitizenContextKt")
    }

    @Test
    fun `can be view the follow`() {
        FollowVoter()
            .canView(follow1, tesla2)
            .vote `should be` GRANTED
    }

    @Test
    fun `can be view the follow list`() {
        FollowVoter()
            .canView(listOf(follow1), tesla2)
            .vote `should be` GRANTED
    }

    @Test
    fun `can be view your anonymous follow`() {
        FollowVoter()
            .canView(followAnon, einstein3)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be view the anonymous follow of other`() {
        FollowVoter()
            .canView(followAnon, tesla2)
            .vote `should be` DENIED
    }

    @Test
    fun `can be follow article`() {
        FollowVoter()
            .canCreate(follow1, tesla2)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be follow article if not connected`() {
        FollowVoter()
            .canCreate(follow1, null)
            .vote `should be` DENIED
    }

    @Test
    fun `can be unfollow article`() {
        FollowVoter()
            .canDelete(follow1, tesla2)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be unfollow article if not connected`() {
        FollowVoter()
            .canDelete(follow1, null)
            .vote `should be` DENIED
    }
}
