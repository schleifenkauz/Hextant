/**
 * @author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.context.Context
import hextant.context.extend
import hextant.core.Editor
import hextant.core.editor.Expander
import hextant.serial.IndexAccessor
import reaktive.value.ReactiveInt
import reaktive.value.binding.binding
import reaktive.value.binding.map
import reaktive.value.reactiveValue

fun Context.child() = extend()

val Editor<*>.line: ReactiveInt
    get() = when (val p = parent) {
        is ProgramEditor -> reactiveValue(0)
        is StatementListEditor -> if (this is Expander<*, *>) accessor.map { (it as IndexAccessor).index } + p.line else expander!!.line
        is Editor<*> -> p.line
        else                   -> error("No line found")
    }

private operator fun ReactiveInt.plus(other: ReactiveInt): ReactiveInt = binding(this, other, Int::plus)