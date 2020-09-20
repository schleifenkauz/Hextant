package hextant.sample.view

import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.sample.editor.GlobalFunction
import hextant.sample.editor.GlobalScope

object FunctionCallCompleter : ConfiguredCompleter<Context, GlobalFunction>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<GlobalFunction> = context[GlobalScope].definitions

    override fun extractText(context: Context, item: GlobalFunction): String? = item.name.toString()

    override fun Builder<GlobalFunction>.configure(context: Context) {
        infoText = completion.toString()
    }
}