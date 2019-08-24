import hextant.base.CompoundEditorControl
import hextant.plugin.dsl.plugin
import hextant.sample.ast.editor.CompEditor

plugin {
    author = "Nikolaus Knop"
    name = "Sample App"
    view { e: CompEditor, args ->
        CompoundEditorControl.build(e, args) {
            line {
                keyword("compound")
                space()
                view(e.x)
                view(e.y)
            }
        }
    }
}