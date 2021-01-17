package unit.voter

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.comment.generic.CommentForView
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.voter.Vote.DENIED
import fr.dcproject.voter.Vote.GRANTED
import fr.postgresjson.connexion.Paginated
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.util.*
import fr.dcproject.component.article.ArticleRepository as ArticleRepo

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tag("voter")
internal class CommentVoterTest {
    private val tesla = Citizen(
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla")
    )
    private val einstein = Citizen(
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "einstein@best.com",
        name = CitizenI.Name("Albert", "Einstein")
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

    private val comment1 = CommentForView(
        content = "Hello",
        createdBy = tesla,
        target = article1
    )

    private val commentForUpdate = CommentForUpdate(
        content = "Hello",
        createdBy = tesla,
        target = article1
    )

    private val comment2 = CommentForView(
        content = "Hello2",
        createdBy = einstein,
        target = article1
    )

    private val commentTargetDeleted = CommentForView(
        content = "Hello",
        createdBy = tesla,
        target = ArticleForView(
            content = "Hi",
            createdBy = einstein2,
            description = "blablabla",
            title = "Super article",
            workgroup = null
        ).copy(deletedAt = DateTime.now())
    )

    private val commentTargetNoUser = CommentForView(
        content = "Hello",
        createdBy = tesla,
        target = ArticleRef()
    )

    private val repoArticle1 = mockk<ArticleRepo> {
        every { findVersionsByVersionId(1, 1, any()) } returns Paginated(listOf(article1), 0, 1, 1)
    }

    @Test
    fun `can be view the comment`() {
        CommentVoter()
            .canView(comment1, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can be view the comment list`() {
        CommentVoter()
            .canView(listOf(comment1, comment2), einstein)
            .vote `should be` GRANTED
    }

    @Test
    fun `can be update your comment`() {
        CommentVoter()
            .canUpdate(comment1, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be update other comment`() {
        CommentVoter()
            .canUpdate(comment1, einstein)
            .vote `should be` DENIED
    }

    @Test
    fun `can be create a comment`() {
        CommentVoter()
            .canCreate(comment1, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be create a comment if target is deleted`() {
        CommentVoter()
            .canCreate(commentTargetDeleted, tesla)
            .vote `should be` DENIED
    }

    @Test
    fun `can not be create a comment with other creator`() {
        CommentVoter()
            .canCreate(comment1, einstein)
            .vote `should be` DENIED
    }

    @Test
    fun `can not be create a comment if not connected`() {
        CommentVoter()
            .canCreate(comment1, null)
            .vote `should be` DENIED
    }
}