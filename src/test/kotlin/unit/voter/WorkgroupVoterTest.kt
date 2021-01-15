package unit.voter

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.entity.WorkgroupRef
import fr.dcproject.entity.WorkgroupWithMembersI
import fr.dcproject.security.voter.WorkgroupVoter
import fr.dcproject.user
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.VoterException
import fr.ktorVoter.can
import io.ktor.application.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.*
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.util.*
import fr.dcproject.entity.Workgroup as WorkgroupEntity

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tag("voter")
internal class WorkgroupVoterTest {
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

    private val workgroupPublic = WorkgroupEntity(
        createdBy = tesla,
        description = "Super desc",
        name = "super name",
        anonymous = false,
        members = listOf(WorkgroupWithMembersI.Member(tesla, listOf(WorkgroupWithMembersI.Member.Role.MASTER)))
    )

    private val workgroupAnon = WorkgroupEntity(
        createdBy = tesla,
        description = "Super desc",
        name = "super name",
        members = listOf(WorkgroupWithMembersI.Member(tesla, listOf(WorkgroupWithMembersI.Member.Role.MASTER))),
        anonymous = true
    )

    private val workgroupref = WorkgroupRef()

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `support workgroup`(): Unit = WorkgroupVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            this(WorkgroupVoter.Action.VIEW, it, workgroupPublic).vote `should be` Vote.GRANTED
            this(WorkgroupVoter.Action.VIEW, it, article1).vote `should be` Vote.ABSTAIN
            this(p, it, workgroupPublic).vote `should be` Vote.ABSTAIN
        }
    }

    @Test
    fun `can be view your workgroup`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(WorkgroupVoter.Action.VIEW, it, workgroupPublic) `should be` true
        }
    }

    @Test
    fun `can be view your workgroup if is not public`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(WorkgroupVoter.Action.VIEW, it, workgroupAnon) `should be` true
        }
    }

    @Test
    fun `can be view workgroup of other if is public`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(WorkgroupVoter.Action.VIEW, it, workgroupPublic) `should be` true
        }
    }

    @Test
    fun `can not be view workgroup of other if is not public`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(WorkgroupVoter.Action.VIEW, it, workgroupAnon) `should be` false
        }
    }

    @Test
    fun `can be not view the workgroup if is null`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            assertThrows<NoSubjectDefinedException> {
                can(WorkgroupVoter.Action.VIEW, it, null)
            }
        }
    }

    @Test
    fun `can be view your workgroup list`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            listOf(workgroupPublic).map { workgroup ->
                can(WorkgroupVoter.Action.VIEW, it, workgroup)
            }.all { it } `should be` true
        }
    }

    @Test
    fun `can be create workgroup`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(WorkgroupVoter.Action.CREATE, it, workgroupPublic) `should be` true
        }
    }

    @Test
    fun `can not be create workgroup if not connected`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(WorkgroupVoter.Action.CREATE, it, workgroupPublic) `should be` false
        }
    }

    @Test
    fun `can be delete workgroup if owner`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(WorkgroupVoter.Action.DELETE, it, workgroupPublic) `should be` true
        }
    }

    @Test
    fun `can not be delete workgroup if not owner`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(WorkgroupVoter.Action.DELETE, it, workgroupPublic) `should be` false
        }
    }

    @Test
    fun `can be update workgroup if owner`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(WorkgroupVoter.Action.UPDATE, it, workgroupPublic) `should be` true
        }
    }

    @Test
    fun `can not be update workgroup if not owner`(): Unit = listOf(WorkgroupVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(WorkgroupVoter.Action.UPDATE, it, workgroupPublic) `should be` false
        }
    }

    @Test
    fun `can not be update workgroup if workgroup has no user`() {
        Assertions.assertThrows(VoterException::class.java) {
            listOf(WorkgroupVoter()).run {
                mockk<ApplicationCall> {
                    every { user } returns tesla.user
                }.let {
                    can(WorkgroupVoter.Action.UPDATE, it, workgroupref)
                }
            }
        }
    }
}