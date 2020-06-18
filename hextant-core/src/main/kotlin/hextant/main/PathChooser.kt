/**
 * @author Nikolaus Knop
 */

package hextant.main

import bundles.Property
import hextant.context.Context
import hextant.context.Internal
import java.nio.file.Path

/**
 * Can be used to let the user choose a file path.
 */
interface PathChooser {
    /**
     * Let the user choose a file path using the given [context].
     * If ´null` is returned that means that the user has not chosen a path.
     */
    fun choosePath(context: Context): Path?

    companion object : Property<PathChooser, Internal, Internal>("path chooser")
}