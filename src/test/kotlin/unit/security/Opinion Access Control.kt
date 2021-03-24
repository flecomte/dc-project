package unit.security

import fr.dcproject.common.security.AccessDecision.DENIED
import fr.dcproject.common.security.AccessDecision.GRANTED
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.auth.database.UserCreator
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.opinion.OpinionAccessControl
import fr.dcproject.component.opinion.database.Opinion
import fr.dcproject.component.opinion.database.OpinionChoice
import org.amshove.kluent.`should be`
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
internal class `Opinion Access Control` {
    private val tesla = CitizenCreator(
        user = UserCreator(
            username = "nicolas-tesla",
        ),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla"),
        followAnonymous = false
    )

    private val einstein = CitizenRef(
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
    )

    private val einstein2 = CitizenCreator(
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
        user = UserCreator(
            username = "albert-einstein",
        ),
        name = CitizenI.Name("Albert", "Einstein"),
        email = "albert-einstein@email.com"
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

    @Test
    fun `can be view the opinion`() {
        OpinionAccessControl()
            .canView(opinion1, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be view the opinion list`() {
        OpinionAccessControl()
            .canView(listOf(opinion1), tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be opinion an article`() {
        OpinionAccessControl()
            .canCreate(opinion1, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be opinion if not connected`() {
        OpinionAccessControl()
            .canCreate(opinion1, null)
            .decision `should be` DENIED
    }

    @Test
    fun `can be remove opinion`() {
        OpinionAccessControl()
            .canDelete(opinion1, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be remove opinion if not connected`() {
        OpinionAccessControl()
            .canDelete(opinion1, null)
            .decision `should be` DENIED
    }

    @Test
    fun `can not be remove opinion of other user`() {
        OpinionAccessControl()
            .canDelete(opinion1, einstein)
            .decision `should be` DENIED
    }
}
