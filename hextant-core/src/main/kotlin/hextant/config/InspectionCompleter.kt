/**
 *@author Nikolaus Knop
 */

package hextant.config

import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.inspect.Inspection
import hextant.inspect.Inspections

internal object InspectionCompleter :
    ConfiguredCompleter<Context, Inspection<*>>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<Inspection<*>> = context[Inspections].all()

    override fun extractText(context: Context, item: Inspection<*>): String? = item.id

    override fun Builder<Inspection<*>>.configure(context: Context) {
        infoText = "inspection"
    }
}