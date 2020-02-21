/**
 * @author Nikolaus Knop
 */

@file:Suppress("FunctionName")

package hextant.test

import org.jetbrains.spek.api.dsl.*

fun TestContainer.TEST(description: String, body: TestBody.() -> Unit) {
    test(description, Pending.No, body)
}

fun TestContainer.IT(description: String, body: TestBody.() -> Unit) {
    it(description, body)
}

fun SpecBody.ACTION(description: String, body: ActionBody.() -> Unit) {
    action(description, Pending.No, body)
}

fun SpecBody.ON(description: String, body: ActionBody.() -> Unit) {
    on(description, body)
}

fun SpecBody.DESCRIBE(description: String, body: SpecBody.() -> Unit) {
    describe(description, body)
}

fun SpecBody.GIVEN(description: String, body: SpecBody.() -> Unit) {
    given(description, body)
}