/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.view.EditorControl
import hextant.fx.Glyphs
import hextant.fx.fontSize
import hextant.project.editor.DirectoryEditor
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import org.controlsfx.glyphfont.FontAwesome.Glyph.FOLDER

/**
 * Displays a [DirectoryEditor] as horizontal combination of the folder glyph and the name of the directory.
 */
class DirectoryEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    editor: DirectoryEditor<*>,
    arguments: Bundle
) : EditorControl<HBox>(editor, arguments) {
    private val directoryName = context.createControl(editor.itemName)

    init {
        setChildren(directoryName)
    }

    override fun receiveFocus() {
        directoryName.receiveFocus()
    }

    override fun createDefaultRoot(): HBox =
        HBox(5.0, Glyphs.create(FOLDER).fontSize(16.0).color(Color.SLATEGRAY), directoryName)
}