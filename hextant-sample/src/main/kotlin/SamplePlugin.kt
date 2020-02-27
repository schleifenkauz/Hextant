import hextant.plugin.dsl.PluginInitializer
import hextant.sample.ast.editor.CompEditor

object SamplePlugin : PluginInitializer({
    author = "Nikolaus Knop"
    name = "Sample App"
    compoundView<CompEditor> { e ->
        line {
            keyword("compound")
            space()
            view(e.x)
            view(e.y)
        }
    }
})