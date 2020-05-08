/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.*
import hextant.base.AbstractEditor
import hextant.base.EditorSnapshot
import kserial.Serializable
import reaktive.value.now
import reaktive.value.reactiveVariable
import java.nio.file.Path

class PathEditor private constructor(context: Context) : AbstractEditor<Path, PathEditorView>(context), Serializable {
    constructor(context: Context, initial: Path) : this(context) {
        _result.now = ok(initial)
    }

    private val _result = reactiveVariable<CompileResult<Path>>(childErr())

    override val result: EditorResult<Path> get() = _result

    fun choosePath(new: Path) {
        _result.now = ok(new)
        views {
            displayPath(new)
        }
    }

    override fun viewAdded(view: PathEditorView) {
        result.now.ifOk { view.displayPath(it) }
    }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    private class Snapshot(original: PathEditor) : EditorSnapshot<PathEditor>(original) {
        private val p = original._result.now

        override fun reconstruct(editor: PathEditor) {
            editor._result.now = p
        }
    }
}