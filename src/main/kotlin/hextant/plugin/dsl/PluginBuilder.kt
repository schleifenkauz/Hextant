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
import hextant.inspect.Inspection
import hextant.inspect.Inspections
import hextant.plugin.Plugin

@PluginDsl
class PluginBuilder @PublishedApi internal constructor(@PublishedApi internal val platform: HextantPlatform) {
    lateinit var name: String

    lateinit var author: String

    inline fun <reified T : Any, E : Editable<T>> editable(noinline noArg: () -> E, noinline oneArg: (T) -> E) {
        val factory = platform[Public, EditableFactory]
        factory.register(T::class, noArg)
        factory.register(T::class, oneArg)
    }

    inline fun <reified E : Editable<*>, Ed : Editor<E>> editor(noinline factory: (E, Context) -> Ed) {
        platform[Public, EditorFactory].register(E::class, factory)
    }

    inline fun <reified E : Editable<*>> view(noinline factory: (E, Context) -> EditorControl<*>) {
        platform[Public, EditorControlFactory].register(E::class, factory)
    }

    inline fun costum(block: HextantPlatform.() -> Unit) {
        platform.block()
    }

    inline fun <reified T : Any, I : Inspection<T>> inspection(noinline factory: (T) -> I) {
        platform[Public, Inspections].of(T::class).register(factory)
    }

    inline fun <reified T : Any, C : Command<T, *>> command(command: C) {
        platform[Public, Commands].of(T::class).register(command)
    }

    @PublishedApi internal fun build() = Plugin(name, author)
}