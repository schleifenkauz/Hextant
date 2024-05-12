package hextant.core.view

import bundles.Bundle
import bundles.createBundle
import bundles.publicProperty
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.Editor
import hextant.fx.Glyphs
import hextant.fx.registerShortcuts
import hextant.fx.withStyleClass
import javafx.scene.Node
import org.controlsfx.glyphfont.FontAwesome.Glyph.PLUS
import reaktive.value.now

class OptionalEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: hextant.core.editor.OptionalEditor<*, *>,
    arguments: Bundle = createBundle()
) : WrappingEditorControl<Node>(editor, arguments), OptionalEditorView {
    private val emptyDisplay = arguments[EMPTY_DISPLAY].invoke()

    init {
        editor.addView(this)
        val emptyDisplay = emptyDisplay
        emptyDisplay.setOnMouseClicked {
            editor.expand()
        }
        registerShortcuts {
            on("Ctrl?+R") {
                editor.reset()
            }
        }
    }

    override fun removed() {
        root = emptyDisplay
    }

    override fun display(content: Editor<*>) {
        val view = context.createControl(content)
        wrapped = view
        root = view
        view.receiveFocus()
    }

    override fun createDefaultRoot(): Node {
        val content = editor.content.now
        return if (content != null) context.createControl(content) else emptyDisplay
    }

    companion object {
        val EMPTY_DISPLAY = publicProperty<() -> Node>("empty display", ::defaultEmptyDisplay)

        fun defaultEmptyDisplay() = Glyphs.create(PLUS).withStyleClass("standard-empty-display")
    }
}