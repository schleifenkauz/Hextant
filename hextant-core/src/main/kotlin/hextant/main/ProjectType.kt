/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.Context
import hextant.Editor
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property

interface ProjectType<out R : Editor<*>> {
    fun createProjectRoot(context: Context): R

    companion object : Property<ProjectType<Editor<*>>, Public, Internal>("project type")
}