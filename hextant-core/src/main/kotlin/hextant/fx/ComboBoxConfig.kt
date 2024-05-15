package hextant.fx

import bundles.Bundle
import bundles.publicProperty
import hextant.core.editor.ComboBoxSource
import impl.org.controlsfx.skin.SearchableComboBoxSkin
import javafx.collections.FXCollections.observableList
import javafx.scene.control.ComboBox
import javafx.scene.control.skin.ComboBoxListViewSkin
import javafx.util.StringConverter

object ComboBoxConfig {
    val searchable = publicProperty("combo-box-searchable", default = false)
    val lazy = publicProperty("combo-box-lazy", default = true)

    fun <E> setSearchable(comboBox: ComboBox<E>, searchable: Boolean) {
        comboBox.skin =
            if (searchable) SearchableComboBoxSkin(comboBox)
            else ComboBoxListViewSkin(comboBox)
    }

    fun <C : Any> createComboBox(source: ComboBoxSource<C>, arguments: Bundle): ComboBox<C> {
        val comboBox = when (arguments[lazy]) {
            false -> ComboBox(observableList(source.choices()))
            true -> LazyComboBox(source::choices)
        }
        setSearchable(comboBox, arguments[searchable])
        comboBox.converter = object : StringConverter<C>() {
            override fun toString(item: C?): String = if (item != null) source.toString(item) else "<null>"

            override fun fromString(str: String?): C? = if (str != null) source.fromString(str) else null
        }
        comboBox.valueProperty().addListener { _, _, item ->
            if (item != null) source.select(item)
        }
        return comboBox.withStyleClass("choice-editor-combo-box")
    }
}