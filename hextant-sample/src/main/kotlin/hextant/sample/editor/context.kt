/**
 * @author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.context.Context
import hextant.context.extend
import hextant.core.Editor
import hextant.serial.IndexAccessor
import reaktive.value.ReactiveInt
import reaktive.value.binding.map

fun Context.child() = extend()

val StatementEditor<*>.indexInBlock: ReactiveInt get() = accessor.map { (it as? IndexAccessor)?.index ?: 0 }

val Editor<*>.enclosingStatement: StatementEditor<*>
    get() = when (val p = parent) {
        is StatementEditor -> p
        is Editor<*> -> p.enclosingStatement
        else               -> throw NoSuchElementException("No enclosing statement editor found")
    }

val ExprEditor<*>.indexInBlock get() = enclosingStatement.indexInBlock