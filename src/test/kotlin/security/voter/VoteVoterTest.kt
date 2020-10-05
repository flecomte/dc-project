package fr.dcproject.security.voter

import fr.dcproject.entity.*
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.can
import fr.ktorVoter.canAll
import fr.postgresjson.connexion.Paginated
import io.ktor.application.ApplicationCall
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import fr.dcproject.entity.Vote as VoteEntity

@KtorExperimentalLocationsAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("voter")
internal class VoteVoterTest {
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
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "einstein@best.com",
        name = CitizenI.Name("Albert", "Einstein"),
        followAnonymous = true
    )

    private val article1 = Article(
        content = "Hi",
        createdBy = einstein,
        description = "blablabla",
        title = "Super article"
    )

    private val vote1 = VoteEntity(
        createdBy = tesla,
        target = article1,
        note = 1
    )

    private val voteOnDeleted = VoteEntity(
        createdBy = tesla,
        target = Article(
            content = "Hi",
            createdBy = einstein,
            description = "blablabla",
            title = "Super article"
        ).apply { deletedAt = DateTime.now() },
        note = 1
    )

    private val voteWithoutUser = VoteEntity(
        createdBy = tesla,
        target = ArticleRef(),
        note = 1
    )

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `support vote`(): Unit = VoteVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            this(VoteVoter.Action.VIEW, it, vote1) `should be` Vote.GRANTED
            this(VoteVoter.Action.VIEW, it, article1) `should be` Vote.ABSTAIN
            this(p, it, vote1) `should be` Vote.ABSTAIN
        }
    }

    @Test
    fun `can be view your the vote`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(VoteVoter.Action.VIEW, it, vote1) `should be` true
        }
    }

    @Test
    fun `can not be view vote of other`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(VoteVoter.Action.VIEW, it, vote1) `should be` false
        }
    }

    @Test
    fun `can be not view the vote if is null`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(VoteVoter.Action.VIEW, it, null) `should be` false
        }
    }

    @Test
    fun `can be view your votes list`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            canAll(VoteVoter.Action.VIEW, it, listOf(vote1)) `should be` true
        }
    }

    @Test
    fun `can be vote an article`(): Unit {
        listOf(VoteVoter(), ArticleVoter(mockk())).run {
            mockk<ApplicationCall> {
                every { user } returns tesla.user
            }.let {
                can(VoteVoter.Action.CREATE, it, vote1) `should be` true
            }
        }
    }

    @Test
    fun `can not be vote if not connected`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(VoteVoter.Action.CREATE, it, vote1) `should be` false
        }
    }

    @Test
    fun `can not be vote an article if article is deleted`(): Unit = listOf(VoteVoter(), ArticleVoter(mockk())).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(VoteVoter.Action.CREATE, it, voteOnDeleted) `should be` false
        }
    }

    @Test
    fun `can not be vote an article if article have no user`(): Unit = listOf(VoteVoter(), ArticleVoter(mockk())).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(VoteVoter.Action.CREATE, it, voteWithoutUser) `should be` false
        }
    }

    @Test
    fun `can not be comment an article if article is deleted`(): Unit = listOf(VoteVoter(), ArticleVoter(mockk())).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(CommentVoter.Action.CREATE, it, voteOnDeleted) `should be` false
        }
    }
}