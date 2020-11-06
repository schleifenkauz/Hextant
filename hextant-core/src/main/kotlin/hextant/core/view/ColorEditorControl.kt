/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.editor.ColorEditor
import reaktive.value.binding.map
import reaktive.value.fx.asObservableValue
import validated.orNull
import validated.reaktive.mapValidated

internal class ColorEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    editor: ColorEditor,
    arguments: Bundle
) : TokenEditorControl(editor, arguments) {
    init {
        val style = editor.result.mapValidated { c -> "-fx-text-fill: ${c.toCSSColor()}" }.map { it.orNull() }
        root.styleProperty().bind(style.asObservableValue())
    }

    private fun String.toCSSColor() = replace("0x", "#")
}