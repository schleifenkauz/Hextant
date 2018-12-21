/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.command.Builder
import org.nikok.reaktive.value.ReactiveBoolean

/**
 * Builder for [Inspection]s
 */
@Builder
class InspectionBuilder<T : Any> @PublishedApi internal constructor(
    private val inspected: T
) {
    /**
     * The description of the [Inspection], must be set
     */
    lateinit var description: String
    private lateinit var isProblem: ReactiveBoolean
    private lateinit var messageProducer: () -> String
    private lateinit var severity: Severity
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
        isProblem = isOk.map("not $isOk", Boolean::not)
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

    @PublishedApi internal fun build(): Inspection<T> {
        return InspectionImpl(isProblem, inspected, description, messageProducer, severity)
    }
}