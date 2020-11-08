/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.context.Context
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.stage.Stage
import java.io.OutputStream
import java.io.PrintStream
import kotlin.concurrent.thread

/**
 * Can be used to show the output of a process to the user.
 */
class ConsoleOutputView(private val context: Context) : OutputStream() {
    private val display = TextArea()
    private val out = System.out
    private val err = System.err

    init {
        display.isEditable = false
        display.setPrefSize(500.0, 500.0)
    }

    @Suppress("KDocMissingDocumentation")
    override fun write(b: Int) {
        out.write(b)
        Platform.runLater { display.appendText(b.toChar().toString()) }
    }

    private fun restoreIO() {
        System.setOut(out)
        System.setErr(err)
    }

    /**
     * Execute the given [action] redirecting stdout and stderr to the text area.
     */
    fun execute(action: () -> Unit, prematureExit: () -> Unit) {
        display.clear()
        val s = PrintStream(this)
        System.setOut(s)
        System.setErr(s)
        val stage = Stage()
        var failed = false
        val t = thread {
            try {
                action()
                Platform.runLater { stage.hide() }
            } catch (ex: Throwable) {
                ex.printStackTrace()
                restoreIO()
                failed = true
            } finally {
                restoreIO()
            }
        }
        with(stage) {
            scene = Scene(display)
            context[Stylesheets].manage(scene)
            show()
            setOnCloseRequest {
                if (failed) hide()
                else if (confirmStop()) {
                    t.stop()
                    prematureExit()
                    hide()
                    restoreIO()
                }
            }
        }
    }

    private fun confirmStop(): Boolean {
        val question = "Do you want to really to stop the process?"
        val answer = Alert(CONFIRMATION, question, ButtonType.NO, ButtonType.YES)
            .showAndWait()
            .orElse(ButtonType.NO)
        return answer == ButtonType.YES
    }

    companion object {
        private const val BUFFER_SIZE = 32
    }
}