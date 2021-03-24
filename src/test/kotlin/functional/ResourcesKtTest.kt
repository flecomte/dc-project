package functional

import fr.dcproject.common.utils.readResource
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("functional"))
class ResourcesKtTest {
    @Test
    fun readResource() {
        "/simpleTextFile.txt".readResource {
            assertEquals("Hello", it)
        }.let {
            assertEquals("Hello", it)
        }

        "/simpleTextFile.txt".readResource().let {
            assertEquals("Hello", it)
        }
    }
}
