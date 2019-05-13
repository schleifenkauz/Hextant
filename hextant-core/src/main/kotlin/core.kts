import hextant.core.inspect.SyntaxErrorInspection
import hextant.core.view.FXExpanderView
import hextant.plugin.dsl.plugin

plugin {
    name = "Hextant Core"
    author = "Nikolaus Knop"
    view(::FXExpanderView)
    inspection(::SyntaxErrorInspection)
    stylesheet("hextant/core/style.css")
}