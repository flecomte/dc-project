package unit.security

import fr.dcproject.common.security.AccessDecision.DENIED
import fr.dcproject.common.security.AccessDecision.GRANTED
import fr.dcproject.component.auth.database.UserCreator
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.database.WorkgroupWithMembersI
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.util.UUID
import fr.dcproject.component.workgroup.database.WorkgroupForView as WorkgroupEntity

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tags(Tag("security"), Tag("unit"), Tag("workgroup"))
internal class `Workgroup Access Control` {
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
        WorkgroupAccessControl()
            .canView(workgroupPublic, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be view your workgroup if is not public`() {
        WorkgroupAccessControl()
            .canView(workgroupAnon, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be view workgroup of other if is public`() {
        WorkgroupAccessControl()
            .canView(workgroupPublic, einstein)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be view workgroup of other if is not public`() {
        WorkgroupAccessControl()
            .canView(workgroupAnon, einstein)
            .decision `should be` DENIED
    }

    @Test
    fun `can be view your workgroup list`() {
        WorkgroupAccessControl()
            .canView(listOf(workgroupPublic, workgroupAnon), tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be create workgroup`() {
        WorkgroupAccessControl()
            .canCreate(workgroupPublic, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be create workgroup if not connected`() {
        WorkgroupAccessControl()
            .canCreate(workgroupPublic, null)
            .decision `should be` DENIED
    }

    @Test
    fun `can be delete workgroup if owner`() {
        WorkgroupAccessControl()
            .canDelete(workgroupPublic, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be delete workgroup if not owner`() {
        WorkgroupAccessControl()
            .canDelete(workgroupPublic, einstein)
            .decision `should be` DENIED
    }

    @Test
    fun `can be update workgroup if owner`() {
        WorkgroupAccessControl()
            .canUpdate(workgroupPublic, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be update workgroup if not owner`() {
        WorkgroupAccessControl()
            .canUpdate(workgroupPublic, einstein)
            .decision `should be` DENIED
    }
}
