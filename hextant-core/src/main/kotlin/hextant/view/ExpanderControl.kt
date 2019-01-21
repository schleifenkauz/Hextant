/**
 *@author Nikolaus Knop
 */

package hextant.view

import hextant.*
import hextant.base.EditorControl
import hextant.core.editable.Expandable
import hextant.core.editor.Expander
import hextant.fx.HextantTextField
import hextant.fx.registerShortcut
import hextant.impl.Stylesheets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.input.*
import org.controlsfx.control.PopOver
import org.controlsfx.control.PopOver.ArrowLocation.TOP_CENTER
import reaktive.value.now

class ExpanderControl(
    expandable: Expandable<*, *>,
    private val ctx: Context
) : ExpanderView,
    EditorControl<Node>() {
    override fun textChanged(newText: String) {}

    private val expander = ctx.getEditor(expandable) as Expander<*, *>

    private val popup = createPopOver(expandable)

    private fun createPopOver(expandable: Expandable<*, *>) =
        PopOver().apply {
            styleClass.add("expander-popover")
            val tf = HextantTextField()
            isDetachable = false
            arrowLocation = TOP_CENTER
            isHeaderAlwaysVisible = false
            contentNode = tf
            isAutoHide = true
            Stylesheets.apply(root.stylesheets)
            tf.setOnAction {
                if (expandable.isExpanded.now) expander.reset()
                expander.setText(tf.text)
                expander.expand()
                hide()
            }
        }

    init {
        registerShortcut(KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN)) {
            popup.show(this)
        }
        initialize(expandable, expander, ctx)
        expander.addView(this)
    }

    override fun reset() {
        root = createDefaultRoot()
    }

    override fun expanded(newContent: Editable<*>) {
        root = ctx.createView(newContent)
    }

    override fun createDefaultRoot(): Node = Button("?")
}