/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.completion.CompletionStrategy
import hextant.completion.CompoundCompleter
import hextant.context.Context
import hextant.sample.editor.ExprExpanderDelegator

object ExprCompleter : CompoundCompleter<Context, Any>() {
    init {
        addCompleter(ReferenceCompleter)
        addCompleter(FunctionCallCompleter)
        addCompleter(ExprExpanderDelegator.config.completer(CompletionStrategy.simple))
    }
}