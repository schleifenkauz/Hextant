/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.view.builder

import org.nikok.hextant.Editable

interface EditorViewBuilder {
    fun keyword(name: String)

    fun operator(op: String)

    fun view(editable: Editable<*>)
}