import hextant.plugin.PluginInitializer
import hextant.sample.ast.editor.CompEditor

object SamplePlugin : PluginInitializer({
    compoundView<CompEditor> { e ->
        line {
            keyword("compound")
            space()
            view(e.x)
            view(e.y)
        }
    }
})