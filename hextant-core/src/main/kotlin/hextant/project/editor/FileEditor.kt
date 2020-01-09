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
    constructor(context: Context, name: FileNameEditor, editor: Editor<R>, p: String) : this(context, name) {
        initialize(p, editor)
    }

    constructor(context: Context, name: FileNameEditor, editor: Editor<R>)
            : this(context, name, editor, context[PathGenerator].genFile())

    private fun initialize(p: String, editor: Editor<R>) {
        path = p
        val manager = context[HextantFileManager]
        val pr = context[SerialProperties.projectRoot]
        _root = manager.get(pr.resolve(path), editor)
        bindResult(editor)
        subscription = root.read.subscribe { _, e -> bindResult(e) }
    }

    private lateinit var _root: HextantFile<Editor<R>>
    private lateinit var path: String
    private val _result = reactiveVariable<CompileResult<File<R>>>(childErr())
    val fileName = name.moveTo(context)
    private var obs: Observer? = null
    private lateinit var subscription: Subscription

    val root get() = _root

    private fun bindResult(e: Editor<R>) {
        obs?.kill()
        obs = _result.bind(result2(fileName, e) { name, content -> ok(File(name, content)) })
    }

    init {
        child(fileName)
    }

    fun dispose() {
        obs?.kill()
        subscription.cancel()
    }

    override fun deserialize(input: Input, context: SerialContext) {
        val file = input.readString()
        input.readInplace(fileName)
        val serial = this.context[SerialProperties.serial]
        val pr = this.context[SerialProperties.projectRoot]
        val editorInput = serial.createInput(pr.resolve(path), context)
        val editor = editorInput.readTyped<Editor<R>>()
        initialize(file, editor)
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeString(path)
        output.writeUntyped(fileName)
        val serial = this.context[SerialProperties.serial]
        val pr = this.context[SerialProperties.projectRoot]
        val editorOutput = serial.createOutput(pr.resolve(path), context)
        editorOutput.writeObject(root.get())
    }

    override val result: EditorResult<File<R>> get() = _result
}