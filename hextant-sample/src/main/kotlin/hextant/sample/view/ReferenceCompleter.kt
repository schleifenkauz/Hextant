package hextant.sample.view

import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.core.Editor
import hextant.sample.editor.Scope
import hextant.sample.editor.Scope.Def
import hextant.sample.editor.line
import reaktive.value.now

object ReferenceCompleter : ConfiguredCompleter<Editor<*>, Def>(CompletionStrategy.simple) {
    override fun completionPool(context: Editor<*>): Collection<Def> =
        context.context[Scope].availableBindings(context.line.now)

    override fun extractText(context: Editor<*>, item: Def): String? = item.name.toString()

    override fun Builder<Def>.configure(context: Editor<*>) {
        infoText = completion.type.toString()
    }
}