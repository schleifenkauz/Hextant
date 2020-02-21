/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.Context
import hextant.bundle.Internal
import hextant.bundle.Property
import java.nio.file.Path

interface PathChooser {
    fun choosePath(context: Context): Path?

    companion object : Property<PathChooser, Internal, Internal>("path chooser")
}