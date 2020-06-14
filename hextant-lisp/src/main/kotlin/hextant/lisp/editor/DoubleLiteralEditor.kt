/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.lisp.DoubleLiteral
import validated.*

class DoubleLiteralEditor(context: Context) : TokenEditor<DoubleLiteral>(context),
                                              SExprEditor<DoubleLiteral> {
    constructor(value: Double, context: Context) : this(context) {
        setText(value.toString())
    }

    constructor(value: DoubleLiteral, context: Context) : this(value.value, context)

    override fun compile(token: String): Validated<DoubleLiteral> =
        token.toDoubleOrNull().validated { invalid("Invalid double literal $token") }.map(::DoubleLiteral)
}