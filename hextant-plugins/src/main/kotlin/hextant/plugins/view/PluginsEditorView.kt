/**
 * @author Nikolaus Knop
 */

package hextant.plugins.view

import hextant.core.EditorView
import reaktive.value.ReactiveValue

interface PluginsEditorView : EditorView {
    fun showAvailable(plugins: Collection<String>)

    fun enabled(plugin: String)

    fun disabled(plugin: String)

    fun available(plugin: String)

    fun notAvailable(id: String)

    val searchText: ReactiveValue<String>
}