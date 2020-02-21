/**
 * @author Nikolaus Knop
 */

@file:Suppress("FunctionName")

package hextant.test

import org.jetbrains.spek.api.dsl.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE

@UseExperimental(ExperimentalContracts::class)
fun TestContainer.TEST(description: String, body: TestBody.() -> Unit) {
    contract { callsInPlace(body, EXACTLY_ONCE) }
    test(description, Pending.No, body)
}

@UseExperimental(ExperimentalContracts::class)
fun TestContainer.IT(description: String, body: TestBody.() -> Unit) {
    contract { callsInPlace(body, EXACTLY_ONCE) }
    it(description, body)
}

@UseExperimental(ExperimentalContracts::class)
fun SpecBody.ACTION(description: String, body: ActionBody.() -> Unit) {
    contract { callsInPlace(body, EXACTLY_ONCE) }
    action(description, Pending.No, body)
}

@UseExperimental(ExperimentalContracts::class)
fun SpecBody.ON(description: String, body: ActionBody.() -> Unit) {
    contract { callsInPlace(body, EXACTLY_ONCE) }
    on(description, body)
}

@UseExperimental(ExperimentalContracts::class)
fun SpecBody.DESCRIBE(description: String, body: SpecBody.() -> Unit) {
    contract { callsInPlace(body, EXACTLY_ONCE) }
    describe(description, body)
}

@UseExperimental(ExperimentalContracts::class)
fun SpecBody.GIVEN(description: String, body: SpecBody.() -> Unit) {
    contract { callsInPlace(body, EXACTLY_ONCE) }
    given(description, body)
}