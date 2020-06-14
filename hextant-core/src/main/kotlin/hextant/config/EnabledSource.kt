/**
 *@author Nikolaus Knop
 */

package hextant.config

import bundles.Property
import hextant.core.Internal

/**
 * Source for [Enabled] objects
 */
class EnabledSource {
    private val sources = mutableListOf<() -> Collection<Enabled>>()

    /**
     * Add the given [source] for [Enabled] objects.
     */
    fun addSource(source: () -> Collection<Enabled>) {
        sources.add(source)
    }

    /**
     * Return all the [Enabled] objects from all the sources.
     */
    fun all(): Collection<Enabled> = sources.flatMap { source -> source() }

    companion object : Property<EnabledSource, Any, Internal>("enabled-source")
}