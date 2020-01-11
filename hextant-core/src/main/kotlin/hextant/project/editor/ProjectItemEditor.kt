/**
 * @author Nikolaus Knop
 */

package hextant.project.editor

import hextant.Editor
import hextant.project.ProjectItem
import hextant.serial.ReactivePath

interface ProjectItemEditor<T : Any, I : ProjectItem<T>> : Editor<I> {
    val path: ReactivePath?
}