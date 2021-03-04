/**
 *@author Nikolaus Knop
 */

package hextant.launcher.view

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.core.Editor
import hextant.serial.Files
import hextant.serial.Files.Companion.PROJECTS

internal object ProjectNameCompleter : ConfiguredCompleter<Editor<*>, String>(CompletionStrategy.simple) {
    override fun completionPool(context: Editor<*>): Collection<String> =
        context.context[Files][PROJECTS].listFiles()!!
            .filter { it.isDirectory }
            .filter { d -> d.resolve(Files.PROJECT_INFO).exists() && !context.context[Files].isLocked(d) }
            .map { it.name }

}