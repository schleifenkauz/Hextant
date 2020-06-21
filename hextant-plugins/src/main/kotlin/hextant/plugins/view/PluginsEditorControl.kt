/**
 *@author Nikolaus Knop
 */

package hextant.plugins.view

import bundles.Bundle
import hextant.core.view.EditorControl
import hextant.plugins.editor.PluginsEditor
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import reaktive.value.ReactiveValue
import reaktive.value.fx.asReactiveValue

class PluginsEditorControl(private val editor: PluginsEditor, arguments: Bundle) :
    EditorControl<HBox>(editor, arguments), PluginsEditorView {
    private val available = ListView<String>()
    private val enabled = ListView<String>()
    private val searchField = TextField()

    override val searchText: ReactiveValue<String> = searchField.textProperty().asReactiveValue()

    init {
        available.setCellFactory { PluginCell(enabled = false) }
        enabled.setCellFactory { PluginCell(enabled = true) }
        editor.addView(this)
    }

    override fun createDefaultRoot(): HBox = HBox(VBox(searchField, available), enabled)

    override fun showAvailable(plugins: Collection<String>) {
        available.items.setAll(plugins)
    }

    override fun enabled(plugin: String) {
        enabled.items.add(plugin)
    }

    override fun disabled(plugin: String) {
        enabled.items.remove(plugin)
    }

    override fun available(plugin: String) {
        available.items.add(plugin)
    }

    override fun notAvailable(id: String) {
        available.items.remove(id)
    }

    private inner class PluginCell(private val enabled: Boolean) : ListCell<String>() {
        init {
            setOnMouseClicked { ev ->
                if (ev.clickCount >= 2) {
                    if (enabled) editor.disable(item)
                    else editor.enable(item)
                }
            }
        }

        override fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) {
                text = null
                tooltip = null
                return
            }
            val (_, name, author, _, description) = editor.getInfo(item)
            text = "$name by $author"
            tooltip = Tooltip(description)
        }
    }
}