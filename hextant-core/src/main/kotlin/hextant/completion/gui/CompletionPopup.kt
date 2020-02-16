/**
 *@author Nikolaus Knop
 */

package hextant.completion.gui

import hextant.completion.Completer
import hextant.completion.Completion
import hextant.fx.*
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.*
import javafx.scene.text.TextFlow
import javafx.stage.Popup
import reaktive.event.event

/**
 * A [Popup] that displays completion items.
 */
class CompletionPopup<Ctx, T>(
    private val context: Ctx,
    private val iconManager: IconManager,
    completer: Completer<Ctx, T>
) : Popup() {
    private var input = ""
    private val choose = event<Completion<T>>()
    /**
     * Emits events when a completion was chosen by the user.
     */
    val completionChosen = choose.stream
    private var valid = false

    /**
     * The completer used to find completion items
     */
    var completer = completer
        set(value) {
            field = value
            valid = false
            if (isShowing) updateCompletions()
        }

    init {
        isAutoHide = true
        scene.registerShortcuts {
            on("ESCAPE") {
                hide()
            }
        }
    }

    /**
     * Show the completions if there are any
     */
    override fun show() {
        if (!valid) updateCompletions()
        if (scene.root.childrenUnmodifiable.isEmpty()) return
        super.show()
    }

    /**
     * Update the input typed by the user
     */
    fun updateInput(text: String) {
        input = text
        valid = false
        if (isShowing) updateCompletions()
    }

    private fun setCompletions(completions: Collection<Completion<T>>) {
        val root = VBox()
        root.styleClass.add("completions")
        for (c in completions) {
            addChoice(c, root)
        }
        scene.root = root
    }

    private fun updateCompletions() {
        val completions = completer.completions(context, input)
        setCompletions(completions)
        valid = true
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
        return container
    }

    private fun addCompletionText(completion: Completion<T>, left: HBox) {
        val flow = TextFlow()
        val labels = completion.completionText.map { Label(it.toString()) }
        for (region in completion.match) {
            for (i in region) {
                labels[i].style = "-fx-text-fill: blue;"
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
            val view = iconManager.viewIcon(icon)
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

    private fun addChoice(c: Completion<T>, container: VBox) {
        val n = createCompletionItem(c)
        container.children.add(n)
        n.onAction {
            choose.fire(c)
            hide()
        }
    }

    companion object {
        private const val FIXED_COMPLETION_ITEM_WIDTH = 500.0
    }
}

