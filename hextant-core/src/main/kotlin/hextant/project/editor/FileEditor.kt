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
    private constructor(context: Context) : this(context, FileNameEditor(context, ""))

    constructor(
        context: Context,
        name: FileNameEditor,
        editor: Editor<R>,
        parentPath: ReactivePath
    ) : this(context, name) {
        path = parentPath.resolve(fileName.text)
        val manager = context[HextantFileManager]
        _content = manager.get(editor, path)
        bindResult()
    }

    private lateinit var _content: HextantFile<Editor<R>>
    override lateinit var path: ReactivePath
        private set
    private val _result = reactiveVariable<CompileResult<File<R>>>(childErr())
    val fileName = name.moveTo(context)
    private var obs: Observer? = null
    private lateinit var subscription: Subscription

    val content get() = _content

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
        _content = manager.from(path)
        bindResult()
    }

    private fun bindResult() {
        updateEditor(_content.get())
        subscription = content.read.subscribe { _, e ->
            obs?.kill()
            updateEditor(e)
        }
    }

    private fun updateEditor(e: Editor<R>) {
        obs = _result.bind(result2(fileName, e) { name, content -> ok(File(name, content)) })
        e.initFile(this)
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeUntyped(fileName)
        content.write()
    }

    override val result: EditorResult<File<R>> get() = _result
}