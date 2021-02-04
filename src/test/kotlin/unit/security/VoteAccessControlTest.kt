package unit.security

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.entity.VoteForUpdate
import fr.dcproject.security.AccessDecision.DENIED
import fr.dcproject.security.AccessDecision.GRANTED
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.util.UUID
import fr.dcproject.component.vote.entity.Vote as VoteEntity

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tags(Tag("security"), Tag("unit"))
internal class VoteAccessControlTest {
    private val tesla = Citizen(
        id = UUID.fromString("a1e35c99-9d33-4fb4-9201-58d7071243bb"),
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla"),
        followAnonymous = false
    )
    private val tesla3 = CitizenBasic(
        id = UUID.fromString("a1e35c99-9d33-4fb4-9201-58d7071243bb"),
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla"),
        followAnonymous = false
    )

    private val einstein = Citizen(
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

    private val vote1 = VoteEntity(
        createdBy = tesla3,
        target = article1,
        note = 1
    )

    private val voteForUpdate = VoteForUpdate(
        createdBy = tesla,
        target = article1,
        note = 1
    )

    private val voteOnDeleted = VoteForUpdate(
        createdBy = tesla,
        target = ArticleForView(
            content = "Hi",
            createdBy = einstein2,
            description = "blablabla",
            title = "Super article"
        ).copy(deletedAt = DateTime.now()),
        note = 1
    )

    @Test
    fun `can be view your the vote`() {
        VoteAccessControl()
            .canView(vote1, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be view vote of other`() {
        VoteAccessControl()
            .canView(vote1, einstein)
            .decision `should be` DENIED
    }

    @Test
    fun `can be view your votes list`() {
        VoteAccessControl()
            .canView(listOf(vote1), tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be vote an article`() {
        VoteAccessControl()
            .canCreate(voteForUpdate, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be vote if not connected`() {
        VoteAccessControl()
            .canCreate(voteForUpdate, null)
            .decision `should be` DENIED
    }

    @Test
    fun `can not be vote an article if article is deleted`() {
        VoteAccessControl()
            .canCreate(voteOnDeleted, tesla)
            .decision `should be` DENIED
    }
}