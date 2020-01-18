/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.base.CompoundEditor
import hextant.project.File
import hextant.serial.*
import kserial.*
import reaktive.Observer
import reaktive.event.Subscription
import reaktive.event.event
import reaktive.value.binding.map
import reaktive.value.reactiveVariable

class FileEditor<R : Any> private constructor(
    context: Context,
    name: FileNameEditor
) : CompoundEditor<File<R>>(context), ProjectItemEditor<R, File<R>>, Serializable {
    private constructor(context: Context) : this(context, FileNameEditor(context, ""))

    constructor(
        context: Context,
        name: FileNameEditor,
        editor: Editor<R>
    ) : this(context, name) {
        this.editor = editor
    }

    private var editor: Editor<R>? = null
    private lateinit var content: HextantFile<Editor<R>>
    private var initialized = false
    override val path: ReactivePath by lazy {
        val p = getProjectItemEditorParent()!!
        p.path!!.resolve(fileName.result.map { it.force() })
    }

    val fileName by child(name, context)

    private val _result = reactiveVariable<CompileResult<File<R>>>(childErr())

    private var obs: Observer? = null
    private lateinit var subscription: Subscription

    val rootEditor get() = editor ?: content.get()

    private val rootEditorChange = event<Editor<R>>()
    val rootEditorChanged get() = rootEditorChange.stream

    fun dispose() {
        obs?.kill()
        subscription.cancel()
    }

    fun initialize() {
        check(!initialized)
        initialized = true
        val e = editor ?: error("Editor must be present")
        editor = null
        val m = context[HextantFileManager]
        content = m.get(e, path)
        content.write()
        bindResult()
    }

    override fun deserialize(input: Input, context: SerialContext) {
        input.readInplace(fileName)
        val manager = this.context[HextantFileManager]
        content = manager.from(path)
        bindResult()
    }

    override fun deletePhysical() {
        if (initialized) context[HextantFileManager].deleteFile(path)
    }

    private fun bindResult() {
        updateEditor(content.get())
        subscription = content.read.subscribe { _, e ->
            obs?.kill()
            updateEditor(e)
            rootEditorChange.fire(e)
        }
    }

    @Suppress("DEPRECATION")
    private fun updateEditor(e: Editor<R>) {
        obs = _result.bind(result2(fileName, e) { name, content -> ok(File(name, content)) })
        e.setFile(this)
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeUntyped(fileName)
        content.write()
    }

    override fun copyForImpl(context: Context): FileEditor<R> =
        FileEditor(context, fileName, content.get().copy())

    override val result: EditorResult<File<R>> get() = _result
}