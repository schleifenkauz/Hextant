/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.base.AbstractEditor
import hextant.base.EditorSnapshot
import hextant.context.Context
import kserial.Serializable
import reaktive.value.now
import reaktive.value.reactiveVariable
import validated.*
import validated.reaktive.ReactiveValidated
import java.nio.file.Path

/**
 * An editor for [Path]s.
 */
class PathEditor private constructor(context: Context) : AbstractEditor<Path, PathEditorView>(context), Serializable {
    constructor(context: Context, initial: Path) : this(context) {
        _result.now = valid(initial)
    }

    private val _result = reactiveVariable<Validated<Path>>(invalidComponent())

    override val result: ReactiveValidated<Path> get() = _result

    /**
     * Set the result to the given [new] path.
     */
    fun choosePath(new: Path) {
        _result.now = valid(new)
        views {
            displayPath(new)
        }
    }

    override fun viewAdded(view: PathEditorView) {
        result.now.ifValid { view.displayPath(it) }
    }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    private class Snapshot(original: PathEditor) : EditorSnapshot<PathEditor>(original) {
        private val p = original._result.now

        override fun reconstruct(editor: PathEditor) {
            editor._result.now = p
        }
    }
}