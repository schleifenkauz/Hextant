/**
 * @author Nikolaus Knop
 */

package hextant.main

import bundles.Property
import hextant.Context
import hextant.Editor
import hextant.core.Internal

interface ProjectType<out R : Editor<*>> {
    fun createProjectRoot(context: Context): R

    companion object : Property<ProjectType<Editor<*>>, Any, Internal>("project type")
}