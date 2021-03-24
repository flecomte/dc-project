package assert

import kotlin.test.assertTrue

fun <T : Number> T.assertGreaterThan(expected: T, message: String) {
    assertTrue(expected.toDouble() > this.toDouble(), message)
}
infix fun <T : Number> T.assertGreaterThan(expected: T) {
    assertTrue(expected.toDouble() >= this.toDouble(), "Expected $this greater than $expected")
}
fun <T : Number> T.assertLessThan(expected: T, message: String) {
    assertTrue(expected.toDouble() <= this.toDouble(), message)
}
infix fun <T : Number> T.assertLessThan(expected: T) {
    assertTrue(expected.toDouble() <= this.toDouble(), "Expected $this less than $expected")
}
