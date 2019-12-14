/**
 *@author Nikolaus Knop
 */

package hextant.plugin.dsl

import hextant.*
import hextant.base.CompoundEditorControl
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.command.*
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.core.editor.TokenEditor
import hextant.core.view.FXTokenEditorView
import hextant.core.view.TokenEditorView
import hextant.impl.Stylesheets
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
    inline fun <reified R : Any, reified E : Editor<R>> editor(noinline factory: (Context, R) -> E) {
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
    inline fun <reified E : Editor<*>, reified V : EditorControl<*>> view(noinline factory: (E, Bundle) -> V) {
        platform[Public, EditorControlFactory].register(E::class, factory)
        val viewName = V::class.qualifiedName
        val editableName = E::class.qualifiedName
        logger.config { "Registered $viewName for $editableName" }
    }

    /**
     * Register a factory for views of class [E] that produce a [hextant.base.CompoundEditorControl]
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
     * Register an [FXTokenEditorView] for the given type of editor
     * @param styleClass the style class added to the text field displaying the token editor, or `null` if there is none
     * @param completer the [Completer] used by the [TokenEditorView]. Defaults to [NoCompleter]
     * @param extraConfig some extra configuration
     */
    inline fun <reified E : TokenEditor<*, TokenEditorView>> tokenEditorView(
        styleClass: String? = null,
        completer: Completer<String> = NoCompleter,
        crossinline extraConfig: FXTokenEditorView.() -> Unit = {}
    ) {
        view { e: E, args ->
            FXTokenEditorView(e, args, completer).apply {
                if (styleClass != null) root.styleClass.add(styleClass)
                extraConfig()
            }
        }
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
    inline fun <reified R : Any> command(command: Command<R, *>) {
        platform[Public, Commands].of(R::class).register(command)
        logger.config { "Command ${command.name} registered" }
    }

    /**
     * Register a command configured by [config]
     */
    inline fun <reified R : Any, T> registerCommand(config: CommandBuilder<R, T>.() -> Unit) {
        command(command(config))
    }

    fun stylesheet(path: String) {
        platform[Stylesheets].add(path)
    }

    @PublishedApi internal fun build() = Plugin(name, author).also {
        logger.config("Loaded plugin $name of author $author")
    }

    companion object {
        val logger by myLogger()
    }
}