/**
 * @author Nikolaus Knop
 */

package hextant.test

import hextant.context.Context
import hextant.context.Properties.defaultContext
import hextant.context.Properties.projectContext
import hextant.plugins.initializePluginsFromClasspath

private val root = projectContext(Context.newInstance(), HextantTestApplication::class.java.classLoader)
    .also { ctx -> initializePluginsFromClasspath(ctx, testing = true) }

fun testingContext(block: Context.() -> Unit = {}): Context = defaultContext(root).apply(block)

