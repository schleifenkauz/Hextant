/**
 *@author Nikolaus Knop
 */

package hextant.core.inspect

import hextant.Editor
import hextant.inspect.AbstractInspection
import hextant.inspect.Severity
import reaktive.value.ReactiveBoolean
import reaktive.value.binding.map
import reaktive.value.now
import validated.Validated.Invalid
import validated.isInvalid

internal class SyntaxErrorInspection(inspected: Editor<*>) : AbstractInspection<Editor<*>>(inspected, inspected) {
    override val isProblem: ReactiveBoolean = inspected.result.map { it.isInvalid }

    override fun message(): String {
        val invalid = inspected.result.now as Invalid
        val msg = invalid.reason
        return "Syntax error: $msg"
    }

    override val description: String
        get() = "Highlights syntactical errors"

    override val severity: Severity
        get() = Severity.Error
}