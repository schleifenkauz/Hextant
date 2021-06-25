package hextant.lisp

import bundles.createBundle
import bundles.set
import hextant.command.line.CommandLine
import hextant.command.line.CommandReceiverType.*
import hextant.command.line.ContextCommandSource
import hextant.context.EditorControlGroup
import hextant.context.SelectionDistributor
import hextant.context.extend
import hextant.context.withoutUndo
import hextant.core.editor.copyFor
import hextant.fx.WindowSize
import hextant.fx.getUserInput
import hextant.fx.showDialog
import hextant.fx.showStage
import hextant.lisp.ctx.EditingContext
import hextant.lisp.editor.*
import hextant.lisp.rt.*
import hextant.lisp.view.createViewWithCommandLine
import hextant.plugins.*
import hextant.serial.makeRoot
import hextant.undo.UndoManager
import hextant.undo.compoundEdit
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import reaktive.value.binding.map
import reaktive.value.now

object Lisp : PluginInitializer({
    stylesheet("hextant/lisp/style.css")
    registerCommand<SExprExpander, Unit> {
        name = "Collapse"
        shortName = "collapse"
        description = "Substitutes the result of evaluating the expression fully"
        applicableIf { p -> !p.isNormalized && p.result.now.isSyntacticallyCorrect() }
        executing { e, _ ->
            val expr = e.result.now
            val scope = RuntimeScope.root { scope, name ->
                val editor = SExprExpander(e.context)
                getUserInput("Specify value for $name", editor)?.evaluate(scope)
            }
            tryWithExceptionAlert {
                val v = expr.evaluate(scope)
                e.context.compoundEdit("substitute result") {
                    e.reconstruct(v)
                    if (!e.context[EditingContext].canReduceNormalized) e.isNormalized = true
                }
            }
        }
    }
    registerCommand<SExprExpander, Unit> {
        name = "Evaluate"
        shortName = "eval"
        description = "Shows the result of evaluating the expression fully"
        applicableIf { p -> !p.isNormalized && p.result.now.isSyntacticallyCorrect() }
        executing { e, _ ->
            val expr = e.result.now
            val scope = RuntimeScope.root { scope, name ->
                val editor = SExprExpander(e.context)
                getUserInput("Specify value for $name", editor)?.evaluate(scope)
            }
            tryWithExceptionAlert {
                val v = expr.evaluate(scope)
                Alert(AlertType.INFORMATION, display(v)).showDialog()
            }
        }
    }
    registerCommand<SExprExpander, Unit> {
        name = "Reduce"
        shortName = "reduce"
        description = "Reduces the expression and substitutes the result"
        applicableIf { p -> p.result.now.isSyntacticallyCorrect() }
        executing { e, _ ->
            val expr = e.result.now
            tryWithExceptionAlert {
                val scope = RuntimeScope.root()
                val reduced = expr.reduce(scope)
                e.reconstruct(reduced)
            }
        }
    }
    registerCommand<SExprExpander, Unit> {
        name = "Denormalize"
        shortName = "denormalize"
        description = "Can be used to make it possible to evaluate expressions, " +
                "even if they have been fully evaluated, already." +
                "Example: '(1 + 2) -> (1 + 2) -> 3"
        applicableIf { p -> p.isNormalized }
        executing { e, _ ->
            e.isNormalized = false
        }
    }
    registerCommand<SExprExpander, Unit> {
        name = "Reduce-Edit-Print-Loop"
        shortName = "repl"
        description = "Opens a sub-window where the selected expression can be manipulated."
        executing { e, _ ->
            val editor = e.copyFor(e.context.extend {
                set(EditingContext, EditingContext.REPL)
                set(SelectionDistributor, SelectionDistributor.newInstance())
                set(UndoManager, UndoManager.newInstance().apply { isActive = false })
            })
            val ctx = editor.context
            ctx[UndoManager].isActive = true
            editor.makeRoot()
            val commandLine = CommandLine.create(ctx, ContextCommandSource(ctx, Targets, Expanders, Views))
            val view = createViewWithCommandLine(editor, createBundle(), commandLine)
            showStage(view, editor.context, applyStyle = true).apply {
                title = "REPL"
                width = 500.0
                height = 500.0
            }
            ctx[EditorControlGroup].getViewOf(commandLine).receiveFocus()
        }
    }
    registerInspection<SExprExpander> {
        id = "illegal-scalar"
        description = "Reports scalars that do not match the lexer rules"
        isSevere(true)
        preventingThat { inspected.result.map { it is IllegalScalar } }
        message { "${inspected.result.now}" }
    }
    registerInspection<SymbolEditor> {
        id = "illegal-symbol"
        description = "Reports symbols that do not match the lexer rules"
        isSevere(true)
        preventingThat { inspected.result.map { Symbol.validate(it.name) != "ok" } }
        message { Symbol.validate(inspected.result.now.name) }
    }
    registerCommand(beautify)
    addSpecialSyntax(LetSyntax)
    addSpecialSyntax(LambdaSyntax)
    resultStyleClass<IntLiteral> { "int-literal" }
    resultStyleClass<BooleanLiteral> { "boolean-literal" }
    resultStyleClass<Symbol> { "symbol" }
    resultStyleClass<Quotation> { q -> "scalar-quote".takeIf { q.quoted is Scalar } }
    resultStyleClass<Unquote> { q -> "scalar-unquote".takeIf { q.expr is Scalar } }
    set(WindowSize, WindowSize.Configured(500.0, 500.0))
})

