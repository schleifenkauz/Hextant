/**
 * @author Nikolaus Knop
 */

package hextant.plugin

import bundles.*
import hextant.command.*
import hextant.config.*
import hextant.config.ConfigurableProperty
import hextant.config.PropertyRegistrar
import hextant.context.*
import hextant.context.Properties.propertyChangeHandler
import hextant.fx.ResultStyleClasses
import hextant.fx.Stylesheets
import hextant.inspect.*
import hextant.plugin.PluginBuilder.Phase.*
import hextant.serial.*
import hextant.serial.SerialProperties.projectRoot
import kotlinx.serialization.serializer
import reaktive.Observer
import kotlin.reflect.*

/**
 * Registers the given [command].
 */
fun <R : Any> PluginBuilder.registerCommand(command: Command<R, *>) {
    on(Initialize) { ctx -> ctx[FeatureRegistrar].register(command) }
    on(Disable) { ctx -> ctx[FeatureRegistrar].unregister(command) }
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
fun <T : Any> PluginBuilder.inspection(inspection: Inspection<T>) {
    on(Initialize) { ctx -> ctx[FeatureRegistrar].register(inspection) }
    on(Disable) { ctx -> ctx[FeatureRegistrar].unregister(inspection) }
}

/**
 * Builds an inspection and registers it.
 */
inline fun <reified T : Any> PluginBuilder.registerInspection(block: InspectionBuilder<T>.() -> Unit) {
    inspection(inspection(block))
}

/**
 * Apply the given CSS-[stylesheet].
 */
fun PluginBuilder.stylesheet(resource: String) {
    on(Initialize) { ctx -> ctx[Stylesheets].add(resource) }
    on(Disable) { ctx -> ctx[Stylesheets].remove(resource) }
}

/**
 * Register the given [styleClass] for editors whose current result is of type [R].
 */
inline fun <reified R : Any> PluginBuilder.resultStyleClass(noinline styleClass: (R) -> String?) {
    on(Initialize) { ctx -> ctx[ResultStyleClasses].register(R::class, styleClass) }
    on(Disable) { ctx -> ctx[ResultStyleClasses].unregister(R::class, styleClass) }
}

/**
 * Sets the value of the given [property].
 */
fun <T : Any, P : Permission> PluginBuilder.set(permission: P, property: Property<T, P>, value: T) {
    on(Initialize) { ctx -> ctx[permission, property] = value }
    on(Disable) { ctx -> ctx.delete(permission, property) }
}

/**
 * Sets the value of the given [property].
 */
fun <T : Any> PluginBuilder.set(property: PublicProperty<T>, value: T) {
    set(property, value)
}

@Suppress("UNCHECKED_CAST")
@PublishedApi internal fun <T : Any, P : Permission> PluginBuilder.persistentProperty(
    permission: P,
    property: Property<T, P>,
    type: KType
) {
    val serializer = serializer(type)
    on(Initialize) { ctx ->
        if (!testing) {
            val path = getPath(ctx, property)
            ctx[permission, property] = if (path.exists()) {
                path.readJson(serializer, json) as T
            } else property.default ?: throw NoSuchElementException("No value for $property")
        } else ctx[permission, property] = property.default ?: throw NoSuchElementException("No value for $property")
    }
    on(Close) { ctx ->
        if (!testing) {
            val value = ctx[property]
            val path = getPath(ctx, property)
            path.writeJson(serializer, value, json)
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
inline fun <reified T : Any, P : Permission> PluginBuilder.persistentProperty(permission: P, property: Property<T, P>) {
    persistentProperty(permission, property, typeOf<T>())
}

private fun getPath(ctx: Context, property: Property<*, *>) = ctx[projectRoot].resolve("${property.name}.json")


/**
 * Registers a *configurable property*.
 *
 * @param editorFactory produces the editor used to edit the value of this property.
 * Configurable properties can be set by the user via the command line.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> PluginBuilder.configurableProperty(property: PublicProperty<T>, editorFactory: EditorFactory<T>) {
    val p = ConfigurableProperty(property, editorFactory)
    registerCommand<Context, Unit> {
        name = "Set property ${property.name}"
        shortName = "set-${property.name}"
        description = "Sets the value of the property ${property.name}"
        applicableIf { ctx -> ctx.hasProperty(Settings) }
        val value = addParameter<Any> {
            name = "value"
            description = "The value"
            editWith(editorFactory)
        }
        executing { ctx, args ->
            ctx[Settings][property] = args[value] as T
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
inline fun <reified T : Any> PluginBuilder.configurableProperty(property: PublicProperty<T>) {
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

@PublishedApi internal val propertyObservers = mutableMapOf<Pair<KClass<*>, Property<*, *>>, Observer>()

/**
 * Observe the specified [property] for changes in the given context.
 */
inline fun <reified Ctx : Any, T : Any> PluginBuilder.observeProperty(
    property: Property<T, *>,
    noinline handler: (Ctx, T) -> Unit
) {
    on(Initialize) { ctx ->
        propertyObservers[Ctx::class to property] = ctx[propertyChangeHandler].observe(Ctx::class, property, handler)
    }
    on(Disable) {
        propertyObservers.remove(Ctx::class to property)!!.kill()
    }

}