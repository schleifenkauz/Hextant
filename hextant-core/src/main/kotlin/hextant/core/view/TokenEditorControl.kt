/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.core.editor.TokenEditor
import hextant.fx.*
import hextant.isError
import javafx.scene.input.KeyEvent
import reaktive.event.EventHandler
import reaktive.event.Subscription

class TokenEditorControl(private val editor: TokenEditor<*, TokenEditorView>, arguments: Bundle) :
    EditorControl<HextantTextField>(editor, arguments), TokenEditorView {
    private var editable = false

    private val textHandler: EventHandler<String> = { _, txt ->
        val error = editor.compile(txt).isError
        pseudoClassStateChanged(PseudoClasses.ERROR, error)
    }

    private var textSubscription: Subscription? = null

    init {
        listenForCommands()
        editor.addView(this)
    }

    override fun displayText(newText: String) {
        root.text = newText
    }

    private fun listenForCommands() {
        addEventHandler(KeyEvent.KEY_RELEASED) { ev ->
            when {
                arguments[Public, COMMIT_CHANGE].matches(ev) && editable -> {
                    commitChange()
                    ev.consume()
                }
                arguments[Public, BEGIN_CHANGE].matches(ev) && !editable -> {
                    beginChange()
                    ev.consume()
                }
                arguments[Public, ABORT_CHANGE].matches(ev) && editable  -> {
                    abortChange()
                    ev.consume()
                }
            }
        }
    }

    fun beginChange() {
        check(!editable)
        textSubscription = root.userUpdatedText.subscribe(textHandler)
        editable = true
        root.isEditable = true
        root.isFocusTraversable = true
        root.selectAll()
    }

    fun commitChange() {
        check(editable)
        if (!editor.compile(root.text).isError) {
            editor.setText(root.text)
            makeUneditable()
        }
    }

    fun abortChange() {
        check(editable)
        makeUneditable()
    }

    private fun makeUneditable() {
        editable = false
        root.isEditable = false
        root.isFocusTraversable = false
        textSubscription?.cancel()
    }

    override fun createDefaultRoot(): HextantTextField {
        val tf = HextantTextField()
        tf.isEditable = false
        tf.isFocusTraversable = false
        return tf
    }

    companion object {
        val COMMIT_CHANGE = Property<Shortcut, Public, Public>("commit", default = never())

        val BEGIN_CHANGE = Property<Shortcut, Public, Public>("begin change", default = never())

        val ABORT_CHANGE = Property<Shortcut, Public, Public>("abort change", default = never())
    }
}