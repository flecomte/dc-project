package fr.dcproject.security.voter

import fr.dcproject.entity.*
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.VoterException
import fr.ktorVoter.can
import io.ktor.application.ApplicationCall
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import fr.dcproject.entity.Workgroup as WorkgroupEntity

@KtorExperimentalLocationsAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
            this(WorkgroupVoter.Action.VIEW, it, workgroupPublic) `should be` Vote.GRANTED
            this(WorkgroupVoter.Action.VIEW, it, article1) `should be` Vote.ABSTAIN
            this(p, it, workgroupPublic) `should be` Vote.ABSTAIN
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
            can(WorkgroupVoter.Action.VIEW, it, null) `should be` false
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