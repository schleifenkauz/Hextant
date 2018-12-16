/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import org.nikok.hextant.core.base.AbstractEditable
import org.nikok.reaktive.value.*

class EditableText : AbstractEditable<String>() {
    val text = reactiveVariable("text of $this", "")

    override val edited: ReactiveVariable<String> get() = text

    override val isOk: ReactiveBoolean = reactiveValue("is $this ok", true)
}