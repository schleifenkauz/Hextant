/**
 * @author Nikolaus Knop
 */

package hextant.expr.view

import bundles.Bundle
import fxutils.registerShortcuts
import fxutils.styleClass
import hextant.completion.CompletionStrategy
import hextant.completion.CompoundCompleter
import hextant.context.Properties
import hextant.context.SelectionDistributor
import hextant.core.editor.Expander
import hextant.core.view.CompoundEditorControl
import hextant.core.view.ExpanderControl
import hextant.core.view.ListEditorControl
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.core.view.TokenEditorControl
import hextant.expr.editor.*
import hextant.plugins.PluginBuilder
import hextant.plugins.registerControlFactory
import org.controlsfx.glyphfont.FontAwesome
import reaktive.value.now

fun PluginBuilder.registerControlFactories() {
    registerControlFactory(::OperatorApplicationEditorControl)
    registerControlFactory(::SumEditorControl)

    registerControlFactory { editor: ExprListEditor, arguments ->
        ListEditorControl.withAltGlyph(editor, FontAwesome.Glyph.PLUS, arguments, Horizontal).apply {
            cellFactory = { ListEditorControl.SeparatorCell(", ") }
        }
    }
    registerControlFactory { e: ExprExpander, args: Bundle ->
        val c = CompoundCompleter<Expander<*, *>, Any>()
        c.addCompleter(ExprExpander.config.completer(CompletionStrategy.simple))
        //    c.addCompleter(SpecialNumbers)
        ExpanderControl(e, args, c)
    }
    registerControlFactory { e: OperatorEditor, args: Bundle -> TokenEditorControl(e, args, styleClass = "operator") }
    registerControlFactory { e: IntLiteralEditor, args: Bundle ->
        TokenEditorControl(e, args, styleClass = "int-literal")
    }
    registerControlFactory { e: ExpressionEditor, args: Bundle ->
        CompoundEditorControl(e, args) {
            styleClass("expression-root")
            vertical {
                view(e.root)
                val cl = view(e.context[Properties.localCommandLine])
                root.registerShortcuts {
                    on("Ctrl+K") {
                        cl.receiveFocus()
                    }
                    on("Ctrl+I") {
                        val selected = e.context[SelectionDistributor].focusedView.now
                        selected?.focus()
                    }
                }
                root.setPrefSize(500.0, 500.0)
            }
        }
    }
}