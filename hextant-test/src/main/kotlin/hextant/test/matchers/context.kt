/**
 * @author Nikolaus Knop
 */

package hextant.test.matchers

import hextant.Context
import hextant.HextantPlatform
import hextant.bundle.CorePermissions.Public
import hextant.undo.UndoManager

inline fun testingContext(block: Context.() -> Unit = {}): Context = Context.newInstance(HextantPlatform.forTesting) {
    set(Public, UndoManager, UndoManager.newInstance())
    block()
}