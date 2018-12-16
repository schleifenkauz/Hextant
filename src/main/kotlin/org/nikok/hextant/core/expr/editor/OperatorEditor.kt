/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.editor.TokenEditor
import org.nikok.hextant.core.expr.editable.EditableOperator
import org.nikok.hextant.core.expr.view.TextEditorView

class OperatorEditor(
    editable: EditableOperator,
    platform: HextantPlatform
) : TokenEditor<EditableOperator, TextEditorView>(editable, platform)