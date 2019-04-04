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

class SyntaxErrorInspection(override val inspected: Editable<*>) : AbstractInspection<Editable<*>>() {
    override val isProblem: ReactiveBoolean = inspected.result.map {
        it.isErr
    }

    override fun message(): String {
        val er = inspected.result.now as Err
        val msg = er.message
        return "Syntax error: $msg"
    }

    override val description: String
        get() = "Highlights syntactical errors"

    override val severity: Severity
        get() = Severity.Error
}