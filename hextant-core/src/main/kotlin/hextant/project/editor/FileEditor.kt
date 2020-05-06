/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.base.CompoundEditor
import hextant.core.editor.*
import hextant.project.File
import hextant.serial.*
import kserial.*
import reaktive.Observer
import reaktive.event.event
import reaktive.value.binding.map
import reaktive.value.reactiveVariable

class FileEditor<R : Any> private constructor(context: Context) : CompoundEditor<File<R>>(context),
                                                                  ProjectItemEditor<R, File<R>>, Serializable {
    private constructor(
        context: Context,
        editor: Editor<R>
    ) : this(context) {
        this.editor = editor
    }

    private var editor: Editor<R>? = null
    private lateinit var content: HextantFile<Editor<R>>
    private var initialized = false
    override val path: ReactivePath by lazy {
        val p = getProjectItemEditorParent()!!
        p.path!!.resolve(itemName.result.map { it.force() })
    }

    private val resultClass by lazy { getTypeArgument(FileEditor::class, 0) }

    override val itemName by child(FileNameEditor(context))

    private val _result = reactiveVariable<CompileResult<File<R>>>(childErr())

    private var obs: Observer? = null
    private lateinit var observer: Observer

    val rootEditor get() = editor ?: content.get()

    private val rootEditorChange = event<Editor<R>>()
    val rootEditorChanged get() = rootEditorChange.stream

    fun dispose() {
        obs?.kill()
        observer.kill()
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
        input.readInplace(itemName)
        val manager = this.context[HextantFileManager]
        content = manager.from(path)
        bindResult()
    }

    override fun deletePhysical() {
        if (initialized) context[HextantFileManager].deleteFile(path)
    }

    private fun bindResult() {
        updateEditor(content.get())
        observer = content.read.observe { _, e ->
            obs?.kill()
            updateEditor(e)
            rootEditorChange.fire(e)
        }
    }

    @Suppress("DEPRECATION")
    private fun updateEditor(e: Editor<R>) {
        obs = _result.bind(result2(itemName, e) { name, content -> ok(File(name, content)) })
        e.setFile(this)
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeUntyped(itemName)
        content.write()
    }

    override val result: EditorResult<File<R>> get() = _result

    internal class RootExpander<R : Any>(
        context: Context,
        config: ExpanderDelegate<Editor<R>> = context[ProjectItemEditor.expanderConfig<R>()],
        initial: Editor<R>? = null
    ) : ConfiguredExpander<R, Editor<R>>(config, context, initial)

    companion object {
        fun <R : Any> newInstance(context: Context) = FileEditor<R>(context, RootExpander(context))
    }
}