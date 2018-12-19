/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editor

import org.nikok.hextant.Context
import org.nikok.hextant.EditorView
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.sample.editable.EditableIntOperatorApplication

class IntOperatorApplicationEditor(
    editable: EditableIntOperatorApplication,
    context: Context
) : AbstractEditor<EditableIntOperatorApplication, EditorView>(editable, context)