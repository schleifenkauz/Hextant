/**
 *@author Nikolaus Knop
 */

package hextant.fx

import com.sun.javafx.scene.control.skin.TextFieldSkin
import hextant.main.InputMethod
import hextant.main.InputMethod.REGULAR
import hextant.main.InputMethod.VIM
import javafx.application.Platform
import javafx.scene.control.Skin
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Region
import javafx.scene.text.Font
import javafx.scene.text.Text
import reaktive.event.event
import kotlin.concurrent.thread

/**
 * A Text field that adds the "hextant-text" style class and does automatically resize its width
 */
open class HextantTextField(
    text: String? = "",
    initialInputMethod: InputMethod = REGULAR
) : TextField(text) {
    override fun createDefaultSkin(): Skin<*> = HextantTextFieldSkin()

    private val userUpdatesText = event<String>()

    /**
     * This event stream emits strings when the **user** changes the text by typing.
     */
    val userUpdatedText = userUpdatesText.stream

    /**
     * The [InputMethod] of this [HextantTextField]
     *
     * If it is [InputMethod.VIM] than the text field is uneditable by default.
     */
    var inputMethod = initialInputMethod
        internal set(value) {
            field = value
            isEditable = value != VIM || (isEditable && isFocused)
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
        focusedProperty().addListener { _, _, focused ->
            if (!focused && inputMethod == VIM) {
                isEditable = false
            }
        }
        addEventHandler(KeyEvent.KEY_RELEASED) { ev ->
            when {
                INPUT_MODE.match(ev) && inputMethod == VIM && !isEditable  -> {
                    isEditable = true
                    ev.consume()
                }
                COMMAND_MODE.match(ev) && inputMethod == VIM && isEditable -> {
                    isEditable = false
                    ev.consume()
                }
                isEditable && shouldConsume(ev)                            -> {
                    ev.consume()
                }
            }
        }
        autoSize()
    }

    override fun requestFocus() {
        super.requestFocus()
        positionCaret(text.length)
    }

    private fun autoSize() {
        maxWidth = Region.USE_PREF_SIZE
        minWidth = Region.USE_PREF_SIZE
        textProperty().addListener { _, _, new ->
            updateWidth(new)
        }
        updateWidth(text)
        thread {
            Thread.sleep(10)
            Platform.runLater {
                updateWidth(text)
            }
        }
    }

    private fun updateWidth(text: String) {
        val textWidth = TextUtils.computeTextWidth(font, text) + 5.0
        prefWidth = textWidth.coerceAtLeast(15.0)
    }

    companion object {
        private const val STYLE_CLASS = "hextant-text"

        private fun shouldConsume(ev: KeyEvent): Boolean = when {
            ev.code.isFunctionKey || ev.code.isMediaKey || ev.code.isModifierKey -> false
            ev.isControlDown || ev.isAltDown                                     -> false
            ev.code in controlKeys                                               -> false
            else                                                                 -> true
        }

        private val controlKeys = setOf(ENTER, ESCAPE, TAB)

        private val INPUT_MODE = KeyCodeCombination(I)

        private val COMMAND_MODE = KeyCodeCombination(ESCAPE)
    }
}
