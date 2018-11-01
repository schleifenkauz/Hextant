/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableOperator
import org.nikok.hextant.core.expr.view.FXOperatorEditorView

class OperatorEditor(
    editable: EditableOperator,
    platform: HextantPlatform
) : AbstractEditor<EditableOperator, FXOperatorEditorView>(editable, platform)