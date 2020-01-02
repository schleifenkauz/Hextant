/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.fx.Glyphs
import hextant.fx.onAction
import hextant.project.editor.FileEditor
import hextant.serial.RootEditor
import javafx.scene.layout.HBox
import org.controlsfx.glyphfont.FontAwesome.Glyph.FILE
import reaktive.Observer
import reaktive.value.binding.orElse
import reaktive.value.now

class FileEditorControl(private val editor: FileEditor<*>, arguments: Bundle) : EditorControl<HBox>(editor, arguments) {
    private val iconProvider = context[IconProvider.property<Editor<*>>()]

    private var currentGlyph = glyphBinding(editor.root.get())

    private fun glyphBinding(e: RootEditor<*>) = iconProvider.provideIcon(e).orElse(FILE)

    private var glyphObserver: Observer = currentGlyph.observe { _, _, g ->
        children[0] = Glyphs.create(g)
    }

    private val rootObserver = editor.root.read.subscribe { _, e ->
        currentGlyph = glyphBinding(e)
        glyphObserver.kill()
        glyphObserver = currentGlyph.observe { _, _, g ->
            children[0] = Glyphs.create(g)
        }
    }

    val fileName = context.createView(editor.name)

    override fun receiveFocus() {
        fileName.receiveFocus()
    }

    init {
        setChildren(fileName)
        root.children.add(Glyphs.create(currentGlyph.now))
        root.children.add(fileName)
        onAction {
            val pane = context[Public, EditorPane]
            pane.show(editor.root.get())
        }
    }

    override fun createDefaultRoot(): HBox = HBox()
}