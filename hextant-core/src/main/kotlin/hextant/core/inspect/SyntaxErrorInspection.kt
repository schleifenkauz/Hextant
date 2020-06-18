/**
 *@author Nikolaus Knop
 */

package hextant.core.inspect

import hextant.core.Editor
import hextant.inspect.*
import reaktive.value.ReactiveBoolean
import reaktive.value.binding.map
import reaktive.value.now
import validated.Validated.Invalid
import validated.isInvalid

internal object SyntaxErrorInspection : AbstractInspection<Editor<*>>() {
    override val id: String
        get() = "syntax-error"

    override fun InspectionBody<Editor<*>>.isProblem(): ReactiveBoolean = inspected.result.map { it.isInvalid }

    override fun InspectionBody<Editor<*>>.message(): String {
        val invalid = inspected.result.now as Invalid
        val msg = invalid.reason
        return "Syntax error: $msg"
    }

    override val description: String
        get() = "Highlights syntactical errors"
    override val severity: Severity
        get() = Severity.Error
}