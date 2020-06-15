/**
 *@author Nikolaus Knop
 */

package hextant.plugin.dsl

import bundles.Bundle
import hextant.*
import hextant.command.*
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.core.Internal
import hextant.core.editor.TokenEditor
import hextant.core.view.*
import hextant.fx.*
import hextant.inspect.*
import hextant.plugin.Plugin
import validated.Validated

/**
 * The plugin builder is used to create plugins.
 * @property context the root context
 */
@PluginDsl
class PluginBuilder @PublishedApi internal constructor(val context: Context) {
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
    inline fun <reified R> editor(noinline factory: (Context, R) -> Editor<R>) {
        context[EditorFactory].register(factory)
    }

    /**
     * Register the specified [factory] for editors for class [R]
     */
    inline fun <reified R> defaultEditor(noinline factory: (Context) -> Editor<R>) {
        context[EditorFactory].register(factory)
    }

    /**
     * Register an [Editor] for values of type [R] using an editor for type [T] and transforming with the given function
     */
    inline fun <reified T, reified R> registerConversion(noinline transform: (T) -> Validated<R>) {
        defaultEditor { context -> context.createEditor<T>().map(transform) }
    }

    /**
     * Register the specified [factory] for views of the class [E]
     */
    inline fun <reified E : Editor<*>> view(noinline factory: (E, Bundle) -> EditorControl<*>) {
        context[EditorControlFactory].register(E::class, factory)
    }

    /**
     * Register a factory for views of class [E] that produce a [hextant.fx.CompoundEditorControl]
     * applying the given [block] to it.
     */
    inline fun <reified E : Editor<*>> compoundView(
        styleCls: String? = null,
        crossinline block: CompoundEditorControl.Vertical.(editor: E) -> Unit
    ) {
        view { e: E, args ->
            CompoundEditorControl.build(e, args) {
                if (styleCls != null) styleClass.add(styleCls)
                block(e)
            }
        }
    }

    /**
     * Register an [TokenEditorControl] for the given type of editor
     * @param styleClass the style class added to the text field displaying the token editor, or `null` if there is none
     * @param completer the [Completer] used by the [TokenEditorView]. Defaults to [NoCompleter]
     * @param extraConfig some extra configuration
     */
    inline fun <reified E : TokenEditor<*, TokenEditorView>> tokenEditorView(
        styleClass: String? = null,
        completer: Completer<Context, Any> = NoCompleter,
        crossinline extraConfig: TokenEditorControl.() -> Unit = {}
    ) {
        view { e: E, args ->
            args[AbstractTokenEditorControl.COMPLETER] = completer
            TokenEditorControl(e, args).apply {
                if (styleClass != null) root.styleClass.add(styleClass)
                extraConfig()
            }
        }
    }

    /**
     * Execute a custom [block] on the [HextantPlatform]
     */
    inline fun costum(block: Context.() -> Unit) {
        context.block()
    }

    /**
     * Register the specified [inspection] for instances of class [T]
     */
    inline fun <reified T : Any> inspection(inspection: Inspection<T>) {
        context[Inspections].register(T::class, inspection)
    }

    /**
     * Register an inspection using the given inspection [configuration].
     */
    inline fun <reified T : Any> registerInspection(configuration: InspectionBuilder<T>.() -> Unit) {
        context[Inspections].registerInspection(configuration)
    }

    /**
     * Register the specified [command]
     */
    inline fun <reified R : Any> command(command: Command<R, *>) {
        context[Commands].register(R::class, command)
    }

    /**
     * Register a command configured by [config]
     */
    inline fun <reified R : Any, T> registerCommand(config: CommandBuilder<R, T>.() -> Unit) {
        command(command(config))
    }

    /**
     * Add the stylesheet from the specified [path].
     */
    fun stylesheet(path: String) {
        context[Internal, Stylesheets].add(path)
    }

    @PublishedApi internal fun build() = Plugin(name, author)
}