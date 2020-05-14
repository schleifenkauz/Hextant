/**
 * @author Nikolaus Knop
 */

package hextant.test

import hextant.Context
import hextant.HextantPlatform

inline fun testingContext(block: Context.() -> Unit = {}): Context = HextantPlatform.rootContext().apply(block)