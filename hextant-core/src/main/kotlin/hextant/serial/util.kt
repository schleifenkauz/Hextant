/**
 * @author Nikolaus Knop
 */

package hextant.serial

import java.io.IOException

internal inline fun safeIO(action: () -> Unit) {
    try {
        action()
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
}
