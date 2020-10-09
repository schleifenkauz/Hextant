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
import hextant.core.view.ListEditorControl.Companion.CELL_FACTORY
import hextant.core.view.ListEditorControl.Companion.ORIENTATION
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.core.view.ListEditorControl.SeparatorCell
import hextant.fx.registerShortcuts
import hextant.fx.view
import hextant.lisp.editor.*
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
            set(ORIENTATION, Horizontal)
            set(CELL_FACTORY) { SeparatorCell(" ") }
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
        on("Ctrl?+I") {
            ctx[SelectionDistributor].focusedView.now?.focus()
        }
    }
    registerShortcuts {
        on("Ctrl?+K") {
            ctx[EditorControlGroup].getViewOf(cl).receiveFocus()
        }
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: LetEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        keyword("let")
        space()
        view(editor.name)
        keyword("=")
        view(editor.value)
    }
    line {
        keyword("in")
        space()
        view(editor.body)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: LambdaEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        keyword("lambda")
        space()
        view(editor.parameters) {
            set(ORIENTATION, Horizontal)
            set(CELL_FACTORY) { SeparatorCell(" ") }
        }
        operator(" -> ")
        view(editor.body)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: NormalizedSExprEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    view(editor.expr)
    styleClass.add("normalized")
}