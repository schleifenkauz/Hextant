/**
 * @author Nikolaus Knop
 */

package hextant.lisp.view

import bundles.Bundle
import bundles.set
import hextant.codegen.ProvideImplementation
import hextant.command.line.CommandLine
import hextant.completion.CompletionStrategy
import hextant.context.ControlFactory
import hextant.context.EditorControlGroup
import hextant.context.Properties.localCommandLine
import hextant.context.SelectionDistributor
import hextant.core.view.CompoundEditorControl
import hextant.core.view.ExpanderControl
import hextant.core.view.ListEditorControl.Companion.CELL_FACTORY
import hextant.core.view.ListEditorControl.Companion.ORIENTATION
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.core.view.ListEditorControl.SeparatorCell
import hextant.core.view.TokenEditorControl
import hextant.fx.registerShortcuts
import hextant.fx.view
import hextant.lisp.editor.SExprExpander
import reaktive.value.now

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: hextant.lisp.editor.SymbolEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, styleClass = "symbol")

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: hextant.lisp.editor.QuotationEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    horizontal {
        operator("'")
        view(editor.quoted)
        styleClass.add("quote")
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: hextant.lisp.editor.SExprExpander, arguments: Bundle) =
    ExpanderControl(editor, arguments, SExprExpander.config.completer(CompletionStrategy.simple))

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: hextant.lisp.editor.QuasiQuotationEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    horizontal {
        operator("`")
        view(editor.quoted)
        styleClass("quasiquote")
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: hextant.lisp.editor.UnquoteEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    horizontal {
        operator(",")
        view(editor.expr)
        styleClass("unquote")
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: hextant.lisp.editor.CallExprEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    horizontal {
        operator("(")
        view(editor.expressions) {
            set(ORIENTATION, Horizontal)
            set(CELL_FACTORY) { SeparatorCell(" ") }
        }
        operator(")")
        styleClass("compound-expr")
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: hextant.lisp.editor.LispProject, arguments: Bundle) =
    createViewWithCommandLine(editor.root, arguments, editor.context[localCommandLine])

fun createViewWithCommandLine(root: hextant.lisp.editor.SExprExpander, arguments: Bundle, commandLine: CommandLine) =
    CompoundEditorControl(root, arguments) {
        vertical {
            val ctx = root.context
            view(root)
            view(commandLine).registerShortcuts {
                on("Ctrl?+I") {
                    ctx[SelectionDistributor].focusedView.now?.focus()
                }
            }
            this.root.registerShortcuts {
                on("Ctrl?+K") {
                    ctx[EditorControlGroup].getViewOf(commandLine).receiveFocus()
                }
            }
        }
    }

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: hextant.lisp.editor.LetEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    horizontal {
        keyword("let")
        space()
        view(editor.name)
        keyword("=")
        view(editor.value)
    }
    horizontal {
        keyword("in")
        space()
        view(editor.body)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: hextant.lisp.editor.LambdaEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    horizontal {
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
fun createControl(editor: hextant.lisp.editor.MacroInvocationEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    horizontal {
        operator("[")
        view(editor.macro)
        space()
        view(editor.arguments) {
            set(ORIENTATION, Horizontal)
            set(CELL_FACTORY) { SeparatorCell(" ") }
        }
        operator("]")
    }
}