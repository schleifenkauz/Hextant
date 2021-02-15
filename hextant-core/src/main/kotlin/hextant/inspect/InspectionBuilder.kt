/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.command.Command
import hextant.inspect.Problem.Severity
import reaktive.value.ReactiveBoolean
import reaktive.value.binding.map

/**
 * Builder for [Inspection]s
 */
class InspectionBuilder<T : Any> @PublishedApi internal constructor() {
    /**
     * The [Inspection.id] of the built inspection, must be set.
     */
    lateinit var id: String

    /**
     * The description of the [Inspection], must be set
     */
    lateinit var description: String

    /**
     * Indicates whether the built inspection should be enabled by default.
     *
     * Default is `true`.
     */
    var initiallyEnabled = true

    private var applies: InspectionBody<T>.() -> Boolean = { true }
    private lateinit var isProblem: InspectionBody<T>.() -> ReactiveBoolean
    private lateinit var messageProducer: InspectionBody<T>.() -> String
    private lateinit var severity: Severity
    private var location: InspectionBody<T>.() -> Any = { inspected }
    private var fixes: MutableCollection<ProblemFix<T>> = mutableSetOf()

    /**
     * The built inspection will be applied to a target only if [predicate] returns true on it.
    */
    fun appliesIf(predicate: InspectionBody<T>.() -> Boolean) {
        applies = predicate
    }

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
        if (isSevere) severity(Severity.Error)
        else severity(Severity.Warning)
    }

    /**
     * Set the location of the inspection.
     *
     * Default is the inspected object itself.
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
        vararg arguments: Any,
        target: InspectionBody<T>.() -> Any = { inspected }
    ) {
        addFix(CommandProblemFix(description, command, arguments.asList(), target))
    }

    @PublishedApi internal fun build(): Inspection<T> {
        val fixes: InspectionBody<T>.() -> Collection<ProblemFix<T>> = { fixes.filter { it.run { isApplicable() } } }
        return InspectionImpl(id, applies, isProblem, description, messageProducer, severity, fixes, location)
    }
}