/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.view.EditorControl
import hextant.fx.*
import hextant.project.editor.FileEditor
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import org.controlsfx.glyphfont.FontAwesome.Glyph
import org.controlsfx.glyphfont.FontAwesome.Glyph.FILE
import reaktive.Observer
import reaktive.observe
import reaktive.value.binding.orElse
import reaktive.value.now

internal class FileEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: FileEditor<*>, arguments: Bundle
) : EditorControl<HBox>(editor, arguments) {
    private val iconProvider = context[IconProvider.property<Editor<*>>()]

    private var currentGlyph = glyphBinding(editor.rootEditor)

    private fun glyphBinding(e: Editor<*>) = iconProvider.provideIcon(e).orElse(FILE)

    private var glyphObserver: Observer = currentGlyph.observe { _, _, g ->
        children[0] = createIcon(g)
    }

    private val rootObserver = editor.rootEditorChanged.observe(this) { _, e ->
        currentGlyph = glyphBinding(e)
        glyphObserver.kill()
        glyphObserver = currentGlyph.observe { _, _, g ->
            children[0] = createIcon(g)
        }
    }

    val fileName = context.createControl(editor.itemName)

    override fun receiveFocus() {
        fileName.receiveFocus()
    }

    init {
        setChildren(fileName)
        root.children.add(createIcon(currentGlyph.now))
        root.children.add(fileName)
        onAction {
            val pane = context[EditorPane]
            pane.show(editor.rootEditor)
        }
    }

    override fun createDefaultRoot(): HBox = HBox(5.0)

    companion object {
        private const val GLYPH_FONT_SIZE = 14.0
        private val GLYPH_COLOR = Color.SLATEGRAY

        private fun createIcon(g: Glyph) =
            Glyphs.create(g).fontSize(GLYPH_FONT_SIZE).color(GLYPH_COLOR)
    }
}