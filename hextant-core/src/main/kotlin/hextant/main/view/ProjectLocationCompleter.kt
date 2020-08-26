/**
 *@author Nikolaus Knop
 */

package hextant.main.view

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.main.GlobalDirectory
import hextant.main.GlobalDirectory.Companion.PROJECTS

internal object ProjectLocationCompleter : ConfiguredCompleter<Context, String>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<String> =
        context[GlobalDirectory][PROJECTS].listFiles()!!.filter { d ->
            d.isDirectory && d.resolve("project.hxt").exists()
        }.map { it.name }

}