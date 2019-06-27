/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import reaktive.value.ReactiveBoolean
import reaktive.value.binding.map
import java.lang.ref.WeakReference

/**
 * Builder for [Inspection]s
 */
class InspectionBuilder<out T> @PublishedApi internal constructor(inspected: T) {
    private val weakInspected = WeakReference(inspected)

    /**
     * Returns the inspected element
     */
    val inspected get() = weakInspected.get() ?: error("Inspected object collected before inspection builder")

    /**
     * The description of the [Inspection], must be set
     */
    lateinit var description: String
    private lateinit var isProblem: ReactiveBoolean
    private lateinit var messageProducer: () -> String
    private lateinit var severity: Severity
    private lateinit var location: Any
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

    fun location(loc: Any) {
        location = loc
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

    @PublishedApi internal fun build(): Inspection {
        val fixes = { fixes.filter { it.isApplicable() } }
        return InspectionImpl(isProblem, description, messageProducer, severity, fixes, location)
    }
}