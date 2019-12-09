/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.blocky.End
import hextant.blocky.Executable
import reaktive.value.binding.flatMap
import reaktive.value.binding.map
import reaktive.value.reactiveValue
import reaktive.value.reactiveVariable

class NextExecutableEditor(context: Context, nxt: Executable? = null) :
    AbstractEditor<Executable, EditorView>(context) {
    private val next = reactiveVariable(null as ExecutableEditor<*>?)

    override val result: EditorResult<Executable> = next.flatMap {
        it?.result?.map { res -> res.or(childErr()) } ?: reactiveValue(ok(End))
    }

    fun setNext(next: ExecutableEditor<*>) {
        this.next.set(next)
    }

    fun clearNext() {
        next.set(null)
    }
}