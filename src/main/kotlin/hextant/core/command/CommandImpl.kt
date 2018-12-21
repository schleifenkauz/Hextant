/**
 *@author Nikolaus Knop
 */

package hextant.core.command

import hextant.core.command.Command.Category
import hextant.core.command.Command.Parameter
import kotlin.reflect.KClass

internal class CommandImpl<R : Any, T>(
    override val name: String,
    override val category: Category?,
    override val shortName: String?,
    override val parameters: List<Parameter>,
    override val description: String,
    private val execute: (R, Array<out Any?>) -> T,
    private val applicable: (R) -> Boolean,
    receiverCls: KClass<R>
) : AbstractCommand<R, T>(
    receiverCls
) {
    override fun doExecute(receiver: R, vararg args: Any?): T = execute.invoke(receiver, args)

    @Suppress("UNCHECKED_CAST") override fun isApplicableOn(receiver: Any) =
            super.isApplicableOn(receiver) && applicable(receiver as R)
}