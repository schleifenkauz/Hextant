package hextant.lisp

import hextant.context.createControl
import hextant.fx.*
import hextant.lisp.editor.*
import hextant.lisp.rt.*
import hextant.plugin.*
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import reaktive.value.binding.map
import reaktive.value.now
import reaktive.value.reactiveValue
import validated.*

object Lisp : PluginInitializer({
    stylesheet("hextant/lisp/style.css")
    registerCommand<SExprExpander, Unit> {
        name = "Evaluate"
        shortName = "eval"
        defaultShortcut("Ctrl?+Shift+E")
        applicableIf { p -> p.result.now.isValid }
        executing { e, _ ->
            val expr = e.result.now.force()
            val scope = RuntimeScope.root { scope, name ->
                val editor = SExprExpander(e.context, RuntimeScopeEditor(e.context, scope))
                val view = e.context.createControl(editor)
                val control = vbox(label("Specify value for $name: "), view)
                getUserInput(editor, control).orNull()?.evaluate(scope)
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
        applicableIf { p -> p.result.now.isValid }
        defaultShortcut("Ctrl?+E")
        executing { e, _ ->
            val expr = e.result.now.force()
            try {
                val reduced = expr.reduce()
                e.reconstruct(reduced)
            } catch (ex: LispRuntimeException) {
                Alert(AlertType.ERROR, ex.message).show()
                ex.printStackTrace()
            }
        }
    }
    registerCommand<SExprExpander, Unit> {
        name = "Print scope"
        shortName = "scope"
        defaultShortcut("Ctrl?+H")
        executing { ex, _ ->
            println(ex.scope.scope)
        }
    }
    registerInspection<SExprEditor> {
        id = "holes"
        description = "Highlights parts of the program that are not yet filled out"
        message { "Hole in program" }
        isSevere(true)
        preventingThat {
            if (inspected is SExprExpander) reactiveValue(false)
            else inspected.result.map { it.orNull() is Hole? }
        }
    }
    registerCommand(beautify)
    addSpecialSyntax(LetSyntax)
    addSpecialSyntax(LambdaSyntax)
})