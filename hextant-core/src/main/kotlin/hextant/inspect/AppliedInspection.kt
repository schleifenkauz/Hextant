package hextant.inspect

internal data class AppliedInspection<T : Any>(val body: InspectionBody<T>, val inspection: Inspection<T>) {
    constructor(target: T, inspection: Inspection<T>) : this(InspectionBody.soft(target), inspection)

    fun getProblem() = inspection.run { body.getProblem() }

    fun isProblem() = inspection.run { body.isProblem() }

    val isEnabled get() = inspection.isEnabled

    val severity get() = inspection.severity
}