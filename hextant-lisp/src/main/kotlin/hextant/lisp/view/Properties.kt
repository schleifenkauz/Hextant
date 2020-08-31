package hextant.lisp.view

import bundles.SimpleProperty
import javafx.scene.layout.Region

object Properties {
    /**
     * The parent region of the visual editor
     */
    val editorParentRegion =
        SimpleProperty<Region>("editor parent region")
}