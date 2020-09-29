/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.completion.CompletionStrategy
import hextant.completion.CompoundCompleter
import hextant.core.Editor
import hextant.sample.editor.ExprExpanderDelegator

object ExprCompleter : CompoundCompleter<Editor<*>, Any>() {
    init {
        addCompleter(ReferenceCompleter)
        addCompleter(FunctionCallCompleter)
        addCompleter(ExprExpanderDelegator.config.completer(CompletionStrategy.simple))
    }
}