package assert

import kotlin.test.assertTrue

infix fun IntProgression.assertContain(expected: Int) {
    assertTrue(this.contains(expected), "Expected $this less than $expected")
}
