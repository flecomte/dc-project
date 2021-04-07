package unit

import fr.dcproject.common.validation.email
import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.Validation
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
@Tags(Tag("validation"), Tag("unit"))
internal class `Email Validation` {
    @Test
    fun passwordScore() {
        Validation<ObjectToValid> {
            ObjectToValid::email {
                email()
            }
        }.run {
            validate(ObjectToValid("abc@123.com")) `should be instance of` Valid::class
            validate(ObjectToValid("abc123.com")) `should be instance of` Invalid::class
        }
    }

    class ObjectToValid(val email: String)
}
