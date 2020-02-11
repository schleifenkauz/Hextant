/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import javafx.scene.image.Image
import javafx.scene.image.ImageView

class IconManager {
    private val images = HashMap<String, Image>()

    private fun loadImage(resource: String): Image {
        val url =
            javaClass.classLoader.getResource(resource) ?: throw RuntimeException("Resource not found: '$resource'")
        return Image(url.toExternalForm())
    }

    fun viewIcon(resource: String): ImageView {
        val image = images.getOrPut(resource) { loadImage(resource) }
        return ImageView(image)
    }

    companion object : Property<IconManager, Public, Internal>("icon manager", default = IconManager())
}