/**
 *@author Nikolaus Knop
 */

package hextant.fx

import com.sun.javafx.scene.control.skin.TextFieldSkin
import javafx.scene.control.Skin
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode.LEFT
import javafx.scene.input.KeyCode.RIGHT
import javafx.scene.input.KeyEvent
import javafx.scene.text.Font
import javafx.scene.text.Text
import reaktive.event.event

/**
 * A Text field that adds the "hextant-text" style class and automatically resized its width
*/
open class HextantTextField(text: String? = "") : TextField(text) {
    override fun createDefaultSkin(): Skin<*> = HextantTextFieldSkin()

    private val userUpdatesText = event<String>()

    val userUpdatedText = userUpdatesText.stream

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
        styleClass.add(HextantTextField.STYLE_CLASS)
        autoSize()
        letJumpWithArrows()
    }

    private fun letJumpWithArrows() {
        addEventHandler(KeyEvent.KEY_RELEASED) { e ->
            if (e.code == LEFT && (e.isAltDown || caretPosition == 0)) {
                focusPrevious()
                e.consume()
            } else if (e.code == RIGHT && (e.isAltDown || caretPosition == text.length)) {
                focusNext()
                e.consume()
            }
        }
    }

    private fun autoSize() {
        textProperty().addListener { _, _, new ->
            updateWidth(new)
        }
        updateWidth(text)
    }

    private fun updateWidth(text: String) {
        prefWidth = Math.max(TextUtils.computeTextWidth(font, text) + 5.0, 15.0)
    }

    companion object {
        private const val STYLE_CLASS = "hextant-text"
    }
}
