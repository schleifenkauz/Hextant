/**
 * @author Nikolaus Knop
 */

package hextant.plugin

import hextant.context.Context
import hextant.core.Editor

/**
 * The [PluginBuilder] provides the DSL needed to initialize plugins.
 */
@PluginDsl
class PluginBuilder internal constructor(
    @PublishedApi internal val phase: Phase,
    @PublishedApi internal val context: Context,
    @PublishedApi internal val root: Editor<*>?
) {
    /**
     * If the specified [phase] is currently executed the given [action] is supplied with the root context.
     */
    inline fun on(phase: Phase, action: (context: Context) -> Unit) {
        if (this.phase == phase && root == null) action(context)
    }

    /**
     * If the specified [phase] in currently executed and the root project is of type [R]
     * the given [action] is supplied with the root context and root project.
     */
    inline fun <reified R : Editor<*>> on(phase: Phase, action: (context: Context, root: R) -> Unit) {
        if (this.phase == phase && root is R) action(context, root)
    }

    /**
     * The phase decides which actions are actually executed.
     */
    enum class Phase {
        /**
         * This phase is only executed when a plugin is actively enabled by the user on a project.
         */
        Enable,

        /**
         * This phase is executed when a project is loaded and the plugins need to be activated.
         */
        Initialize,

        /**
         * This phase is executed when the user disables a plugin.
         */
        Disable,

        /**
         * This phase is executed when a project is closed.
         */
        Close
    }
}