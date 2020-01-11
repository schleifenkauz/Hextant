/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.core.editor.*
import hextant.project.ProjectItem
import hextant.project.view.EditorPane
import hextant.serial.*
import reaktive.Observer
import reaktive.event.Subscription
import reaktive.value.now

class ProjectItemExpander<R : Any>(context: Context, initialText: String = "") :
    Expander<ProjectItem<R>, ProjectItemEditor<R, *>>(context, initialText), ProjectItemEditor<R, ProjectItem<R>> {
    private val cfg = context[config<R>()]
    private var commitChangeSubscription: Subscription? = null
    private var abortChangeSubscription: Subscription? = null
    private var renamer: Observer? = null
    override val path: ReactivePath?
        get() = editor.now?.path

    val config: ExpanderConfig<ProjectItemEditor<R, *>> = defaultConfig().extendWith(cfg.transform {
        val exp = RootExpander(it.context, cfg, it)
        fileEditor(exp)
    })

    class RootExpander<R : Any>(
        context: Context,
        config: ExpanderDelegate<Editor<R>> = context[config<R>()],
        initial: Editor<R>? = null
    ) : ConfiguredExpander<R, Editor<R>>(config, context, initial)

    private fun defaultConfig(): ExpanderConfig<ProjectItemEditor<R, *>> =
        ExpanderConfig<ProjectItemEditor<R, *>>().apply {
            registerConstant("file") { ctx ->
                val cfg = context[config<R>()]
                val obj = RootExpander(ctx, cfg, null)
                fileEditor(obj)
            }
            registerConstant("dir") { ctx -> DirectoryEditor(ctx, FileNameEditor(ctx, "_")) }
        }

    private fun fileEditor(editor: RootExpander<R>): FileEditor<R> {
        val parentPath = getProjectItemEditorParent()?.path ?: ReactivePath.empty()
        return FileEditor(context, FileNameEditor(context, "_"), editor, parentPath)
    }

    override fun onExpansion(editor: ProjectItemEditor<R, *>) {
        val name = editor.getItemNameEditor() ?: error("Unexpected project item editor")
        name.beginChange()
        commitChangeSubscription = name.commitedChange.subscribe { _, _ ->
            renamer?.kill()
            renamer = editor.renamePhysicalOnNameChange()
            if (editor is FileEditor<*>) {
                val pane = context[EditorPane]
                editor.content.write()
                pane.show(editor.content.get())
            } else if (editor is DirectoryEditor<*>) {
                context[HextantFileManager].createDirectory(editor.path.now)
            }
            cancelSubscriptions()
        }
        abortChangeSubscription = name.abortedChange.subscribe { _, _ ->
            reset()
            cancelSubscriptions()
        }
    }

    override fun onReset(editor: ProjectItemEditor<R, *>) {
        val p = editor.path ?: error("Expanded project item editor must have path")
        context[HextantFileManager].delete(p)
    }

    private fun cancelSubscriptions() {
        abortChangeSubscription?.cancel()
        commitChangeSubscription?.cancel()
        abortChangeSubscription = null
        commitChangeSubscription = null
    }

    override fun expand(text: String): ProjectItemEditor<R, *>? = config.expand(text, context)

    companion object {
        private val config = Property<ExpanderConfig<*>, Public, Public>("project item expander config")

        @Suppress("UNCHECKED_CAST")
        fun <R : Any> config() = config as Property<ExpanderConfig<out Editor<R>>, Public, Public>
    }
}