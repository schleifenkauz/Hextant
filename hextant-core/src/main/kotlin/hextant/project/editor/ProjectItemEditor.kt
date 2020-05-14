/**
 * @author Nikolaus Knop
 */

package hextant.project.editor

import bundles.SimpleProperty
import hextant.Editor
import hextant.core.editor.ExpanderConfig
import hextant.project.ProjectItem

interface ProjectItemEditor<T : Any, I : ProjectItem<T>> : Editor<I> {
    val itemName: FileNameEditor

    override fun supportsCopyPaste(): Boolean = true

    fun deletePhysical()

    companion object {
        private val config = SimpleProperty<ExpanderConfig<*>>("project item expander config")

        @Suppress("UNCHECKED_CAST")
        fun <R : Any> expanderConfig() = config as SimpleProperty<ExpanderConfig<out Editor<R>>>
    }
}