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
import reaktive.value.reactiveVariable

class FileEditor<R : Any> private constructor(
    context: Context,
    name: FileNameEditor,
    editor: Editor<R>,
    private val file: String
) : AbstractEditor<File<R>, EditorView>(context), ProjectItemEditor<R, File<R>> {
    constructor(context: Context, name: FileNameEditor, editor: Editor<R>) :
            this(context, name, editor, context[PathGenerator].genFile())

    val root = context[HextantFileManager].get(context[SerialProperties.projectRoot].resolve(file), editor)
    private val _result = reactiveVariable<CompileResult<File<R>>>(childErr())
    val fileName = name.moveTo(context)
    private var obs: Observer? = null
    private val subscription = root.read.subscribe { _, e -> bindResult(e) }

    private fun bindResult(e: Editor<R>) {
        obs?.kill()
        obs = _result.bind(result2(fileName, e) { name, content -> ok(File(name, content)) })
    }

    init {
        child(fileName)
        bindResult(editor)
    }

    fun dispose() {
        obs?.kill()
        subscription.cancel()
    }

    override val result: EditorResult<File<R>> get() = _result

    companion object Ser : Serializer<FileEditor<*>> {
        override fun deserialize(cls: Class<FileEditor<*>>, input: Input, context: SerialContext): FileEditor<*> {
            check(context is HextantSerialContext)
            val ctx = context.context
            val file = input.readString()
            val name = input.readTyped<FileNameEditor>()
            val serial = ctx[SerialProperties.serial]
            val editorInput = serial.createInput(ctx[SerialProperties.projectRoot].resolve(file), context)
            val editor = editorInput.readTyped<Editor<*>>()
            return FileEditor(ctx, name, editor, file)
        }

        override fun serialize(obj: FileEditor<*>, output: Output, context: SerialContext) {
            check(context is HextantSerialContext)
            output.writeString(obj.file)
            output.writeObject(obj.fileName)
            val serial = context.context[SerialProperties.serial]
            val root = context.context[SerialProperties.projectRoot]
            val editorOutput = serial.createOutput(root.resolve(obj.file), context)
            editorOutput.writeObject(obj.root.get())
        }
    }
}