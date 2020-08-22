/**
 *@author Nikolaus Knop
 */

package hextant.main.view

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.main.HextantApp

internal object ProjectLocationCompleter : ConfiguredCompleter<Context, String>(CompletionStrategy.simple) {
    private val pool = HextantApp.projects.listFiles()!!.filter { d ->
        d.isDirectory && d.resolve("project.hxt").exists()
    }.map { it.name }

    override fun completionPool(context: Context): Collection<String> = pool
}