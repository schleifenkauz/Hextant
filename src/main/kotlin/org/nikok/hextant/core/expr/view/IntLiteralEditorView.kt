/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import org.nikok.hextant.EditorView

interface IntLiteralEditorView: EditorView {
    fun textChanged(new: String)
}