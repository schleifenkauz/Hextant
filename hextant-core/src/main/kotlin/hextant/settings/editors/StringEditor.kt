/**
 *@author Nikolaus Knop
 */

package hextant.settings.editors

import hextant.*
import hextant.core.editor.BidirectionalTokenEditor

/**
 * A simple editor for string values.
 */
class StringEditor(context: Context, text: String) : BidirectionalTokenEditor<String>(context, text) {
    constructor(context: Context) : this(context, "")

    override fun compile(token: String): CompileResult<String> = ok(token)
}