package hextant.lisp

import hextant.lisp.editor.SExprEditor
import hextant.lisp.rt.*
import hextant.plugin.*
import reaktive.value.now
import validated.force
import validated.isValid

object Lisp : PluginInitializer({
    stylesheet("hextant/lisp/style.css")
    registerCommand<SExprEditor<*>, String> {
        name = "Evaluate"
        shortName = "eval"
        applicableIf { p -> p.result.now.isValid }
        executing { p, _ ->
            val expr = p.result.now.force()
            val value = expr.evaluate(Env.root())
            display(value)
        }
    }
})