/**
 *@author Nikolaus Knop
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package hextant.completion.gui

import fxutils.alwaysHGrow
import fxutils.centerChildren
import fxutils.hspace
import fxutils.infiniteSpace
import fxutils.onAction
import fxutils.style
import fxutils.styleClass
import hextant.completion.Completer
import hextant.completion.Completion
import hextant.completion.CompletionCollector
import hextant.context.Context
import hextant.fx.HextantPopup
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.control.Tooltip.install
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Popup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignC
import org.kordamp.ikonli.materialdesign2.MaterialDesignS
import reaktive.event.event

/**
 * A [Popup] that displays completion items.
 */
internal class CompletionPopup<Ctx>(
    context: Context,
    private val ctx: Ctx,
    private val completer: () -> Completer<Ctx>,
    private val maxItems: () -> Int
) : HextantPopup(context) {
    private val layout = VBox() styleClass "completion-list"
    private var input = ""
    private val choose = event<Completion<*>>()

    /**
     * Emits events when a completion was chosen by the user.
     */
    val completionChosen = choose.stream
    private var valid = false

    init {
        scene.root = StackPane(layout)
    }

    /**
     * Show the completions if there are any
     */
    override fun show() {
        if (!valid) {
            updateItems()
        }
        if (layout.children.isNotEmpty() && ownerNode.isFocused) {
            super.show()
        }
    }

    private fun updateItems() {
        if (input.isEmpty()) {
            layout.children.clear()
            valid = true
            if (isShowing) hide()
            return
        }
        CoroutineScope(Dispatchers.Default).launch {
            val collector = CompletionCollector.limit(maxItems())
            with(completer()) {
                try {
                    collectCompletions(ctx, input, collector)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@launch
                }
            }
            collector.join()
            val completions = collector.getCompletions()
            Platform.runLater {
                layout.children.clear()
                for ((index, completion) in completions.withIndex()) {
                    val cell = createCompletionCell(completion, index)
                    layout.children.add(cell)
                }
                valid = true
                if (isShowing) {
                    if (completions.isEmpty()) {
                        hide()
                    } else {
                        layout.children[0].requestFocus()
                    }
                }
            }
        }
    }

    /**
     * Update the input typed by the user
     */
    fun updateInput(text: String) {
        input = text
        valid = false
        if (isShowing) updateItems()
    }

    private fun selectItem(index: Int) {
        val wrapped = index.mod(layout.children.size)
        layout.children[wrapped].requestFocus()
    }

    private fun createCompletionCell(completion: Completion<*>, index: Int): Node {
        val container = HBox().styleClass("option-cell", "completion")
        val iconCode = completion.icon ?: MaterialDesignC.CIRCLE_MEDIUM
        container.children.add(FontIcon(iconCode) styleClass "completion-icon")
        container.children.add(hspace(5.0))
        val flow = makeCompletionTextFlow(completion)
        container.children.add(flow)
        val space = infiniteSpace()
        space.minWidth = 20.0
        container.children.add(space)
        if (completion.infoText != null) {
            container.children.add(Label(completion.infoText) styleClass "completion-info")
        }
        if (completion.tooltipText != null) {
            install(container, Tooltip(completion.tooltipText))
        }
        container.onAction {
            choose.fire(completion)
            hide()
        }
        container.addEventHandler(KeyEvent.KEY_PRESSED) { ev ->
            when (ev.code) {
                KeyCode.UP -> selectItem(index - 1)
                KeyCode.DOWN -> selectItem(index + 1)
                else -> return@addEventHandler
            }
            ev.consume()
        }
        return container
    }

    private fun makeCompletionTextFlow(completion: Completion<*>): TextFlow {
        val flow = TextFlow()
        val ln = completion.completionText.length

        @Suppress("EmptyRange")
        val regions = listOf(0 until 0) + completion.match + listOf(ln until ln)
        for ((r1, r2) in regions.zipWithNext()) {
            if (!(r1.isEmpty())) {
                val matchedText = completion.completionText.substring(r1)
                flow.children.add(Text(matchedText).styleClass("completion-text", "matched-text"))
            }
            val unmatchedText = completion.completionText.substring(r1.last + 1, r2.first)
            if (unmatchedText.isNotEmpty()) {
                flow.children.add(Text(unmatchedText) styleClass ("completion-text"))
            }
        }
        return flow.centerChildren()
    }
}

