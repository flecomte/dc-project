package unit.security

import fr.dcproject.common.security.AccessDecision.DENIED
import fr.dcproject.common.security.AccessDecision.GRANTED
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.comment.generic.CommentForView
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
@Tags(Tag("common/security"), Tag("unit"))
internal class CommentAccessControlTest {
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

    @Test
    fun `can be view the comment`() {
        CommentAccessControl()
            .canView(comment1, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be view the comment list`() {
        CommentAccessControl()
            .canView(listOf(comment1, comment2), einstein)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be update your comment`() {
        CommentAccessControl()
            .canUpdate(comment1, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be update other comment`() {
        CommentAccessControl()
            .canUpdate(comment1, einstein)
            .decision `should be` DENIED
    }

    @Test
    fun `can be create a comment`() {
        CommentAccessControl()
            .canCreate(comment1, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be create a comment if target is deleted`() {
        CommentAccessControl()
            .canCreate(commentTargetDeleted, tesla)
            .decision `should be` DENIED
    }

    @Test
    fun `can not be create a comment with other creator`() {
        CommentAccessControl()
            .canCreate(comment1, einstein)
            .decision `should be` DENIED
    }

    @Test
    fun `can not be create a comment if not connected`() {
        CommentAccessControl()
            .canCreate(comment1, null)
            .decision `should be` DENIED
    }
}
