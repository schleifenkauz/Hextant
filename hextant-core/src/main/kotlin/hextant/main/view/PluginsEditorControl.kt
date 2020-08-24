/**
 *@author Nikolaus Knop
 */

package hextant.main.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.EditorControl
import hextant.main.editor.PluginsEditor
import hextant.main.plugins.PluginManager.DisableConfirmation
import hextant.plugins.Plugin
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.control.ButtonType.NO
import javafx.scene.control.ButtonType.YES
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import kotlinx.coroutines.runBlocking

internal class PluginsEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: PluginsEditor,
    arguments: Bundle
) : EditorControl<HBox>(editor, arguments), PluginsEditorView {
    private val availableList = ListView<Plugin>()
    private val enabledList = ListView<Plugin>()

    override val available: MutableCollection<Plugin>
        get() = availableList.items
    override val enabled: MutableCollection<Plugin>
        get() = enabledList.items
    private val availableSearchField = TextField()
    private val enabledSearchField = TextField()

    override val availableSearchText: String
        get() = availableSearchField.text
    override val enabledSearchText: String
        get() = enabledSearchField.text

    init {
        availableList.setCellFactory { PluginCell(enabled = false) }
        enabledList.setCellFactory { PluginCell(enabled = true) }
        availableSearchField.textProperty().addListener { _ ->
            editor.searchInAvailable(this@PluginsEditorControl)
        }
        enabledSearchField.textProperty().addListener { _ ->
            runBlocking {
                editor.searchInEnabled(this@PluginsEditorControl)
            }
        }
        editor.addView(this)
    }

    override fun createDefaultRoot(): HBox =
        HBox(VBox(availableSearchField, availableList), VBox(enabledSearchField, enabledList))

    override fun confirmEnable(enabled: Collection<Plugin>): Boolean {
        val alert = Alert(CONFIRMATION, enabled.joinToString("\n") { it.name })
        alert.headerText = "Confirm enabling dependencies"
        val result = alert.showAndWait().orElse(ButtonType.CANCEL)
        return result == ButtonType.OK
    }

    override fun confirmDisable(disabled: Collection<Plugin>): Boolean {
        val alert = Alert(CONFIRMATION, disabled.joinToString("\n") { it.name })
        alert.headerText = "This will disable the following dependent plugins"
        val result = alert.showAndWait().orElse(ButtonType.CANCEL)
        return result == ButtonType.OK
    }

    override fun alertError(message: String) {
        Alert(ERROR, message).show()
    }

    override fun askDisable(plugin: Plugin): DisableConfirmation {
        val alert = Alert(CONFIRMATION, "Plugin ${plugin.name} is not needed anymore, disable it?", YES, NO, ALL, NONE)
        return when (alert.showAndWait().orElse(NO)) {
            YES -> DisableConfirmation.Yes
            NO -> DisableConfirmation.No
            ALL -> DisableConfirmation.All
            NONE -> DisableConfirmation.None
            else -> error("cannot happen")
        }
    }

    private inner class PluginCell(private val enabled: Boolean) : ListCell<Plugin>() {
        init {
            setOnMouseClicked { ev ->
                if (ev.clickCount >= 2) {
                    if (enabled) editor.disable(item, this@PluginsEditorControl)
                    else editor.enable(item, this@PluginsEditorControl)
                }
            }
        }

        override fun updateItem(item: Plugin?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) {
                text = null
                tooltip = null
                return
            }
            val (_, name, author, _, description) = item
            text = "$name by $author"
            tooltip = Tooltip(description)
        }
    }

    companion object {
        private val ALL = ButtonType("All")
        private val NONE = ButtonType("None")
    }
}