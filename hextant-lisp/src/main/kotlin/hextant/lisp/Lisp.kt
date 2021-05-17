package hextant.lisp

import hextant.fx.getUserInput
import hextant.lisp.editor.*
import hextant.lisp.rt.LispRuntimeException
import hextant.lisp.rt.RuntimeScope
import hextant.lisp.rt.evaluate
import hextant.lisp.rt.reduce
import hextant.plugins.PluginInitializer
import hextant.plugins.registerCommand
import hextant.plugins.stylesheet
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import reaktive.value.now

object Lisp : PluginInitializer({
    stylesheet("hextant/lisp/style.css")
    registerCommand<SExprExpander, Unit> {
        name = "Evaluate"
        shortName = "eval"
        defaultShortcut("Ctrl?+Shift+E")
        applicableIf { p -> p.result.now != null }
        executing { e, _ ->
            val expr = e.result.now!!
            val scope = RuntimeScope.root { scope, name ->
                val editor = SExprExpander(e.context)
                getUserInput("Specify value for $name", editor)?.evaluate(scope)
            }
            try {
                val v = expr.evaluate(scope)
                e.reconstruct(v)
            } catch (ex: LispRuntimeException) {
                Alert(AlertType.ERROR, ex.message).show()
                ex.printStackTrace()
            }
        }
    }
    registerCommand<SExprExpander, Unit> {
        name = "Reduce"
        shortName = "reduce"
        applicableIf { p -> p.result.now != null }
        defaultShortcut("Ctrl?+E")
        executing { e, _ ->
            val expr = e.result.now!!
            try {
                val scope = RuntimeScope.root()
                val reduced = expr.reduce(scope)
                e.reconstruct(reduced)
            } catch (ex: LispRuntimeException) {
                Alert(AlertType.ERROR, ex.message).show()
                ex.printStackTrace()
            }
        }
    }
    registerCommand(beautify)
    addSpecialSyntax(LetSyntax)
    addSpecialSyntax(LambdaSyntax)
})