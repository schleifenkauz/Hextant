/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import org.nikok.reaktive.value.ReactiveBoolean

internal class InspectionImpl<T : Any>(
    override val isProblem: ReactiveBoolean,
    override val inspected: T,
    override val description: String,
    private val message: () -> String,
    override val severity: Severity
) : AbstractInspection<T>() {
    override fun message(): String = message.invoke()
}