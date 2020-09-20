package hextant.sample.view

import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.sample.editor.Scope
import hextant.sample.editor.Scope.Binding

object ReferenceCompleter : ConfiguredCompleter<Context, Binding>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<Binding> = context[Scope].availableBindings()

    override fun extractText(context: Context, item: Binding): String? = item.name.toString()

    override fun Builder<Binding>.configure(context: Context) {
        infoText = completion.type.toString()
    }
}