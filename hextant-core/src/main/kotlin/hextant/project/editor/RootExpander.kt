package hextant.project.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderDelegate

internal class RootExpander<R>(
    context: Context,
    config: ExpanderDelegate<Editor<R>, Context> = context[ProjectItemEditor.expanderConfig()],
    initial: Editor<R>? = null
) : ConfiguredExpander<R, Editor<R>>(config, context, initial)