/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.completion.*
import hextant.core.editable.Expandable

open class ConfiguredExpander<E : Editable<*>, Ex : Expandable<*, E>>(
    private val config: ExpanderConfig<E>,
    edited: Ex,
    context: Context,
    completer: Completer<String> = NoCompleter
) : Expander<E, Ex>(edited, context, completer) {
    constructor(
        config: ExpanderConfig<E>,
        edited: Ex,
        context: Context,
        factory: CompletionFactory<String> = CompletionFactory.simple(),
        strategy: CompletionStrategy = CompletionStrategy.simple
    ) : this(config, edited, context, config.completer(strategy, factory))

    @Suppress("UNCHECKED_CAST")
    override fun expand(text: String): E? = config.expand(text)

    override fun accepts(child: Editor<*>): Boolean = true
}