package unit.voter

import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.workgroup.WorkgroupVoter
import fr.dcproject.component.workgroup.WorkgroupWithMembersI
import fr.dcproject.voter.Vote.DENIED
import fr.dcproject.voter.Vote.GRANTED
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.util.UUID
import fr.dcproject.component.workgroup.Workgroup as WorkgroupEntity

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

    @Test
    fun `can be view your workgroup`() {
        WorkgroupVoter()
            .canView(workgroupPublic, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can be view your workgroup if is not public`() {
        WorkgroupVoter()
            .canView(workgroupAnon, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can be view workgroup of other if is public`() {
        WorkgroupVoter()
            .canView(workgroupPublic, einstein)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be view workgroup of other if is not public`() {
        WorkgroupVoter()
            .canView(workgroupAnon, einstein)
            .vote `should be` DENIED
    }

    @Test
    fun `can be view your workgroup list`() {
        WorkgroupVoter()
            .canView(listOf(workgroupPublic, workgroupAnon), tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can be create workgroup`() {
        WorkgroupVoter()
            .canCreate(workgroupPublic, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be create workgroup if not connected`() {
        WorkgroupVoter()
            .canCreate(workgroupPublic, null)
            .vote `should be` DENIED
    }

    @Test
    fun `can be delete workgroup if owner`() {
        WorkgroupVoter()
            .canDelete(workgroupPublic, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be delete workgroup if not owner`() {
        WorkgroupVoter()
            .canDelete(workgroupPublic, einstein)
            .vote `should be` DENIED
    }

    @Test
    fun `can be update workgroup if owner`() {
        WorkgroupVoter()
            .canUpdate(workgroupPublic, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be update workgroup if not owner`() {
        WorkgroupVoter()
            .canUpdate(workgroupPublic, einstein)
            .vote `should be` DENIED
    }
}
