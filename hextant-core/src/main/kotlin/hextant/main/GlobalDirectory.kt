/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.SimpleProperty
import java.io.File

internal class GlobalDirectory(private val root: File) {
    fun resolve(name: String): File = root.resolve(name)

    companion object : SimpleProperty<GlobalDirectory>("global directory")
}