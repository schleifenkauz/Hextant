/**
 *@author Nikolaus Knop
 */

package hackus.editable

import hackus.ast.JIdent
import hextant.core.editable.SimpleEditableToken

class EditableIdent : SimpleEditableToken<JIdent>(JIdent)