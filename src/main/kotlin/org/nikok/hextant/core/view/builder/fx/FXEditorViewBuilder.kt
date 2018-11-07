/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.view.builder.fx

import javafx.scene.Node
import javafx.scene.layout.Pane
import org.nikok.hextant.*
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.fx.*
import org.nikok.hextant.core.view.builder.EditorViewBuilder

open class FXEditorViewBuilder(
    protected val platform: HextantPlatform,
    protected val editable: Editable<*>,
    internal val pane: Pane
) : EditorViewBuilder {
    protected val editor = platform[EditorFactory].resolveEditor(editable)

    private val viewFactory = platform[EditorViewFactory]

    protected fun addChild(node: Node) {
        pane.children.add(node)
    }

    override fun keyword(name: String) {
        val tf = HextantTextField(name)
        tf.isEditable = false
        tf.styleClass.add("keyword")
        tf.initSelection(editor)
        addChild(tf)
    }

    override fun operator(op: String) {
        val tf = HextantTextField(op)
        tf.isEditable = false
        tf.styleClass.add("operator")
        tf.initSelection(editor)
        addChild(tf)
    }

    override fun view(editable: Editable<*>) {
        val view = viewFactory.getFXView(editable)
        addChild(view.node)
    }

    internal fun build() = object : FXEditorView {
        override val node: Node = pane

        init {
            node.initSelection(editor)
            activateSelectionExtension(editor)
            node.activateInspections(editable, platform)
            node.activateContextMenu(editor, platform)
            if (editor !is AbstractEditor<*, *>) {
                throw IllegalArgumentException("The editor returned for $editable must be an AbstractEditor")
            }
            @Suppress("UNCHECKED_CAST")
            editor as AbstractEditor<*, EditorView>
            try {
                editor.addView(this)
            } catch (e: ClassCastException) {
                throw IllegalArgumentException("The editor returned for $editor, must accept simple EditorViews")
            }
        }
    }
}