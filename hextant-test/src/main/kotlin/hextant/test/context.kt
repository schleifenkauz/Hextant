/**
 * @author Nikolaus Knop
 */

package hextant.test

import hextant.context.Context
import hextant.context.Properties.defaultContext
import hextant.main.HextantPlatform
import hextant.main.initializePluginsFromClasspath

private val root = HextantPlatform.projectContext().also { ctx -> initializePluginsFromClasspath(ctx) }

fun testingContext(block: Context.() -> Unit = {}): Context = defaultContext(root).apply(block)