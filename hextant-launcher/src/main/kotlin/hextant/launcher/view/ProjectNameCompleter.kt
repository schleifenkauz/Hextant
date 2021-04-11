/**
 *@author Nikolaus Knop
 */

package hextant.launcher.view

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.core.Editor
import hextant.main.HextantDirectory
import hextant.main.HextantDirectory.Companion.PROJECTS

internal object ProjectNameCompleter : ConfiguredCompleter<Editor<*>, String>(CompletionStrategy.simple) {
    override fun completionPool(context: Editor<*>): Collection<String> =
        context.context[HextantDirectory][PROJECTS].listFiles()!!
            .filter { it.isDirectory }
            .filter { d -> d.resolve(HextantDirectory.PROJECT_INFO).exists() && !context.context[HextantDirectory].isLocked(d) }
            .map { it.name }

}