/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property

enum class InputMethod {
    VIM, REGULAR;

    companion object : Property<InputMethod, Public, Public>("input method", default = REGULAR)
}