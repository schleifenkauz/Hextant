/**
 * @author Nikolaus Knop
 */

package hextant.project.editor

import hextant.Editor
import hextant.bundle.SimpleProperty
import hextant.core.editor.ExpanderConfig
import hextant.project.ProjectItem
import hextant.serial.ReactivePath

interface ProjectItemEditor<T : Any, I : ProjectItem<T>> : Editor<I> {
    val path: ReactivePath?

    val itemName: FileNameEditor

    override fun supportsCopyPaste(): Boolean = true

    fun deletePhysical()

    companion object {
        private val config = SimpleProperty<ExpanderConfig<*>>("project item expander config")

        @Suppress("UNCHECKED_CAST")
        fun <R : Any> expanderConfig() = config as SimpleProperty<ExpanderConfig<out Editor<R>>>
    }
}