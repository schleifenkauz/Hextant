/**
 * @author Nikolaus Knop
 */

package hextant.plugin

import bundles.Property
import hextant.command.*
import hextant.fx.Stylesheets
import hextant.inspect.*
import hextant.plugin.PluginBuilder.Phase.Disable
import hextant.plugin.PluginBuilder.Phase.Initialize

/**
 * Registers the given [command].
 */
inline fun <reified R : Any> PluginBuilder.command(command: Command<R, *>) {
    on(Initialize) { ctx -> ctx[Commands].register(command) }
    on(Disable) { ctx -> ctx[Commands].disable(command) }
}

/**
 * Builds a command and registers it.
 */
inline fun <reified R : Any, T : Any> PluginBuilder.registerCommand(block: CommandBuilder<R, T>.() -> Unit) {
    command(command(block))
}

/**
 * Registers the given [inspection].
 */
inline fun <reified T : Any> PluginBuilder.inspection(inspection: Inspection<T>) {
    on(Initialize) { ctx -> ctx[Inspections].register(T::class, inspection) }
    on(Disable) { ctx -> ctx[Inspections].disable(inspection) }
}

/**
 * Builds an inspection and registers it.
 */
inline fun <reified T : Any> PluginBuilder.registerInspection(block: InspectionBuilder<T>.() -> Unit) {
    inspection(inspection(block))
}

/**
 * Applies the given [stylesheet]
 */
fun PluginBuilder.stylesheet(resource: String) {
    on(Initialize) { ctx -> ctx[Stylesheets].add(resource) }
    on(Disable) { ctx -> ctx[Stylesheets].remove(resource) }
}

/**
 * Sets the value of the given [property]
 */
fun <T, Read : Any, Write : Read> PluginBuilder.set(permission: Write, property: Property<T, Read, Write>, value: T) {
    on(Initialize) { ctx -> ctx[permission, property] = value }
    on(Disable) { ctx -> ctx.delete(permission, property) }
}

/**
 * Sets the value of the given [property]
 */
fun <T> PluginBuilder.set(property: Property<T, Any, Any>, value: T) {
    set(Any(), property, value)
}