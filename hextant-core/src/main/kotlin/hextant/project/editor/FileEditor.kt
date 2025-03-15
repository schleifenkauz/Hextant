/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.core.editor.CompoundEditor
import hextant.core.editor.composeResult
import hextant.project.File
import hextant.serial.IdGenerator
import hextant.serial.SerialProperties.projectRoot
import reaktive.Observer
import reaktive.event.event
import reaktive.value.ReactiveValue
import reaktive.value.reactiveVariable

internal class FileEditor<R> private constructor() : CompoundEditor<File<R>?>(),
                                                                     ProjectItemEditor<R, File<R>> {
    private lateinit var id: String
    private lateinit var path: java.io.File
    private lateinit var content: Editor<R>

    override val itemName by child(FileNameEditor())

    private val _result = reactiveVariable<File<R>?>(null)

    private var observer: Observer? = null

    private val rootEditorChange = event<Editor<R>>()
    internal val rootEditorChanged get() = rootEditorChange.stream
    internal val rootEditor get() = content

    private fun updateEditor(e: Editor<R>) {
        observer = _result.bind(composeResult<File<R>?>(itemName, e, default = { null }) { File(itemName.now, e.now) })
    }

    override fun supportsCopyPaste(): Boolean = true

    override val result: ReactiveValue<File<R>?> get() = _result

    companion object {
        fun <R> newInstance(context: Context): FileEditor<R> {
            val e = FileEditor<R>()
            e.initialize(context)
            e.id = context[IdGenerator].generateID()
            e.path = context[projectRoot].resolve(e.id)
            e.content = RootExpander()
            e.updateEditor(e.content)
            return e
        }
    }
}