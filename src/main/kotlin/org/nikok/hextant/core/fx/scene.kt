/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.fx

import javafx.scene.Parent
import javafx.scene.Scene
import org.nikok.hextant.core.impl.Stylesheets

internal fun scene(root: Parent): Scene {
    val scene = Scene(root)
    Stylesheets.apply(scene.stylesheets)
    return scene
}