/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.core.list.EditableList
import hextant.lisp.SExpr

class EditableSExprList : EditableList<SExpr, EditableSExpr<*>>()