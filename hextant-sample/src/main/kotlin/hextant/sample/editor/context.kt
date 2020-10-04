/**
 * @author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.context.Context
import hextant.context.extend
import hextant.core.Editor
import hextant.serial.IndexAccessor
import reaktive.collection.binding.sum
import reaktive.list.binding.values
import reaktive.value.ReactiveInt
import reaktive.value.binding.*
import reaktive.value.reactiveValue
import java.util.*

fun Context.child() = extend()

private val linesCache = WeakHashMap<StatementEditor<*>, ReactiveInt>()
private val lineCache = WeakHashMap<Editor<*>, ReactiveInt>()

val StatementEditor<*>.lines: ReactiveInt
    get() = linesCache.getOrPut(this) {
        when (this) {
            is BlockEditor -> statements.editors.map { e -> e.lines }.values().sum()
            is IfStatementEditor -> consequence.lines + reactiveValue(1)
            is WhileLoopEditor -> body.lines + reactiveValue(1)
            is ForLoopEditor -> body.lines + reactiveValue(1)
            is StatementExpander -> editor.flatMap { e -> e?.lines ?: reactiveValue(1) }
            else                 -> reactiveValue(1)
        }
    }

val Editor<*>.indexInBlock: ReactiveInt get() = accessor.map { (it as IndexAccessor).index }

val Editor<*>.line: ReactiveInt
    get() = lineCache.getOrPut(this) {
        when (val p = parent) {
            is ProgramEditor -> reactiveValue(0)
            is StatementListEditor ->
                if (this is StatementExpander)
                    p.editors[indexInBlock - 1].flatMap {
                        it?.let { prev -> prev.line + prev.lines } ?: p.line
                    }
                else expander!!.line
            is Editor<*> -> p.line
            else                   -> error("No line found")
        }
    }

private operator fun ReactiveInt.plus(other: ReactiveInt): ReactiveInt = binding(this, other, Int::plus)
private operator fun ReactiveInt.plus(other: Int): ReactiveInt = binding(this, reactiveValue(other), Int::plus)
private operator fun ReactiveInt.minus(other: Int): ReactiveInt = binding(this, reactiveValue(other), Int::minus)