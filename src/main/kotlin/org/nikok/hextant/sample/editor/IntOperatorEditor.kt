/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.core.editor.TokenEditor
import org.nikok.hextant.core.expr.editable.EditableOperator

class IntOperatorEditor(
    editable: EditableToken<EditableOperator>,
    platform: HextantPlatform
) : TokenEditor<EditableOperator>(editable, platform)