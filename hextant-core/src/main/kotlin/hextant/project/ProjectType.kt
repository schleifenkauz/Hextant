/**
 * @author Nikolaus Knop
 */

package hextant.project

import hextant.context.Context
import hextant.core.Editor

/**
 * This interface is implemented by objects that are able to create new projects.
 */
interface ProjectType {
    /**
     * Create the root editor for a new project.
     */
    fun createProject(context: Context): Editor<*>
}