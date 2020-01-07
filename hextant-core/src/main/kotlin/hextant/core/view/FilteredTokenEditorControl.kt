/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.core.editor.FilteredTokenEditor
import hextant.fx.*
import javafx.scene.input.KeyEvent
import reaktive.event.subscribe
import reaktive.value.now

class FilteredTokenEditorControl(private val editor: FilteredTokenEditor<*>, arguments: Bundle) :
    EditorControl<HextantTextField>(editor, arguments), FilteredTokenEditorView {
    private val textSubscription = root.userUpdatedText.subscribe { new ->
        editor.setText(new)
    }

    init {
        listenForCommands()
        editor.addView(this)
    }

    override fun beginChange() {
        root.isEditable = true
        root.isFocusTraversable = true
        root.requestFocus()
        root.selectAll()
    }

    override fun displayText(text: String) {
        root.text = text
    }

    override fun endChange() {
        root.isEditable = false
        root.isFocusTraversable = false
    }

    private fun listenForCommands() {
        addEventHandler(KeyEvent.KEY_RELEASED) { ev ->
            when {
                arguments[Public, COMMIT_CHANGE].matches(ev) && editor.editable.now -> {
                    editor.commitChange()
                    ev.consume()
                }
                arguments[Public, BEGIN_CHANGE].matches(ev) && !editor.editable.now -> {
                    editor.beginChange()
                    ev.consume()
                }
                arguments[Public, ABORT_CHANGE].matches(ev) && editor.editable.now  -> {
                    editor.abortChange()
                    ev.consume()
                }
            }
        }
    }


    override fun createDefaultRoot(): HextantTextField = HextantTextField().apply {
        isEditable = false
        isFocusTraversable = false
    }

    companion object {
        val COMMIT_CHANGE = Property<Shortcut, Public, Public>("commit", default = never())

        val BEGIN_CHANGE = Property<Shortcut, Public, Public>("begin change", default = never())

        val ABORT_CHANGE = Property<Shortcut, Public, Public>("abort change", default = never())
    }
}