package hextant.project.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderDelegate

internal class RootExpander<R : Any>(
    context: Context,
    config: ExpanderDelegate<Editor<R>> = context[ProjectItemEditor.expanderConfig<R>()],
    initial: Editor<R>? = null
) : ConfiguredExpander<R, Editor<R>>(config, context, initial)