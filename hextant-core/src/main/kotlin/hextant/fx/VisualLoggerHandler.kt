/**
 *@author Nikolaus Knop
 */

package hextant.fx

import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.util.*
import java.util.logging.*

/**
 * Displays [LogRecord] in a [ListView].
 */
class VisualLoggerHandler : Handler() {
    private val view = ListView<LogRecord>()

    init {
        view.setCellFactory { LogRecordCell() }
    }

    /**
     * Called when a new [LogRecord] is published.
     */
    override fun publish(record: LogRecord) {
        if (!isLoggable(record)) return
        view.items.add(record)
    }

    /**
     * Nothing
     */
    override fun flush() {}

    /**
     * Nothing
     */
    override fun close() {}

    private class LogRecordCell : ListCell<LogRecord>() {
        override fun updateItem(item: LogRecord?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) {
                graphic = null
                return
            }
            val pane = BorderPane()
            pane.left = Label(item.level.toString()).withStyle(logLevelStyle(item.level))
            pane.center = Label(item.message)
            pane.right = Label(Date.from(Instant.ofEpochMilli(item.millis)).toString())
            pane.bottom = item.thrown?.let { ex -> displayStackTrace(ex) }
            graphic = pane
        }

        private fun logLevelStyle(level: Level): String {
            val color = when (level) {
                Level.WARNING -> "yellow"
                Level.SEVERE  -> "red"
                else          -> "green"
            }
            return "-fx-text-fill: $color"
        }

        private fun displayStackTrace(ex: Throwable): TextArea {
            val w = StringWriter()
            val p = PrintWriter(w)
            ex.printStackTrace(p)
            p.close()
            val a = TextArea("$w\n $ex")
            a.isEditable = false
            return a
        }
    }

    /**
     * Returns the JavaFX that displays the log messages.
     */
    fun display(): Control = view

    /**
     * A stage showing the log view.
     */
    val stage by lazy { createStage() }

    private fun createStage(): Stage = Stage().apply {
        scene = Scene(display())
        title = "Log"
        width = 1000.0
        height = 1000.0
        centerOnScreen()
    }
}