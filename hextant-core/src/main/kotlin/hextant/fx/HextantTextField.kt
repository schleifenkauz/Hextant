/**
 *@author Nikolaus Knop
 */

package hextant.fx

import com.sun.javafx.scene.control.skin.TextFieldSkin
import javafx.application.Platform
import javafx.scene.control.Skin
import javafx.scene.control.TextField
import javafx.scene.layout.Region
import javafx.scene.text.Font
import javafx.scene.text.Text
import reaktive.event.event
import kotlin.concurrent.thread

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
        maxWidth = Region.USE_PREF_SIZE
        minWidth = Region.USE_PREF_SIZE
        textProperty().addListener { _, _, new ->
            updateWidth(new)
        }
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
    }
}
