/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.core.editable.Expandable
import hextant.lisp.SExpr

class ExpandableSExpr : Expandable<SExpr, EditableSExpr<*>>(), EditableSExpr<SExpr>