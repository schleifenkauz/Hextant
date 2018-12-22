/**
 *@author Nikolaus Knop
 */

package hextant.plugin.dsl

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.CorePermissions.Public
import hextant.command.Command
import hextant.command.Commands
import hextant.core.*
import hextant.impl.myLogger
import hextant.inspect.Inspection
import hextant.inspect.Inspections
import hextant.plugin.Plugin

@PluginDsl
class PluginBuilder @PublishedApi internal constructor(@PublishedApi internal val platform: HextantPlatform) {
    /**
     * The name of the plugin
     */
    lateinit var name: String

    /**
     * The author of the plugin
     */
    lateinit var author: String

    /**
     * Register the specified [noArg] and [oneArg] factories for editables of the specified class [T]
     */
    inline fun <reified T : Any, reified E : Editable<T>> editable(noinline noArg: () -> E, noinline oneArg: (T) -> E) {
        val factory = platform[Public, EditableFactory]
        factory.register(T::class, noArg)
        factory.register(T::class, oneArg)
        val typeName = T::class.qualifiedName
        val editableName = E::class.qualifiedName
        logger.config { "Registered $editableName for $typeName" }
    }

    /**
     * Register the specified [factory] for editors of the class [E]
     */
    inline fun <reified E : Editable<*>, reified Ed : Editor<E>> editor(noinline factory: (E, Context) -> Ed) {
        platform[Public, EditorFactory].register(E::class, factory)
        val editableName = E::class.qualifiedName
        val editorName = Ed::class.qualifiedName
        logger.config { "Registered $editorName for $editableName" }
    }

    /**
     * Register the specified [factory] for views of the class [E]
     */
    inline fun <reified E : Editable<*>, reified V : EditorControl<*>> view(noinline factory: (E, Context) -> V) {
        platform[Public, EditorControlFactory].register(E::class, factory)
        val viewName = V::class.qualifiedName
        val editableName = E::class.qualifiedName
        logger.config { "Registered $viewName for $editableName" }
    }

    /**
     * Execute a custom [block] on the [HextantPlatform]
     */
    inline fun costum(block: HextantPlatform.() -> Unit) {
        platform.block()
    }

    /**
     * Register the specified [factory] for an inspection [I] inspecting instances of class [T]
     */
    inline fun <reified T : Any, reified I : Inspection<T>> inspection(noinline factory: (T) -> I) {
        platform[Public, Inspections].of(T::class).register(factory)
        val name = I::class.qualifiedName
        logger.config { "Inspection $name registered" }
    }

    /**
     * Register the specified [command]
     */
    inline fun <reified T : Any, C : Command<T, *>> command(command: C) {
        platform[Public, Commands].of(T::class).register(command)
        logger.config { "Command ${command.name} registered" }
    }

    @PublishedApi internal fun build() = Plugin(name, author).also {
        logger.config("Loaded plugin $name of author $author")
    }

    companion object {
        val logger by myLogger()
    }
}