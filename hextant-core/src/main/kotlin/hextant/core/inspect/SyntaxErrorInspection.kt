/**
 *@author Nikolaus Knop
 */

package hextant.core.inspect

import hextant.*
import hextant.inspect.AbstractInspection
import hextant.inspect.Severity
import reaktive.value.ReactiveBoolean
import reaktive.value.binding.map
import reaktive.value.now

internal class SyntaxErrorInspection(override val location: Editor<*>) : AbstractInspection() {
    override val isProblem: ReactiveBoolean = location.result.map { it.isErr }

    override fun message(): String {
        val er = location.result.now as Err
        val msg = er.message
        return "Syntax error: $msg"
    }

    override val description: String
        get() = "Highlights syntactical errors"

    override val severity: Severity
        get() = Severity.Error
}