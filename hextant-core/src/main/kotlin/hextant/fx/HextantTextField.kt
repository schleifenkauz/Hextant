/**
 *@author Nikolaus Knop
 */

package hextant.fx

import com.sun.javafx.scene.control.skin.TextFieldSkin
import javafx.scene.control.Skin
import javafx.scene.control.TextField
import javafx.scene.text.Font
import javafx.scene.text.Text
import reaktive.event.event

/**
 * A Text field that adds the "hextant-text" style class and does automatically resize its width
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
        styleClass.add(STYLE_CLASS)
        autoSize()
    }

    override fun requestFocus() {
        super.requestFocus()
        positionCaret(text.length)
    }

    private fun autoSize() {
        textProperty().addListener { _, _, new ->
            updateWidth(new)
        }
        updateWidth(text)
    }

    private fun updateWidth(text: String) {
        val w = Math.max(TextUtils.computeTextWidth(font, text) + 5.0, 15.0)
        minWidth = w
        prefWidth = w
        maxWidth = w
    }

    companion object {
        private const val STYLE_CLASS = "hextant-text"
    }
}
