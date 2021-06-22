package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.ConfiguredTokenEditor
import hextant.lisp.Scalar
import hextant.lisp.rt.display

class ScalarEditor(context: Context, text: String) :
    ConfiguredTokenEditor<Scalar>(Scalar, context, text), SExprEditor<Scalar> {
    constructor(context: Context, node: Scalar) : this(context, display(node))
}