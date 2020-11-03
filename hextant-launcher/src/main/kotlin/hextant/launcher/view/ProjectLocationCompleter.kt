/**
 *@author Nikolaus Knop
 */

package hextant.launcher.view

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.core.Editor
import hextant.launcher.GlobalDirectory
import hextant.launcher.GlobalDirectory.Companion.PROJECTS

internal object ProjectLocationCompleter : ConfiguredCompleter<Editor<*>, String>(CompletionStrategy.simple) {
    override fun completionPool(context: Editor<*>): Collection<String> =
        context.context[GlobalDirectory][PROJECTS].listFiles()!!.filter { d ->
            d.isDirectory && d.resolve(GlobalDirectory.PROJECT_INFO).exists()
        }.map { it.name }

}