/**
 * @author Nikolaus Knop
 */

package hextant.project.editor

import hextant.Editor
import hextant.project.ProjectItem

interface ProjectItemEditor<T : Any, I : ProjectItem<T>> : Editor<I>