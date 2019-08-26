import hextant.plugin.dsl.plugin
import hextant.sample.ast.editor.CompEditor

plugin {
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
}