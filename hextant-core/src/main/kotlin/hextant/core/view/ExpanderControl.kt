/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.completion.Completion
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
    private val ctx: Context,
    args: Bundle
) : ExpanderView,
    EditorControl<Node>(args) {
    override fun textChanged(newText: String) {}

    private val expander: Expander<*, *> = ctx.getEditor(expandable)

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

    override fun suggestCompletions(completions: Set<Completion<String>>) {
        TODO("not implemented")
    }

    override fun receiveFocus() {
        root.requestFocus()
    }
}