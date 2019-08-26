/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.blocky.End
import hextant.blocky.Executable
import reaktive.value.reactiveVariable

class NextExecutableEditor(context: Context) : AbstractEditor<Executable, EditorView>(context) {
    private val _result = reactiveVariable(ok(End as Executable))

    override val result: EditorResult<Executable> get() = _result

    fun setNext(next: Executable) {
        _result.set(ok(next))
    }

    fun clearNext() {
        _result.set(ok(End))
    }
}