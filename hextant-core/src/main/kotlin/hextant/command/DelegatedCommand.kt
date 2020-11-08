/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.command.Command.*
import hextant.fx.Shortcut
import kotlin.reflect.KClass

internal class DelegatedCommand<D : Any, T : Any, F : Any>(
    private val original: Command<D, T>,
    private val delegation: (F) -> D?,
    receiverCls: KClass<F>
) : AbstractCommand<F, T>(receiverCls) {
    override val shortName: String?
        get() = original.shortName
    override val name: String
        get() = original.name
    override val category: Category?
        get() = original.category
    override val shortcut: Shortcut?
        get() = original.shortcut
    override val parameters: List<Parameter<*>>
        get() = original.parameters
    override val description: String
        get() = original.description
    override val commandType: Type
        get() = original.commandType

    override fun doExecute(receiver: F, args: CommandArguments): T = original.execute(delegation(receiver)!!, args)

    override fun execute(receiver: F, arguments: List<Any>): T = original.execute(delegation(receiver)!!, arguments)

    @Suppress("UNCHECKED_CAST")
    override fun isApplicableOn(receiver: Any): Boolean {
        if (!super.isApplicableOn(receiver)) return false
        receiver as F
        val delegate = delegation(receiver) ?: return false
        return original.isApplicableOn(delegate)
    }

    override fun equals(other: Any?): Boolean = original == other

    override fun hashCode(): Int = original.hashCode()

    override fun toString(): String = original.toString()
}