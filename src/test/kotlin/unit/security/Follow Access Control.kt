package unit.security

import fr.dcproject.common.security.AccessDecision.DENIED
import fr.dcproject.common.security.AccessDecision.GRANTED
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.auth.database.User
import fr.dcproject.component.auth.database.UserCreator
import fr.dcproject.component.auth.database.UserI
import fr.dcproject.component.citizen.database.Citizen
import fr.dcproject.component.citizen.database.CitizenCart
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.database.FollowForView
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tags(Tag("security"), Tag("unit"))
internal class `Follow Access Control` {
    private val tesla = CitizenCreator(
        user = UserCreator(
            username = "nicolas-tesla",
        ),
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

    private val einstein = CitizenCreator(
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
        user = UserCreator(
            username = "albert-einstein",
        ),
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

    private val follow1 = FollowForView(
        createdBy = tesla,
        target = article1
    )

    private val followAnon = FollowForView(
        createdBy = einstein,
        target = article1
    )

    @Test
    fun `can be view the follow`() {
        FollowAccessControl()
            .canView(follow1, tesla2)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be view the follow list`() {
        FollowAccessControl()
            .canView(listOf(follow1), tesla2)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be view your anonymous follow`() {
        FollowAccessControl()
            .canView(followAnon, einstein3)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be view the anonymous follow of other`() {
        FollowAccessControl()
            .canView(followAnon, tesla2)
            .decision `should be` DENIED
    }

    @Test
    fun `can be follow article`() {
        FollowAccessControl()
            .canCreate(follow1, tesla2)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be follow article if not connected`() {
        FollowAccessControl()
            .canCreate(follow1, null)
            .decision `should be` DENIED
    }

    @Test
    fun `can be unfollow article`() {
        FollowAccessControl()
            .canDelete(follow1, tesla2)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be unfollow article if not connected`() {
        FollowAccessControl()
            .canDelete(follow1, null)
            .decision `should be` DENIED
    }
}
