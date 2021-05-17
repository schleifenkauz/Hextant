/**
 *@author Nikolaus Knop
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package hextant.completion.gui

import hextant.completion.Completer
import hextant.completion.Completion
import hextant.context.Context
import hextant.context.executeSafely
import hextant.fx.HextantPopup
import hextant.fx.IconManager
import hextant.fx.fixWidth
import hextant.fx.onAction
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.TextFlow
import javafx.stage.Popup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import reaktive.event.event

/**
 * A [Popup] that displays completion items.
 */
internal class CompletionPopup<Ctx, T : Any>(
    private val context: Context,
    private val ctx: Ctx,
    private val completer: () -> Completer<Ctx, T>
) : HextantPopup(context) {
    private val root = VBox()
    private var input = ""
    private val choose = event<Completion<T>>()
    private val updater = GlobalScope.actor<String>(Dispatchers.Main, capacity = Channel.CONFLATED) {
        for (input in channel) {
            val completions = context.executeSafely("getting completions", emptyList()) {
                withContext(Dispatchers.Default) {
                    completer().completions(ctx, input)
                }
            }
            root.children.setAll(completions.map { c -> createCompletionItem(c) })
            valid = true
            if (completions.isNotEmpty() && ownerNode.isFocused) super.show()
            else hide()
        }
    }

    /**
     * Emits events when a completion was chosen by the user.
     */
    val completionChosen = choose.stream
    private var valid = false

    init {
        scene.root = root
        root.styleClass.add("completions")
    }

    /**
     * Show the completions if there are any
     */
    override fun show() {
        if (!valid) {
            GlobalScope.launch(Dispatchers.Main) {
                updater.send(input)
            }
        } else if (root.children.isNotEmpty() && ownerNode.isFocused) {
            super.show()
        }
    }

    /**
     * Update the input typed by the user
     */
    fun updateInput(text: String) {
        input = text
        valid = false
        if (isShowing) GlobalScope.launch { updater.send(text) }
    }

    private fun createCompletionItem(completion: Completion<T>): Node {
        val container = BorderPane()
        val left = HBox(5.0)
        addIcon(completion.icon, left)
        addCompletionText(completion, left)
        container.left = left
        addInfo(container, completion.infoText)
        installTooltip(container, completion.tooltipText)
        configureItem(container)
        container.onAction {
            choose.fire(completion)
            hide()
        }
        return container
    }

    private fun addCompletionText(completion: Completion<T>, left: HBox) {
        val flow = TextFlow()
        val labels = completion.completionText.map { Label(it.toString()) }
        for (region in completion.match) {
            for (i in region) {
                labels[i].style = "-fx-text-fill: #3657FF"
            }
        }
        flow.children.addAll(labels)
        left.children.addAll(flow)
    }

    private fun configureItem(container: BorderPane) {
        container.isFocusTraversable = true
        container.styleClass.add("completion")
        container.fixWidth(FIXED_COMPLETION_ITEM_WIDTH)
    }

    private fun addIcon(icon: String?, left: HBox) {
        if (icon != null) {
            val view = context[IconManager].viewIcon(icon)
            left.children.add(view)
        }
    }

    private fun addInfo(container: BorderPane, infoText: String?) {
        if (infoText != null) {
            container.right = Label(infoText)
        }
    }

    private fun installTooltip(container: BorderPane, tooltipText: String?) {
        if (tooltipText != null) {
            Tooltip.install(container, Tooltip(tooltipText))
        }
    }

    companion object {
        private const val FIXED_COMPLETION_ITEM_WIDTH = 500.0

        /**
         * Return a new [CompletionPopup] which uses the given [completer] with the specified [context].
         */
        fun <T : Any> forContext(context: Context, completer: () -> Completer<Context, T>) =
            CompletionPopup(context, context, completer)
    }
}

