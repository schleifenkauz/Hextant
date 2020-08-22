/**
 * @author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.blocky.editor.IdEditor
import hextant.blocky.editor.RefEditor
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createView
import hextant.core.view.EditorControlWrapper
import hextant.core.view.TokenEditorControl

/*
* view { e: IdEditor, args ->
        TokenEditorControl(e, args).apply {
            root.styleClass.add("id")
        }
    }
    view { e: RefEditor, args ->
        val v = e.context.createView(e.id)
        EditorControlWrapper(e, v, args)
    }
    view { e: IntLiteralEditor, args ->
        TokenEditorControl(e, args).apply {
            root.styleClass.add("int-literal")
        }
    }
    view { e: UnaryOperatorEditor, args ->
        TokenEditorControl(e, args).apply {
            root.styleClass.add("operator")
        }
    }
    view { e: BinaryOperatorEditor, args ->
        TokenEditorControl(e, args).apply {
            root.styleClass.add("operator")
        }
    }
* */

@ProvideImplementation(ControlFactory::class, IdEditor::class)
fun createControl(e: IdEditor, args: Bundle) = TokenEditorControl(e, args, styleClass = "id")

@ProvideImplementation(ControlFactory::class, RefEditor::class)
fun createControl(e: RefEditor, args: Bundle) = EditorControlWrapper(e, e.context.createView(e.id), args)