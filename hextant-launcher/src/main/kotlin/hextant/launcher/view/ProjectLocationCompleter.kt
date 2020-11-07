/**
 *@author Nikolaus Knop
 */

package hextant.launcher.view

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.core.Editor
import hextant.launcher.Files
import hextant.launcher.Files.Companion.PROJECTS
import hextant.launcher.ProjectManager

internal object ProjectLocationCompleter : ConfiguredCompleter<Editor<*>, String>(CompletionStrategy.simple) {
    override fun completionPool(context: Editor<*>): Collection<String> =
        context.context[Files][PROJECTS].listFiles()!!
            .filter { it.isDirectory }
            .filter { d -> d.resolve(Files.PROJECT_INFO).exists() && !context.context[ProjectManager].isLocked(d) }
            .map { it.name }

}