/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.AbstractEditor
import hextant.lisp.rt.RuntimeScope
import reaktive.value.reactiveValue
import validated.reaktive.ReactiveValidated
import validated.valid

class RuntimeScopeEditor(context: Context, var scope: RuntimeScope) :
    AbstractEditor<RuntimeScope, RuntimeScopeView>(context) {

    override val result: ReactiveValidated<RuntimeScope>
        get() = reactiveValue(valid(scope))
}