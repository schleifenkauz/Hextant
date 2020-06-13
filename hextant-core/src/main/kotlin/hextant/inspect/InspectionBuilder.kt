/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.command.Command
import reaktive.value.ReactiveBoolean
import reaktive.value.binding.map

/**
 * Builder for [Inspection]s
 */
class InspectionBuilder<T : Any> @PublishedApi internal constructor() {
    /**
     * The description of the [Inspection], must be set
     */
    lateinit var description: String
    private lateinit var isProblem: InspectionBody<T>.() -> ReactiveBoolean
    private lateinit var messageProducer: InspectionBody<T>.() -> String
    private lateinit var severity: Severity
    private var location: InspectionBody<T>.() -> Any = { inspected }
    private var fixes: MutableCollection<ProblemFix<T>> = mutableSetOf()

    /**
     * The built inspection will report a problem if [forbidden] is `true`
     */
    fun preventingThat(forbidden: InspectionBody<T>.() -> ReactiveBoolean) {
        isProblem = forbidden
    }

    /**
     * The built inspection will report a problem if [isOk] is `false`
     */
    fun checkingThat(isOk: InspectionBody<T>.() -> ReactiveBoolean) {
        isProblem = { isOk().map(Boolean::not) }
    }

    /**
     * The built inspection will produce messages with [producer]
     */
    fun message(producer: InspectionBody<T>.() -> String) {
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
    fun location(loc: InspectionBody<T>.() -> Any) {
        location = loc
    }

    /**
     * Add a possible problem-[fix] to the problems reported by the built [Inspection]
     */
    fun addFix(fix: ProblemFix<T>) {
        fixes.add(fix)
    }

    /**
     * Builds a [ProblemFix] with [block] and adds it with [addFix]
     */
    inline fun addFix(block: ProblemFixBuilder<T>.() -> Unit) {
        addFix(problemFix(block))
    }

    /**
     * Adds a [CommandProblemFix]
     */
    fun addFix(
        description: String,
        command: Command<*, *>,
        target: InspectionBody<T>.() -> Any = { inspected },
        vararg arguments: Any?
    ) {
        addFix(CommandProblemFix(description, command, arguments.asList(), target))
    }

    @PublishedApi internal fun build(): Inspection<T> {
        val fixes: InspectionBody<T>.() -> Collection<ProblemFix<T>> = { fixes.filter { it.run { isApplicable() } } }
        return InspectionImpl(isProblem, description, messageProducer, severity, fixes, location)
    }
}