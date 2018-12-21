package hextant.core.inspect

/**
 * A fix for a problem used repair the problem
*/
interface ProblemFix {
    /**
     * @return the description of this [ProblemFix]
    */
    val description: String
    /**
     * @return `true` if and only if this [ProblemFix] can be used in the current context
    */
    fun isApplicable(): Boolean
    /**
     * Actually fix the problem
    */
    fun fix()
}
