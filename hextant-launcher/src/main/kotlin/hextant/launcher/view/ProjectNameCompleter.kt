/**
 *@author Nikolaus Knop
 */

package hextant.launcher.view

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.core.Editor
import hextant.cli.HextantDirectory
import hextant.cli.HextantDirectory.PROJECTS

internal object ProjectNameCompleter : ConfiguredCompleter<Editor<*>, String>(CompletionStrategy.simple) {
    override fun completionPool(context: Editor<*>): Collection<String> =
        HextantDirectory[PROJECTS].listFiles()!!
            .filter { it.isDirectory }
            .filter { d -> d.resolve(HextantDirectory.PROJECT_INFO).exists() && !HextantDirectory.isLocked(d) }
            .map { it.name }

}