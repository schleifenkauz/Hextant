/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor

/**
 * An [Expander] which expands text using its [config]
 */
abstract class ConfiguredExpander<R, E : Editor<R>>(
    private val config: ExpanderDelegate<E>,
    private val tokenType: TokenType<R?>,
    context: Context,
    editor: E? = null
) : Expander<R, E>(context, editor) {
    constructor(config: ExpanderDelegate<E>, context: Context, editor: E? = null) :
            this(config, NullTokenType, context, editor)

    override fun expand(text: String): E? = config.expand(text, expansionContext())

    override fun expand(completion: Any): E? = config.expand(completion, expansionContext())

    override fun compile(token: String): R = tokenType.compile(token) ?: defaultResult()
}