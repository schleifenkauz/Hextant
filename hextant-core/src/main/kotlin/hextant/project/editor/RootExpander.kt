package hextant.project.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.NullTokenType
import kotlinx.serialization.Serializable

@Serializable
internal class RootExpander<R> : ConfiguredExpander<R, Editor<R>>() {
    override fun initialize(context: Context) {
        configure(context[ProjectItemEditor.expanderConfig()], tokenType = NullTokenType)
        super.initialize(context)
    }
}