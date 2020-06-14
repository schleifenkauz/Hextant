package hextant.fx

import bundles.Property
import hextant.core.Internal
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
        val resource = javaClass.classLoader.getResource(stylesheet)
        if (resource == null) {
            System.err.println("Can not find stylesheet $stylesheet")
            return
        }
        paths.add(resource.toExternalForm())
    }

    companion object : Property<Stylesheets, Any, Internal>("stylesheets")
}