/**
 * @author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.view.EditorControlWrapper
import hextant.core.view.TokenEditorControl

@ProvideImplementation(ControlFactory::class)
fun createControl(e: hextant.blocky.editor.IdEditor, arguments: Bundle) = TokenEditorControl(e, arguments, styleClass = "id")

@ProvideImplementation(ControlFactory::class)
fun createControl(e: hextant.blocky.editor.RefEditor, arguments: Bundle) = EditorControlWrapper(e, e.context.createControl(e.id), arguments)

@ProvideImplementation(ControlFactory::class)
fun createControl(e: hextant.blocky.editor.IntLiteralEditor, arguments: Bundle) = TokenEditorControl(e, arguments, styleClass = "int-literal")

@ProvideImplementation(ControlFactory::class)
fun createControl(e: hextant.blocky.editor.UnaryOperatorEditor, arguments: Bundle) = TokenEditorControl(e, arguments, styleClass = "operator")

@ProvideImplementation(ControlFactory::class)
fun createControl(e: hextant.blocky.editor.BinaryOperatorEditor, arguments: Bundle) =
    TokenEditorControl(e, arguments, styleClass = "operator")
