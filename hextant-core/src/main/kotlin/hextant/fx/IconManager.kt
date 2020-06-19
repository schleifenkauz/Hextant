/**
 *@author Nikolaus Knop
 */

package hextant.fx

import bundles.Property
import hextant.context.Internal
import javafx.scene.image.Image
import javafx.scene.image.ImageView

/**
 * Acts as a cache for images
 */
class IconManager {
    private val images = HashMap<String, Image>()

    private fun loadImage(resource: String): Image {
        val url =
            javaClass.classLoader.getResource(resource) ?: throw RuntimeException("Resource not found: '$resource'")
        return Image(url.toExternalForm())
    }

    /**
     * Resolves the given the given [resource] and constructs an [ImageView].
     */
    fun viewIcon(resource: String): ImageView {
        val image = images.getOrPut(resource) { loadImage(resource) }
        return ImageView(image)
    }

    companion object : Property<IconManager, Any, Internal>("icon manager") {
        override val default: IconManager = IconManager()
    }
}