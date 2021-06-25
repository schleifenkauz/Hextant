package hextant.lisp

import hextant.fx.getUserInput
import hextant.lisp.editor.*
import hextant.lisp.rt.*
import hextant.plugins.*
import hextant.undo.compoundEdit
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import reaktive.value.binding.map
import reaktive.value.now

object Lisp : PluginInitializer({
    stylesheet("hextant/lisp/style.css")
    registerCommand<SExprExpander, Unit> {
        name = "Evaluate"
        shortName = "eval"
        defaultShortcut("Ctrl?+Shift+E")
        applicableIf { p -> p.result.now.isSyntacticallyCorrect() }
        executing { e, _ ->
            val expr = e.result.now
            val scope = RuntimeScope.root { scope, name ->
                val editor = SExprExpander(e.context)
                getUserInput("Specify value for $name", editor)?.evaluate(scope)
            }
            try {
                val v = expr.evaluate(scope)
                println(display(v))
                e.context.compoundEdit("subsitute result") {
                    e.reconstruct(v)
                }
            } catch (ex: LispRuntimeException) {
                Alert(AlertType.ERROR, ex.message).show()
                ex.printStackTrace()
            }
        }
    }
    registerCommand<SExprExpander, Unit> {
        name = "Reduce"
        shortName = "reduce"
        applicableIf { p -> p.result.now.isSyntacticallyCorrect() }
        defaultShortcut("Ctrl?+E")
        executing { e, _ ->
            val expr = e.result.now
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
    registerInspection<ScalarEditor> {
        id = "illegal-symbol"
        description = "Reports symbols that do not match the lexer rules"
        isSevere(true)
        preventingThat { inspected.result.map { it is IllegalScalar } }
        message { "${inspected.result.now}" }
    }
    registerCommand(beautify)
    addSpecialSyntax(LetSyntax)
    addSpecialSyntax(LambdaSyntax)
    resultStyleClass<IntLiteral> { "int-literal" }
    resultStyleClass<BooleanLiteral> { "boolean-literal" }
    resultStyleClass<Symbol> { "symbol" }
})