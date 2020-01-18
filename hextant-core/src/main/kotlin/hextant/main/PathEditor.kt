/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.*
import hextant.base.AbstractEditor
import kserial.*
import reaktive.value.now
import reaktive.value.reactiveVariable
import java.nio.file.Path
import java.nio.file.Paths

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

    override fun serialize(output: Output, context: SerialContext) {
        output.writeString(_result.now.force().toString())
    }

    override fun deserialize(input: Input, context: SerialContext) {
        val p = input.readString()
        _result.now = ok(Paths.get(p))
    }
}