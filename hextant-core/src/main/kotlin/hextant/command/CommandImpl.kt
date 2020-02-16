/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.command.Command.*
import kotlin.reflect.KClass

internal class CommandImpl<R : Any, T>(
    override val name: String,
    override val category: Category?,
    override val shortName: String?,
    override val parameters: List<Parameter>,
    override val description: String,
    override val commandType: Type,
    private val execute: (R, List<Any?>) -> T,
    private val applicable: (R) -> Boolean,
    receiverCls: KClass<R>
) : AbstractCommand<R, T>(
    receiverCls
) {
    override fun doExecute(receiver: R, args: List<Any?>): T = execute.invoke(receiver, args)

    @Suppress("UNCHECKED_CAST") override fun isApplicableOn(receiver: Any) =
            super.isApplicableOn(receiver) && applicable(receiver as R)
}