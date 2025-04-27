/**
 *@author Nikolaus Knop
 */

package hextant.fx

import fxutils.runFXWithTimeout
import fxutils.shortcut
import javafx.scene.control.Skin
import javafx.scene.control.TextField
import javafx.scene.control.skin.TextFieldSkin
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyEvent
import javafx.scene.text.Font
import javafx.scene.text.Text
import reaktive.event.event

/**
 * A Text field that adds the "hextant-text" style class and does automatically resize its width
 */
open class HextantTextField(
    text: String? = "",
    autoSize: Boolean = true
) : TextField(text) {
    var isAutoSize: Boolean = autoSize
        set(value) {
            field = value
            if (isAutoSize) updateWidth(text)
            else prefWidth = USE_COMPUTED_SIZE
        }

    private var fixSelection = false

    private var onCut: () -> Unit = { super.cut() }
    private var onCopy: () -> Unit = { super.copy() }
    private var onPaste: () -> Unit = { super.paste() }

    fun setOnCut(callback: () -> Unit) {
        onCut = callback
    }

    fun setOnCopy(callback: () -> Unit) {
        onCopy = callback
    }

    fun setOnPaste(callback: () -> Unit) {
        onPaste = callback
    }

    override fun createDefaultSkin(): Skin<*> = HextantTextFieldSkin()

    private val userUpdatesText = event<String>()

    /**
     * This event stream emits strings when the **user** changes the text by typing.
     */
    val userUpdatedText = userUpdatesText.stream

    override fun paste() {
        onPaste()
    }

    override fun copy() {
        onCopy()
    }

    override fun cut() {
        onCut()
    }

    override fun end() {
        //shortcut Ctrl+RIGHT is needed for ListEditorControl
    }

    override fun selectRange(p0: Int, p1: Int) {
        if (fixSelection) {
            println("SELECTRANGE on TextField with text=$text")
            return
        }
        super.selectRange(p0, p1)
    }

    override fun home() {
        println("HOME on TextField with text=$text")
        //shortcut Ctrl+LEFT is needed for ListEditorControl
    }

    override fun selectEnd() {
        //shortcut Ctrl+Shift+RIGHT is needed for ListEditorControl
    }

    override fun selectHome() {
        //shortcut Ctrl+Shift+LEFT is needed for ListEditorControl
    }

    private inner class HextantTextFieldSkin : TextFieldSkin(this) {
        override fun replaceText(start: Int, end: Int, txt: String?) {
            super.replaceText(start, end, txt)
            userUpdatedText()
        }

        override fun deleteChar(previous: Boolean) {
            super.deleteChar(previous)
            userUpdatedText()
        }

        private fun userUpdatedText() {
            userUpdatesText.fire(text)
        }
    }

    private object TextUtils {
        private val helper: Text = Text().apply {
            wrappingWidth = 0.0
        }

        fun computeTextWidth(font: Font, text: String): Double {
            helper.text = text
            helper.font = font
            return helper.prefWidth(-1.0)
        }
    }

    init {
        styleClass.add(STYLE_CLASS)
        addEventFilter(KeyEvent.ANY) { ev ->
            if ("Ctrl+Left".shortcut.matches(ev) || "Ctrl+Right".shortcut.matches(ev)) {
                fixSelection = ev.eventType == KeyEvent.KEY_PRESSED
            }
        }
        addEventHandler(KeyEvent.KEY_RELEASED) { ev ->
            when {
                isEditable && shouldConsume(ev) -> {
                    ev.consume()
                }
            }
        }
        sceneProperty().addListener { _ ->
            runFXWithTimeout(100) { autoSize() }
        }
    }

    private fun autoSize() {
        textProperty().addListener { _, _, new ->
            if (isAutoSize) updateWidth(new)
        }
        if (isAutoSize) updateWidth(text)
    }

    private fun updateWidth(text: String) {
        val textWidth = TextUtils.computeTextWidth(font, text) + 2.0
        prefWidth = 0.0
        minWidth = 0.0
        maxWidth = 0.0
        prefWidth = textWidth.coerceAtLeast(10.0)
        maxWidth = prefWidth
        minWidth = prefWidth
    }

    companion object {
        private const val STYLE_CLASS = "hextant-text"

        private fun shouldConsume(ev: KeyEvent): Boolean = when {
            ev.code.isFunctionKey || ev.code.isMediaKey || ev.code.isModifierKey -> false
            ev.isControlDown || ev.isAltDown -> false
            ev.code in controlKeys -> false
            else -> true
        }

        private val controlKeys = setOf(ENTER, ESCAPE, TAB, INSERT, DELETE)
    }
}
