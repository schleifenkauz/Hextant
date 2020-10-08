/**
 * @author Nikolaus Knop
 */

package hextant.lisp.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.command.line.CommandLine
import hextant.completion.CompletionStrategy
import hextant.context.*
import hextant.core.view.*
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.core.view.ListEditorControl.SeparatorCell
import hextant.fx.*
import hextant.lisp.editor.*
import javafx.scene.paint.Color
import reaktive.value.now

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: SymbolEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, styleClass = "symbol")

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: IntLiteralEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, styleClass = "int-literal")

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: BooleanLiteralEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, styleClass = "boolean-literal")

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: QuotationEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        operator("'")
        view(editor.quoted)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: SExprExpander, arguments: Bundle) =
    ExpanderControl(editor, arguments, SExprExpanderConfigurator.config.completer(CompletionStrategy.simple))

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: QuasiQuotationEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        operator("`")
        view(editor.quoted)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: UnquoteEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        operator(",")
        view(editor.expr)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: CallExprEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        operator("(")
        view(editor.expressions) {
            set(ListEditorControl.ORIENTATION, Horizontal)
            set(ListEditorControl.CELL_FACTORY) { SeparatorCell(" ") }
        }
        operator(")")
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: LispProject, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    val ctx = editor.context
    view(editor.root)
    val cl = ctx[CommandLine]
    view(cl).registerShortcuts {
        on("Ctrl+I") {
            ctx[SelectionDistributor].focusedView.now?.focus()
        }
    }
    registerShortcuts {
        on("ESCAPE") {
            ctx[EditorControlGroup].getViewOf(cl).receiveFocus()
        }
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: NormalizedSExprEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    view(editor.wrapped)
    setBackground(Color.GREEN)
}