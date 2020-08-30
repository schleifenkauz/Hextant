package hextant.fx

import bundles.Property
import hextant.context.Internal
import javafx.scene.Scene

internal class Stylesheets(private val classLoader: ClassLoader) {
    private val managed = mutableSetOf<Scene>()
    private val paths = mutableListOf<String>()

    fun manage(scene: Scene) {
        scene.stylesheets.addAll(paths)
        managed.add(scene)
    }

    fun add(stylesheet: String) {
        val resource = getResource(stylesheet) ?: return
        for (scene in managed) scene.stylesheets.add(resource)
        paths.add(resource)
    }

    private fun getResource(stylesheet: String): String? {
        val resource = classLoader.getResource(stylesheet)
        if (resource == null) System.err.println("Can not find stylesheet $stylesheet")
        return resource?.toExternalForm()
    }

    fun remove(stylesheet: String) {
        val resource = getResource(stylesheet)
        for (scene in managed) scene.stylesheets.remove(resource)
        paths.remove(resource)
    }

    companion object : Property<Stylesheets, Any, Internal>("stylesheets")
}