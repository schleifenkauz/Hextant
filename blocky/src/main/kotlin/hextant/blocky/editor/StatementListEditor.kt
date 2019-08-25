/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.Context
import hextant.blocky.Statement
import hextant.core.editor.ListEditor

class StatementListEditor(context: Context) : ListEditor<Statement, StatementEditor<*>>(context) {
    override fun createEditor(): StatementEditor<*> = StatementExpander(context)
}