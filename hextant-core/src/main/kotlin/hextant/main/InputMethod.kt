/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property

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

    companion object : Property<InputMethod, Public, Public>("input method", default = REGULAR)
}