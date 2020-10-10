/**
 * @author Nikolaus Knop
 */

package hextant.project.editor

import bundles.SimpleProperty
import hextant.context.Context
import hextant.core.Editor
import hextant.core.editor.ExpanderConfig
import hextant.project.ProjectItem

/**
 * An editor for a [ProjectItem], for example a [hextant.project.File] or a  [hextant.project.Directory].
 */
interface ProjectItemEditor<T, I : ProjectItem<T>> : Editor<I> {
    /**
     * The editor for the name of this item
     */
    val itemName: FileNameEditor

    override fun supportsCopyPaste(): Boolean = true

    /**
     * Deletes the physical representation of this project item.
     */
    fun deletePhysical()

    companion object {
        private val config = SimpleProperty<ExpanderConfig<*, Context>>("project item expander config")

        /**
         * The property that is used by the [FileEditor] to produce a root editor.
         */
        @Suppress("UNCHECKED_CAST")
        fun <R> expanderConfig() = config as SimpleProperty<ExpanderConfig<out Editor<R>, Context>>
    }
}