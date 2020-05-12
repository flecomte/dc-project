package fr.dcproject.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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