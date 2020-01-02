/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.project.File
import hextant.serial.HextantFile
import hextant.serial.RootEditor
import reaktive.Observer
import reaktive.event.subscribe
import reaktive.value.reactiveVariable

class FileEditor<R : Any>(context: Context, name: FileNameEditor, val root: HextantFile<RootEditor<R>>) :
    AbstractEditor<File<R>, EditorView>(context), ProjectItemEditor<R, File<R>> {
    constructor(context: Context, root: HextantFile<RootEditor<R>>) : this(context, FileNameEditor(context), root)

    private val _result = reactiveVariable<CompileResult<File<R>>>(childErr())
    val name = name.moveTo(context)

    private var obs: Observer? = null
    private val subscription = root.read.subscribe(this::bindResult)

    private fun bindResult(e: Editor<R>) {
        obs?.kill()
        obs = _result.bind(result2(name, e) { name, content -> ok(File(name, content)) })
    }

    init {
        child(this.name)
        if (root.inMemory()) bindResult(root.get())
    }

    fun dispose() {
        obs?.kill()
        subscription.cancel()
    }

    override val result: EditorResult<File<R>> get() = _result
}