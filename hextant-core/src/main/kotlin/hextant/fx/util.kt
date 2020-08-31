/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.fx

import bundles.Bundle
import bundles.createBundle
import hextant.command.Commands
import hextant.command.line.CommandLine
import hextant.context.Context
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.view.CompoundEditorControl.Compound
import hextant.core.view.EditorControl
import javafx.scene.*
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.layout.Region
import javafx.stage.PopupWindow
import javafx.stage.Stage
import reaktive.value.now
import validated.Validated
import validated.invalidComponent

internal fun control(skin: Skin<out Control>): Control {
    return object : Control() {
        init {
            setSkin(skin)
        }
    }
}

internal fun Control.setRoot(node: Node) {
    skin = null
    skin = skin(this, node)
}

internal fun skin(control: Control, node: Node): Skin<Control> = SimpleSkin(control, node)


private class SimpleSkin(
    private val control: Control, private val node: Node
) : Skin<Control> {
    override fun getSkinnable(): Control = control

    override fun getNode(): Node = node

    override fun dispose() {}
}

internal inline fun Node.registerShortcut(s: KeyCombination, crossinline action: () -> Unit) {
    addEventHandler(KeyEvent.KEY_RELEASED) { k ->
        if (s.match(k)) {
            action()
            k.consume()
        }
    }
}

internal fun PopupWindow.show(node: Node) {
    val p = node.localToScreen(0.0, node.prefHeight(-1.0)) ?: return
    show(node, p.x, p.y)
}

internal fun TextField.smartSetText(new: String) {
    val previous = text
    if (previous != new) {
        text = new
        if (previous.isEmpty()) {
            positionCaret(new.length)
        }
    }
}

internal fun Node.onAction(action: () -> Unit) {
    addEventHandler(KeyEvent.KEY_RELEASED) { ev ->
        if (ev.code == ENTER) {
            action()
            ev.consume()
        }
    }
    addEventHandler(MouseEvent.MOUSE_PRESSED) { ev ->
        if (ev.clickCount >= 2) {
            action()
            ev.consume()
        }
    }
}

/**
 * Return a [Label] with the given text and with the 'hextant-text' and the 'keyword' style class.
 */
fun keyword(name: String) = Label(name).apply {
    styleClass.add("hextant-text")
    styleClass.add("keyword")
}

/**
 * Return a [Label] with the given text and with the 'hextant-text' and the 'operator' style class.
 */
fun operator(name: String) = Label(name).apply {
    styleClass.add("hextant-text")
    styleClass.add("operator")
}

internal fun Region.fixWidth(value: Double) {
    prefWidth = value
    minWidth = value
    maxWidth = value
}

internal fun <N : Node> N.withStyleClass(vararg names: String) = apply { styleClass.addAll(*names) }

internal fun <N : Node> N.withStyle(style: String) = also { it.style = style }

internal fun <C : Control> C.withTooltip(tooltip: Tooltip) = apply { this.tooltip = tooltip }

internal fun <C : Control> C.withTooltip(text: String) = withTooltip(Tooltip(text))

internal fun hextantLabel(text: String, graphic: Node? = null) = Label(text, graphic).withStyleClass("hextant-text")

/**
 * Add the editor control for the given [editor] to this compound view.
 * The [config] block is used to initialize properties of the [hextant.core.EditorView.arguments] bundle.
 */
fun Compound.view(editor: Editor<*>, bundle: Bundle = createBundle(), config: Bundle.() -> Unit) =
    view(editor, bundle.apply(config))

/**
 * Gets input from the user by showing the given [editor] in a [Dialog] to him.
 *
 * If the user cancels the dialog [invalidComponent] is returned.
 * @param control the [EditorControl] that shows the editor to the user.
 * @param buttonTypes the possible button types.
 */
fun <R> getUserInput(
    editor: Editor<R>,
    control: EditorControl<*> = editor.context.createControl(editor),
    buttonTypes: List<ButtonType> = listOf(ButtonType.OK, ButtonType.CANCEL)
): Validated<R> {
    val d = Dialog<Validated<R>>()
    d.dialogPane.content = control
    d.dialogPane.buttonTypes.setAll(buttonTypes)
    d.setResultConverter { btn ->
        when (btn) {
            ButtonType.OK -> editor.result.now
            ButtonType.CANCEL -> invalidComponent
            else              -> error("Unexpected button type: $btn")
        }
    }
    return d.showAndWait().orElse(invalidComponent)
}

fun KeyEventHandlerBody<*>.handleCommands(target: Any, context: Context) {
    for (command in context[Commands].applicableOn(target)) {
        val shortcut = command.shortcut
        if (shortcut != null) {
            on(shortcut, consume = false) { ev ->
                if (command.parameters.isEmpty()) {
                    val result = command.execute(target, emptyList())
                    if (result != false) ev.consume()
                } else {
                    val cl = context[CommandLine]
                    cl.expand(command)
                }
            }
        }
    }
}

/**
 * Shows a new [Stage] with the given node as the root.
 */
fun showStage(root: Parent): Stage = Stage().apply {
    scene = Scene(root)
    show()
}

/**
 * Shows a [Stage] with the view of the given [editor] as the root.
 */
fun showStage(editor: Editor<*>) = showStage(editor.context.createControl(editor))