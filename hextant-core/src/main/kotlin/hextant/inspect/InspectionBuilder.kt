/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.command.Command
import org.nikok.kref.forcedWeak
import org.nikok.kref.mutableWeak
import reaktive.value.ReactiveBoolean
import reaktive.value.binding.map

/**
 * Builder for [Inspection]s
 */
class InspectionBuilder<out T : Any> @PublishedApi internal constructor(inspected: T) {
    /**
     * Returns the inspected element
     */
    val inspected by forcedWeak(inspected)

    /**
     * The description of the [Inspection], must be set
     */
    lateinit var description: String
    private lateinit var isProblem: ReactiveBoolean
    private lateinit var messageProducer: () -> String
    private lateinit var severity: Severity
    private var location = mutableWeak(inspected as Any)
    private var fixes: MutableCollection<ProblemFix> = mutableSetOf()

    /**
     * The built inspection will report a problem if [forbidden] is `true`
     */
    fun preventingThat(forbidden: ReactiveBoolean) {
        isProblem = forbidden
    }

    /**
     * The built inspection will report a problem if [isOk] is `false`
     */
    fun checkingThat(isOk: ReactiveBoolean) {
        isProblem = isOk.map(Boolean::not)
    }

    /**
     * The built inspection will produce messages with [producer]
     */
    fun message(producer: () -> String) {
        messageProducer = producer
    }

    /**
     * The built inspection has the specified [severity]
     */
    fun severity(value: Severity) {
        severity = value
    }

    /**
     * The built inspection will be severe if [isSevere] is `true`
     */
    fun isSevere(isSevere: Boolean) {
        severity(Severity.of(isSevere))
    }

    /**
     * Set the location of the inspection
     */
    fun location(loc: Any) {
        location.referent = loc
    }

    /**
     * Add a possible problem-[fix] to the problems reported by the built [Inspection]
     */
    fun addFix(fix: ProblemFix) {
        fixes.add(fix)
    }

    /**
     * Builds a [ProblemFix] with [block] and adds it with [addFix]
     */
    inline fun addFix(block: ProblemFixBuilder.() -> Unit) {
        addFix(problemFix(block))
    }

    /**
     * Adds a [CommandProblemFix]
     */
    fun addFix(description: String, command: Command<*, *>, target: Any = inspected, vararg arguments: Any?) {
        addFix(CommandProblemFix(description, command, arguments.asList(), target))
    }

    @PublishedApi internal fun build(): Inspection {
        val fixes = { fixes.filter { it.isApplicable() } }
        return InspectionImpl(isProblem, description, messageProducer, severity, fixes, inspected, location.referent!!)
    }
}