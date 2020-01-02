/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.core.editor.*
import hextant.project.ProjectItem
import hextant.serial.*
import java.nio.file.Paths

class ProjectItemExpander<R : Any>(context: Context) :
    Expander<ProjectItem<R>, ProjectItemEditor<R, *>>(context), ProjectItemEditor<R, ProjectItem<R>> {

    private val cfg = context[config<R>()]

    private val id = Any()

    val config: ExpanderConfig<ProjectItemEditor<R, *>> = defaultConfig().extendWith(cfg.transform {
        val exp = RootExpander(cfg, it.context, it, id)
        FileEditor(context, exp.file())
    })

    class RootExpander<R : Any>(
        config: ExpanderDelegate<Editor<R>>,
        context: Context,
        initial: Editor<R>?,
        private val id: Any
    ) : ConfiguredExpander<R, Editor<R>>(config, context, initial), RootEditor<R> {
        override fun file(): HextantFile<RootExpander<R>> =
            context[HextantFileManager].get(Paths.get(id.toString()), this)
    }

    private fun defaultConfig(): ExpanderConfig<ProjectItemEditor<R, *>> =
        ExpanderConfig<ProjectItemEditor<R, *>>().apply {
            registerConstant("file") { ctx ->
                val cfg = context[config<R>()]
                val obj = RootExpander(cfg, ctx, null, id)
                FileEditor(context, obj.file())
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