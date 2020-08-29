/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.SimpleProperty
import hextant.context.Context
import hextant.context.createOutput
import hextant.core.Editor
import java.nio.file.Path

internal data class Project(val root: Editor<*>, val context: Context, val location: Path) {
    fun save() {
        val output = context.createOutput(location)
        output.writeObject(root)
    }

    companion object : SimpleProperty<Project>("project")
}