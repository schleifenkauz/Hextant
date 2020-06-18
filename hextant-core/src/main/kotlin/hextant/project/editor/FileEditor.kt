/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.base.CompoundEditor
import hextant.base.EditorSnapshot
import hextant.context.*
import hextant.core.Editor
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderDelegate
import hextant.project.File
import hextant.serial.*
import hextant.serial.SerialProperties.projectRoot
import reaktive.Observer
import reaktive.event.event
import reaktive.value.reactiveVariable
import validated.Validated
import validated.invalidComponent
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive
import java.nio.file.Path

internal class FileEditor<R> private constructor(context: Context) : CompoundEditor<File<R>>(context),
                                                                     ProjectItemEditor<R, File<R>> {
    private lateinit var id: String
    private lateinit var path: Path
    private lateinit var content: VirtualFile<Editor<R>>

    override val itemName by child(FileNameEditor(context))

    private val _result = reactiveVariable<Validated<File<R>>>(invalidComponent())

    private var obs: Observer? = null
    private lateinit var observer: Observer

    private val rootEditorChange = event<Editor<R>>()
    internal val rootEditorChanged get() = rootEditorChange.stream
    internal val rootEditor get() = content.get()


    override fun deletePhysical() {
        context[FileManager].deleteFile(path)
    }

    private fun bindResult() {
        updateEditor(content.get())
        observer = content.read.observe { _, e ->
            obs?.kill()
            updateEditor(e)
            rootEditorChange.fire(e)
        }
    }

    private class Snapshot<R>(original: FileEditor<R>) : EditorSnapshot<FileEditor<R>>(original) {
        private val id = original.id
        private val itemName = original.itemName.snapshot()

        init {
            original.content.write()
        }

        override fun reconstruct(editor: FileEditor<R>) {
            itemName.reconstruct(editor.itemName)
            editor.id = id
            editor.path = editor.context[projectRoot].resolve(id)
            editor.content = editor.context[FileManager].from(editor.path, editor.context)
            editor.bindResult()
        }
    }

    @Suppress("DEPRECATION")
    private fun updateEditor(e: Editor<R>) {
        obs = _result.bind(composeReactive(itemName.result, e.result, ::File))
        e.setFile(content)
    }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    override val result: ReactiveValidated<File<R>> get() = _result

    internal class RootExpander<R>(
        context: Context,
        config: ExpanderDelegate<Editor<R>> = context[ProjectItemEditor.expanderConfig<R>()],
        initial: Editor<R>? = null
    ) : ConfiguredExpander<R, Editor<R>>(config, context, initial)

    companion object {
        fun <R> newInstance(context: Context): FileEditor<R> {
            val e = FileEditor<R>(context)
            e.id = context[Internal, IdGenerator].generateID()
            e.path = context[projectRoot].resolve(e.id)
            e.content = context[FileManager].get(RootExpander(context), e.path)
            e.content.write()
            e.bindResult()
            return e
        }
    }
}