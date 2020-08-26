/**
 * @author Nikolaus Knop
 */

package hextant.test

import hextant.context.Context
import hextant.main.HextantPlatform

inline fun testingContext(block: Context.() -> Unit = {}): Context =
    HextantPlatform.defaultContext(HextantPlatform.projectContext(Context.newInstance())).apply(block)