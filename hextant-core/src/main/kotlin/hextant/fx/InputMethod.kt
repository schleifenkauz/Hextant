package hextant.fx

import bundles.PublicProperty
import bundles.property

/**
 * Specifies the way the user interacts with text fields
 */
enum class InputMethod {
    /**
     * The VIM input method enables editing modes that are local to each text field.
     */
    VIM,

    /**
     * The REGULAR input method just makes every text field editable.
     */
    REGULAR;

    companion object : PublicProperty<InputMethod> by property("input method", default = REGULAR)
}