/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.project.File
import hextant.serial.*
import kserial.*
import reaktive.Observer
import reaktive.event.Subscription
import reaktive.value.reactiveVariable

class FileEditor<R : Any> private constructor(
    context: Context,
    name: FileNameEditor
) : AbstractEditor<File<R>, EditorView>(context), ProjectItemEditor<R, File<R>>, Serializable {
    constructor(
        context: Context,
        name: FileNameEditor,
        editor: Editor<R>,
        parentPath: ReactivePath
    ) : this(context, name) {
        path = parentPath.resolve(fileName.text)
        val manager = context[HextantFileManager]
        _root = manager.get(editor, path)
        bindResult()
    }

    private lateinit var _root: HextantFile<Editor<R>>
    override lateinit var path: ReactivePath
        private set
    private val _result = reactiveVariable<CompileResult<File<R>>>(childErr())
    val fileName = name.moveTo(context)
    private var obs: Observer? = null
    private lateinit var subscription: Subscription

    val root get() = _root

    init {
        child(fileName)
    }

    fun dispose() {
        obs?.kill()
        subscription.cancel()
    }

    override fun deserialize(input: Input, context: SerialContext) {
        input.readInplace(fileName)
        val parentPath = getProjectItemEditorParent()?.path ?: ReactivePath.empty()
        path = parentPath.resolve(fileName.text)
        val manager = this.context[HextantFileManager]
        _root = manager.from(path)
        bindResult()
    }

    private fun bindResult() {
        updateEditor(_root.get())
        subscription = root.read.subscribe { _, e ->
            obs?.kill()
            updateEditor(e)
        }
    }

    private fun updateEditor(e: Editor<R>) {
        obs = _result.bind(result2(fileName, e) { name, content -> ok(File(name, content)) })
        e.setFile(this)
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeUntyped(fileName)
        root.write()
    }

    override val result: EditorResult<File<R>> get() = _result
}