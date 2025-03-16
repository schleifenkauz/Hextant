package hextant.project.editor

import hextant.core.Editor
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.NullTokenType
import kotlinx.serialization.Serializable

@Serializable
internal class RootExpander<R> : ConfiguredExpander<R, Editor<R>>() {
    override fun doInitialize() {
        configure(context[ProjectItemEditor.expanderConfig<R>()], tokenType = NullTokenType)
    }
}