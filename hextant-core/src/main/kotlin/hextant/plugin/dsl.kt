/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.plugin

import bundles.Property
import hextant.command.*
import hextant.context.*
import hextant.fx.Stylesheets
import hextant.inspect.*
import hextant.plugin.PluginBuilder.Phase.*
import hextant.serial.SerialProperties.projectRoot
import hextant.settings.ConfigurableProperty
import hextant.settings.PropertyRegistrar
import java.nio.file.Files

/**
 * Registers the given [command].
 */
inline fun <reified R : Any> PluginBuilder.command(command: Command<R, *>) {
    on(Initialize) { ctx -> ctx[Commands].register(command) }
    on(Disable) { ctx -> ctx[Commands].unregister(command) }
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
    on(Disable) { ctx -> ctx[Inspections].unregister(T::class, inspection) }
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
 * Sets the value of the given [property].
 */
fun <T, Read : Any, Write : Read> PluginBuilder.set(permission: Write, property: Property<T, Read, Write>, value: T) {
    on(Initialize) { ctx -> ctx[permission, property] = value }
    on(Disable) { ctx -> ctx.delete(permission, property) }
}

/**
 * Sets the value of the given [property].
 */
fun <T> PluginBuilder.set(property: Property<T, Any, Any>, value: T) {
    set(Any(), property, value)
}

/**
 * Registers a *persistent property*.
 *
 * - When the plugin is enabled the default value of the property is used to initialize it.
 * - The value of this property is stored in the project directory and is retrieved when the project is opened.
 * - When the plugin is disabled the value of the property and the corresponding file is deleted.
 */
fun <T, Read : Any, Write : Read> PluginBuilder.persistentProperty(
    permission: Write,
    property: Property<T, Read, Write>
) {
    on(Enable) { ctx ->
        ctx[permission, property] = property.default
    }
    on(Initialize) { ctx ->
        if (!ctx.hasProperty(permission, property)) {
            val input = context.createInput(getPath(ctx, property))
            val value = input.readObject()
            ctx[permission, property] = value as T
        }
    }
    on(Close) { ctx ->
        val output = ctx.createOutput(getPath(ctx, property))
        val value = ctx[permission, property]
        output.writeObject(value)
    }
    on(Disable) { ctx ->
        ctx.delete(permission, property)
        Files.deleteIfExists(getPath(ctx, property))
    }
}

private fun getPath(ctx: Context, property: Property<*, *, *>) = ctx[projectRoot].resolve("${property.name}.bin")


/**
 * Registers a *configurable property*.
 *
 * @param editorFactory produces the editor used to edit the value of this property.
 * Configurable properties can be set by the user via the command line.
 */
fun <T : Any> PluginBuilder.configurableProperty(property: Property<T, *, Any>, editorFactory: EditorFactory<T>) {
    val p = ConfigurableProperty(property, editorFactory)
    on(Initialize) { ctx -> ctx[PropertyRegistrar].configurable.add(p) }
    on(Disable) { ctx -> ctx[PropertyRegistrar].configurable.remove(p) }
}
