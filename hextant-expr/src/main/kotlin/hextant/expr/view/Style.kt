package hextant.expr.view

import bundles.SimpleReactiveProperty
import javafx.scene.paint.Color

internal object Style {
    val borderColor = SimpleReactiveProperty.withDefault("border-color", Color.PURPLE)
}