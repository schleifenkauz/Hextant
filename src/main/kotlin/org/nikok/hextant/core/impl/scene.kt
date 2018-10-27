/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.impl

import javafx.scene.Parent
import javafx.scene.Scene

internal fun scene(root: Parent): Scene {
    val scene = Scene(root)
    Stylesheets.apply(scene.stylesheets)
    return scene
}