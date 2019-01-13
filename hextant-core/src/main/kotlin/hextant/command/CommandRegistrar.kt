/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.Context
import hextant.command.Command.Category
import hextant.command.gui.showArgumentPrompt
import javafx.scene.Node
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import org.nikok.reaktive.event.event
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Used to register commands with receiver of type [R]
 * @constructor
 */
class CommandRegistrar<R : Any> internal constructor(private val parents: List<CommandRegistrar<Any>>, cls: KClass<R>) {
    private val addCategory = event<Category>("Added category for $cls")

    val addedCategory = addCategory.stream

    private val _categories: MutableSet<Category> = mutableSetOf()

    val categories: Set<Category> get() = _categories + parents.flatMap { it.categories }

    private val declaredCommands: MutableSet<Command<R, Any?>> = mutableSetOf()

    private val accelerators = mutableMapOf<KeyCombination, Command<R, *>>()

    private val shortcuts = mutableMapOf<Command<R, *>, KeyCombination>()

    /**
     * @return all commands registered for receivers of type [R] and all superclasses
     */
    @Suppress("UNCHECKED_CAST") val commands: Set<Command<R, Any?>>
        get() {
            return declaredCommands + parents.flatMap { it.commands }
        }

    /**
     * @return the [commands]
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = commands

    /**
     * @return the [commands] applicable on [receiver]
     */
    @JvmName("commandsFor") @JvmSynthetic operator fun getValue(receiver: R, property: KProperty<*>) =
            commandsFor(receiver)

    /**
     * @return the [commands] applicable on the specified [receiver]
     */
    fun commandsFor(receiver: R): Set<Command<R, *>> =
            commands.filterTo(mutableSetOf()) { c -> c.isApplicableOn(receiver) }

    /**
     * Register the specified [command] for receivers of type [R]
     */
    fun register(command: Command<R, *>) {
        declaredCommands.add(command)
        val category = command.category ?: return
        if (_categories.add(category)) {
            addCategory.fire(category)
        }
    }

    /**
     * Register the specified [command] and assign the specified [shortcut] to it
     */
    fun register(command: Command<R, *>, shortcut: KeyCombination) {
        register(command)
        registerShortcut(command, shortcut)
    }

    fun registerShortcut(command: Command<R, *>, shortcut: KeyCombination) {
        accelerators[shortcut] = command
        shortcuts[command] = shortcut
    }

    fun getShortcut(command: Command<R, *>): KeyCombination? =
            shortcuts[command] ?: parents.fold(null as KeyCombination?) { acc, r -> acc ?: r.shortcuts[command] }

    private fun handle(
        keyEvent: KeyEvent,
        target: R,
        node: Node,
        context: Context
    ): Boolean {
        val comb = accelerators.keys.find { it.match(keyEvent) }
        val c = accelerators[comb]
        return if (c != null) {
            if (c.parameters.isEmpty()) {
                c.execute(target)
                return true
            }
            val args = showArgumentPrompt(node.scene.window, c, context) ?: return false
            if (args.any { it == null }) return false
            c.execute(target, *args.toTypedArray())
            return true

        } else parents.any { p -> p.handle(keyEvent, target, node, context) }
    }

    fun listen(node: Node, target: R, context: Context) {
        node.addEventHandler(KeyEvent.KEY_RELEASED) { event: KeyEvent ->
            handle(event, target, node, context)
        }
    }
}