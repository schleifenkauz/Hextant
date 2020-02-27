package hextant.impl

import hextant.bundle.SimpleProperty
import javafx.scene.Scene

internal class Stylesheets {
    private val paths = mutableListOf<String>()

    fun apply(stylesheets: MutableCollection<String>) {
        stylesheets.addAll(paths)
    }

    fun apply(scene: Scene) {
        apply(scene.stylesheets)
    }

    fun add(stylesheet: String) {
        paths.add(stylesheet)
    }

    companion object : SimpleProperty<Stylesheets>("stylesheets")
}