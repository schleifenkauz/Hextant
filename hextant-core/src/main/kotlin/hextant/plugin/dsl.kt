/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.plugin

import bundles.Bundle
import bundles.Property
import hextant.command.*
import hextant.context.*
import hextant.fx.Stylesheets
import hextant.inspect.*
import hextant.plugin.PluginBuilder.Phase.*
import hextant.serial.*
import hextant.serial.SerialProperties.projectRoot
import hextant.settings.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.reflect.*

/**
 * Registers the given [command].
 */
inline fun <reified R : Any> PluginBuilder.registerCommand(command: Command<R, *>) {
    on(Initialize) { ctx -> ctx[Commands].register(command) }
    on(Disable) { ctx -> ctx[Commands].unregister(command) }
}

/**
 * Builds a command and registers it.
 */
inline fun <reified R : Any, T : Any> PluginBuilder.registerCommand(block: CommandBuilder<R, T>.() -> Unit): Command<R, T> {
    val command = command(block)
    registerCommand(command)
    return command
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

@Suppress("UNCHECKED_CAST")
private val json = Json {
    serializersModule = SerializersModule {
        polymorphic(Bundle::class, Class.forName("bundles.BundleImpl").kotlin as KClass<Bundle>, BundleSerializer)
    }
}

@PublishedApi internal fun <T, Read : Any, Write : Read> PluginBuilder.persistentProperty(
    permission: Write,
    property: Property<T, Read, Write>,
    type: KType
) {
    val deserializer = json.serializersModule.serializer(type)
    on(Initialize) { ctx ->
        if (!testing) {
            val path = getPath(ctx, property)
            ctx[permission, property] = if (path.exists()) {
                path.readJson(deserializer, json) as T
            } else property.default
        } else ctx[permission, property] = property.default
    }
    on(Close) { ctx ->
        if (!testing) {
            val value = ctx[permission, property]
            val path = getPath(ctx, property)
            path.writeJson(deserializer, value, json)
        }
    }
    on(Disable) { ctx ->
        ctx.delete(permission, property)
        if (!testing) {
            getPath(ctx, property).delete()
        }
    }
}

/**
 * Registers a *persistent property*.
 *
 * - When the plugin is enabled the default value of the property is used to initialize it.
 * - The value of this property is stored in the project directory and is retrieved when the project is opened.
 * - When the plugin is disabled the value of the property and the corresponding file is deleted.
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T, Read : Any, Write : Read> PluginBuilder.persistentProperty(
    permission: Write,
    property: Property<T, Read, Write>
) {
    persistentProperty(permission, property, typeOf<T>())
}

private fun getPath(ctx: Context, property: Property<*, *, *>) = ctx[projectRoot].resolve("${property.name}.json")


/**
 * Registers a *configurable property*.
 *
 * @param editorFactory produces the editor used to edit the value of this property.
 * Configurable properties can be set by the user via the command line.
 */
fun <T : Any> PluginBuilder.configurableProperty(property: Property<T, *, Any>, editorFactory: EditorFactory<T>) {
    val p = ConfigurableProperty(property, editorFactory)
    registerCommand<Context, Unit> {
        name = "Set property $property"
        shortName = "set-$property"
        description = "Sets the value of the property $property"
        applicableIf { ctx -> ctx.hasProperty(Settings) }
        addParameter<Any> {
            name = "value"
            description = "The value"
            editWith(editorFactory)
        }
        executing { ctx, (value) ->
            ctx[Settings][property] = value as T
        }
    }
    on(Initialize) { ctx -> ctx[PropertyRegistrar].configurable.add(p) }
    on(Disable) { ctx -> ctx[PropertyRegistrar].configurable.remove(p) }
}

/**
 * Registers a *configurable property*.
 *
 * Uses the [EditorFactory] implementation for type [T] to produce the editor.
 * @see configurableProperty
 */
inline fun <reified T : Any> PluginBuilder.configurableProperty(property: Property<T, *, Any>) {
    configurableProperty(property) { context.createEditor() }
}

/**
 * Register a command delegation.
 */
inline fun <reified D : Any> PluginBuilder.commandDelegation(noinline delegation: (D) -> Any?) {
    on(Initialize) { ctx ->
        ctx[Commands].registerDelegation(D::class, delegation)
    }
    on(Disable) { ctx ->
        ctx[Commands].unregisterDelegation(D::class, delegation)
    }
}