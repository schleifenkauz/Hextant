package hextant.impl

import hextant.bundle.Internal
import hextant.bundle.Property
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

    companion object : Property<Stylesheets, Internal, Internal>("stylesheets")
}