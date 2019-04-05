/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.*
import hextant.Context
import hextant.Editor
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig

class RightSideExpander(editable: ExpandableRightSide, context: Context) :
    ConfiguredExpander<EditableRightSide<*>, ExpandableRightSide>(config, editable, context) {
    override fun accepts(child: Editor<*>): Boolean = child.editable is EditableRightSide<*>

    companion object {
        val config = ExpanderConfig<EditableRightSide<*>>().apply {
            registerConstant("token") { EditableTerminal() }
            registerConstant("alt") { EditableAlternative() }
            registerConstant("compound") { EditableCompound() }
        }
    }
}