/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.core.Editor
import kotlinx.serialization.Transient

/**
 * An [Expander] which expands text using its [config]
 */
abstract class ConfiguredExpander<R, E : Editor<R>> : Expander<R, E>() {
    @Transient
    private lateinit var config: ExpanderDelegate<E>

    @Transient
    private lateinit var tokenType: TokenType<R?>

    fun configure(configurator: ExpanderDelegate<E>, tokenType: TokenType<R?> = NullTokenType) {
        this.config = configurator
        this.tokenType = tokenType
    }

    override fun expand(text: String): E? = config.expand(text, expansionContext())

    override fun expand(completion: Any): E? = config.expand(completion, expansionContext())

    override fun compile(token: String): R = tokenType.compile(token) ?: defaultResult()
}