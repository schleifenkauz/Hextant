package hextant.sample.view

import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.core.Editor
import hextant.sample.editor.GlobalFunction
import hextant.sample.editor.GlobalScope

object FunctionCallCompleter : ConfiguredCompleter<Editor<*>, GlobalFunction>(CompletionStrategy.simple) {
    override fun completionPool(context: Editor<*>): Collection<GlobalFunction> =
        context.context[GlobalScope].definitions

    override fun extractText(context: Editor<*>, item: GlobalFunction): String? = item.name.toString()

    override fun Builder<GlobalFunction>.configure(context: Editor<*>) {
        infoText = completion.toString()
    }
}