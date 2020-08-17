/**
 *@author Nikolaus Knop
 */

package hextant.main.editors

import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.main.HextantApp
import java.io.File

object ProjectLocationCompleter : ConfiguredCompleter<Context, File>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<File> = context[HextantApp.recentProjects]

    override fun extractText(context: Context, item: File): String? = item.nameWithoutExtension

    override fun Builder<File>.configure(context: Context) {
        infoText = completion.absolutePath
    }
}