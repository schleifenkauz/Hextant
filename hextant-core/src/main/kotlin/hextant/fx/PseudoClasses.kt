package hextant.fx

import javafx.css.PseudoClass

/**
 * Common pseudo classes
 */
object PseudoClasses {
    /**
     * Used when a Node should display an error
     */
    val ERROR: PseudoClass = PseudoClass.getPseudoClass("error")

    /**
     * Used when a Node should be selected
     */
    val SELECTED: PseudoClass = PseudoClass.getPseudoClass("selected")

    /**
     * Indicates a warning
     */
    val WARN: PseudoClass = PseudoClass.getPseudoClass("warning")!!
}