/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.context.Context
import hextant.context.Internal
import hextant.serial.SerialProperties.projectRoot
import java.nio.file.Path
import kotlin.reflect.full.companionObjectInstance

internal fun Context.setProjectRoot(path: Path) {
    if (hasProperty(projectRoot)) return
    val perm = Internal::class.companionObjectInstance as Internal
    set(perm, projectRoot, path)
}