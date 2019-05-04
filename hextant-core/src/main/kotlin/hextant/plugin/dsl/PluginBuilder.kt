/**
 *@author Nikolaus Knop
 */

package hextant.plugin.dsl

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.command.Command
import hextant.command.Commands
import hextant.impl.myLogger
import hextant.inspect.Inspection
import hextant.inspect.Inspections
import hextant.plugin.Plugin

@PluginDsl
class PluginBuilder @PublishedApi internal constructor(val platform: HextantPlatform) {
    /**
     * The name of the plugin
     */
    lateinit var name: String

    /**
     * The author of the plugin
     */
    lateinit var author: String

    /**
     * Register the specified [factory] for editors of the class [R]
     */
    inline fun <reified R : Any, reified E : Editor<R>> editor(noinline factory: (R, Context) -> E) {
        platform[Public, EditorFactory].register(R::class, factory)
        val editableName = R::class.qualifiedName
        val editorName = E::class.qualifiedName
        logger.config { "Registered $editorName for $editableName" }
    }

    /**
     * Register the specified [factory] for editors for class [R]
     */
    inline fun <reified R : Any, reified E : Editor<R>> defaultEditor(noinline factory: (Context) -> E) {
        platform[Public, EditorFactory].register(R::class, factory)
    }

    /**
     * Register the specified [factory] for views of the class [E]
     */
    inline fun <reified E : Editor<*>, reified V : EditorControl<*>> view(noinline factory: (E, Context, Bundle) -> V) {
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
    inline fun <reified T : Any, reified I : Inspection> inspection(noinline factory: (T) -> I) {
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

    val unsupported: () -> Nothing
        get() {
            throw UnsupportedOperationException()
        }

    val unsupported1: (Any?) -> Nothing get() = { throw UnsupportedOperationException() }

    @PublishedApi internal fun build() = Plugin(name, author).also {
        logger.config("Loaded plugin $name of author $author")
    }

    companion object {
        val logger by myLogger()
    }
}