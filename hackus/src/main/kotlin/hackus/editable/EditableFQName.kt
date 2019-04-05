/**
 *@author Nikolaus Knop
 */

package hackus.editable

import hackus.ast.FQName
import hextant.core.editable.SimpleEditableToken

class EditableFQName: SimpleEditableToken<FQName>(FQName)