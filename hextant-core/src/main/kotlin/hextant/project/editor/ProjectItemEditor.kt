/**
 * @author Nikolaus Knop
 */

package hextant.project.editor

import hextant.Editor
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.core.editor.ExpanderConfig
import hextant.project.ProjectItem
import hextant.serial.ReactivePath

interface ProjectItemEditor<T : Any, I : ProjectItem<T>> : Editor<I> {
    val path: ReactivePath?

    val itemName: FileNameEditor

    override fun supportsCopyPaste(): Boolean = true

    fun deletePhysical()

    companion object {
        private val config = Property<ExpanderConfig<*>, Public, Public>("project item expander config")

        @Suppress("UNCHECKED_CAST")
        fun <R : Any> expanderConfig() = config as Property<ExpanderConfig<out Editor<R>>, Public, Public>
    }
}