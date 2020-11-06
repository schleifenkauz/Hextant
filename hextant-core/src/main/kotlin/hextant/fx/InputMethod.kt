package hextant.fx

import bundles.SimpleProperty

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

    companion object : SimpleProperty<InputMethod>("input method") {
        override val default: InputMethod
            get() = REGULAR
    }
}