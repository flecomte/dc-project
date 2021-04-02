package unit.security

import fr.dcproject.common.security.AccessDecision.GRANTED
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.opinion.OpinionChoiceAccessControl
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
@Tags(Tag("security"), Tag("unit"), Tag("opinion"))
internal class `OpinionChoice Access Control` {
    private val tesla = CitizenRef(
        id = UUID.fromString("e6efc288-4283-4729-a268-6debb18de1a0"),
    )

    private val tesla2 = CitizenRef(
        id = UUID.fromString("e6efc288-4283-4729-a268-6debb18de1a0"),
    )

    private val choice1 = OpinionChoice(
        name = "Opinion1",
        target = listOf()
    )

    @Test
    fun `can be view the opinion choice`() {
        OpinionChoiceAccessControl()
            .canView(choice1, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be view the opinion choice list`() {
        OpinionChoiceAccessControl()
            .canView(listOf(choice1), tesla)
            .decision `should be` GRANTED
    }
}
