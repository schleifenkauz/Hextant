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
import reaktive.event.Subscription

class ProjectItemExpander<R : Any>(context: Context, initialText: String = "") :
    Expander<ProjectItem<R>, ProjectItemEditor<R, *>>(context, initialText), ProjectItemEditor<R, ProjectItem<R>> {
    private val cfg = context[config<R>()]
    private var commitChangeSubscription: Subscription? = null
    private var abortChangeSubscription: Subscription? = null

    val config: ExpanderConfig<ProjectItemEditor<R, *>> = defaultConfig().extendWith(cfg.transform {
        val exp = RootExpander(cfg, it.context, it)
        FileEditor(context, FileNameEditor(context, "_"), exp)
    })

    class RootExpander<R : Any>(
        config: ExpanderDelegate<Editor<R>>,
        context: Context,
        initial: Editor<R>?
    ) : ConfiguredExpander<R, Editor<R>>(config, context, initial)

    private fun defaultConfig(): ExpanderConfig<ProjectItemEditor<R, *>> =
        ExpanderConfig<ProjectItemEditor<R, *>>().apply {
            registerConstant("file") { ctx ->
                val cfg = context[config<R>()]
                val obj = RootExpander(cfg, ctx, null)
                FileEditor(context, FileNameEditor(context, "_"), obj)
            }
            registerConstant("dir") { ctx -> DirectoryEditor(ctx, FileNameEditor(ctx, "_")) }
        }

    override fun onExpansion(editor: ProjectItemEditor<R, *>) {
        val name = editor.getItemNameEditor() ?: error("Unexpected project item editor")
        name.beginChange()
        if (editor is FileEditor<*>) {
            commitChangeSubscription = name.commitedChange.subscribe { _, _ ->
                val pane = context[EditorPane]
                pane.show(editor.root.get())
                cancelSubscriptions()
            }
        }
        abortChangeSubscription = name.abortedChange.subscribe { _, _ ->
            reset()
            cancelSubscriptions()
        }
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