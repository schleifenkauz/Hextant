package hextant.expr.view

import bundles.SimpleReactiveProperty

internal object Style {
    object BorderColor : SimpleReactiveProperty<String>("border-color") {
        override val default: String get() = "purple"
    }
}