/**
 * @author Nikolaus Knop
 */

package hextant.expr.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.completion.CompletionStrategy
import hextant.completion.CompoundCompleter
import hextant.context.ControlFactory
import hextant.context.Properties.localCommandLine
import hextant.context.SelectionDistributor
import hextant.core.Editor
import hextant.core.view.CompoundEditorControl
import hextant.core.view.ExpanderControl
import hextant.core.view.ListEditorControl
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.core.view.TokenEditorControl
import hextant.expr.editor.*
import hextant.fx.registerShortcuts
import org.controlsfx.glyphfont.FontAwesome
import reaktive.value.now

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ExprListEditor, arguments: Bundle) =
    ListEditorControl.withAltGlyph(editor, FontAwesome.Glyph.PLUS, arguments, Horizontal).apply {
        cellFactory = { ListEditorControl.SeparatorCell(", ") }
    }

@ProvideImplementation(ControlFactory::class)
fun createControl(e: ExprExpander, args: Bundle): ExpanderControl {
    val c = CompoundCompleter<Editor<*>, Any>()
    c.addCompleter(ExprExpander.config.completer(CompletionStrategy.simple))
    //    c.addCompleter(SpecialNumbers)
    return ExpanderControl(e, args, c)
}

@ProvideImplementation(ControlFactory::class)
fun createControl(e: OperatorEditor, arguments: Bundle) = TokenEditorControl(e, arguments, styleClass = "operator")

@ProvideImplementation(ControlFactory::class)
fun createControl(e: IntLiteralEditor, arguments: Bundle) =
    TokenEditorControl(e, arguments, styleClass = "int-literal")

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ExpressionEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    view(editor.root)
    val cl = view(editor.context[localCommandLine])
    registerShortcuts {
        on("Ctrl+K") {
            cl.receiveFocus()
        }
        on("Ctrl+I") {
            val selected = editor.context[SelectionDistributor].focusedView.now
            selected?.focus()
        }
    }
    setPrefSize(500.0, 500.0)
}