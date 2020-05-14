/**
 * @author Nikolaus Knop
 */

package hextant.main

import bundles.Property
import hextant.Context
import hextant.core.Internal
import java.nio.file.Path

interface PathChooser {
    fun choosePath(context: Context): Path?

    companion object : Property<PathChooser, Internal, Internal>("path chooser")
}