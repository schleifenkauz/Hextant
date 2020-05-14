/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.SimpleProperty
import hextant.core.editor.FilteredTokenEditor
import hextant.fx.*
import javafx.scene.input.KeyEvent
import reaktive.value.now

class FilteredTokenEditorControl(val editor: FilteredTokenEditor<*>, arguments: Bundle) :
    EditorControl<HextantTextField>(editor, arguments), FilteredTokenEditorView {
    private val textObserver = root.userUpdatedText.observe { _, new ->
        editor.setText(new)
    }

    init {
        listenForCommands()
        editor.addView(this)
    }

    override fun displayText(text: String) {
        root.smartSetText(text)
    }

    private fun listenForCommands() {
        addEventHandler(KeyEvent.KEY_RELEASED) { ev ->
            when {
                arguments[COMMIT_CHANGE].matches(ev) && editor.editable.now -> {
                    editor.commitChange()
                    ev.consume()
                }
                arguments[BEGIN_CHANGE].matches(ev) && !editor.editable.now -> {
                    editor.beginChange()
                    ev.consume()
                }
                arguments[ABORT_CHANGE].matches(ev) && editor.editable.now  -> {
                    editor.abortChange()
                    ev.consume()
                }
            }
        }
    }

    override fun setEditable(editable: Boolean) {
        root.isEditable = editable
        if (editable) {
            root.requestFocus()
            root.selectAll()
        }
    }

    override fun createDefaultRoot(): HextantTextField = HextantTextField()

    companion object {
        val COMMIT_CHANGE = SimpleProperty("commit", default = never())

        val BEGIN_CHANGE = SimpleProperty("begin change", default = never())

        val ABORT_CHANGE = SimpleProperty("abort change", default = never())
    }
}