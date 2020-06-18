/**
 * @author Nikolaus Knop
 */

package hextant.main

import bundles.Property
import hextant.context.Context
import hextant.context.Internal
import hextant.core.Editor

/**
 * A project type with root editor of type [R].
 */
interface ProjectType<out R : Editor<*>> {
    /**
     * Create the root editor in the given [context].
     */
    fun createProjectRoot(context: Context): R

    companion object : Property<ProjectType<Editor<*>>, Any, Internal>("project type")
}