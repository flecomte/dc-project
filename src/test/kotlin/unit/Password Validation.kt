package unit

import fr.dcproject.common.validation.passwordScore
import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.Validation
import org.amshove.kluent.`should be equal to`
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
internal class `Password Validation` {
    @Test
    fun password() {
        "1234567890".passwordScore() `should be equal to` 10
        "1234567A".passwordScore() `should be equal to` 10
        "1234Aa".passwordScore() `should be equal to` 10
        "12Aab".passwordScore() `should be equal to` 11
        "1234Aa".passwordScore() `should be equal to` 10
        "12abCD-+".passwordScore() `should be equal to` 18
        "Abcde12!".passwordScore() `should be equal to` 15
        "Hello world".passwordScore() `should be equal to` 16
        "hello WORLD".passwordScore() `should be equal to` 17
    }

    @Test
    fun passwordScore() {
        Validation<ObjectToValid> {
            ObjectToValid::password {
                this.passwordScore(10)
            }
        }.run {
            validate(ObjectToValid("1234567890")) `should be instance of` Valid::class
            validate(ObjectToValid("12345678")) `should be instance of` Invalid::class
        }
    }

    class ObjectToValid(val password: String)
}
