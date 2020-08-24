/**
 * @author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.blocky.editor.*
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.view.EditorControlWrapper
import hextant.core.view.TokenEditorControl

@ProvideImplementation(ControlFactory::class)
fun createControl(e: IdEditor, arguments: Bundle) = TokenEditorControl(e, arguments, styleClass = "id")

@ProvideImplementation(ControlFactory::class)
fun createControl(e: RefEditor, arguments: Bundle) = EditorControlWrapper(e, e.context.createControl(e.id), arguments)

@ProvideImplementation(ControlFactory::class)
fun createControl(e: IntLiteralEditor, arguments: Bundle) = TokenEditorControl(e, arguments, styleClass = "int-literal")

@ProvideImplementation(ControlFactory::class)
fun createControl(e: UnaryOperatorEditor, arguments: Bundle) = TokenEditorControl(e, arguments, styleClass = "operator")

@ProvideImplementation(ControlFactory::class)
fun createControl(e: BinaryOperatorEditor, arguments: Bundle) =
    TokenEditorControl(e, arguments, styleClass = "operator")
