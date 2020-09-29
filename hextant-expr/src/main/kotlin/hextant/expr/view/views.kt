/**
 * @author Nikolaus Knop
 */

package hextant.expr.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.command.line.CommandLine
import hextant.completion.CompletionStrategy
import hextant.completion.CompoundCompleter
import hextant.context.ControlFactory
import hextant.core.Editor
import hextant.core.view.*
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.expr.editor.*
import org.controlsfx.glyphfont.FontAwesome

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ExprListEditor, arguments: Bundle) =
    ListEditorControl.withAltGlyph(editor, FontAwesome.Glyph.PLUS, arguments, Horizontal).apply {
        cellFactory = { ListEditorControl.SeparatorCell(", ") }
    }

@ProvideImplementation(ControlFactory::class)
fun createControl(e: ExprExpander, args: Bundle): ExpanderControl {
    val c = CompoundCompleter<Editor<*>, Any>()
    c.addCompleter(ExprExpander.config.completer(CompletionStrategy.simple))
    c.addCompleter(SpecialNumbers)
    return ExpanderControl(e, args, c)
}

@ProvideImplementation(ControlFactory::class)
fun createControl(e: OperatorEditor, arguments: Bundle) = TokenEditorControl(e, arguments, styleClass = "operator")

@ProvideImplementation(ControlFactory::class)
fun createControl(e: IntLiteralEditor, arguments: Bundle) =
    TokenEditorControl(e, arguments, styleClass = "decimal-editor")

@ProvideImplementation(ControlFactory::class)
fun createControl(e: ExpressionEditor, arguments: Bundle) = CompoundEditorControl(e, arguments) {
    view(e.root)
    view(e.context[CommandLine])
}