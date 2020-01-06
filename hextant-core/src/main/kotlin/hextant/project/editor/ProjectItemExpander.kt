/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.core.editor.*
import hextant.project.ProjectItem

class ProjectItemExpander<R : Any>(context: Context, initialText: String = "") :
    Expander<ProjectItem<R>, ProjectItemEditor<R, *>>(context, initialText), ProjectItemEditor<R, ProjectItem<R>> {
    private val cfg = context[config<R>()]

    val config: ExpanderConfig<ProjectItemEditor<R, *>> = defaultConfig().extendWith(cfg.transform {
        val exp = RootExpander(cfg, it.context, it)
        FileEditor(context, FileNameEditor(context), exp)
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
                FileEditor(context, FileNameEditor(context), obj)
            }
            registerConstant("dir") { ctx -> DirectoryEditor(ctx) }
        }

    override fun expand(text: String): ProjectItemEditor<R, *>? = config.expand(text, context)

    companion object {
        private val config = Property<ExpanderConfig<*>, Public, Public>("project item expander config")

        @Suppress("UNCHECKED_CAST")
        fun <R : Any> config() = config as Property<ExpanderConfig<out Editor<R>>, Public, Public>
    }
}