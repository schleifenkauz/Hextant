package hextant.lisp

import hextant.lisp.debug.reconstruct
import hextant.lisp.editor.SExprExpander
import hextant.lisp.rt.evaluate
import hextant.lisp.rt.reduce
import hextant.plugin.*
import reaktive.value.now
import validated.force
import validated.isValid

object Lisp : PluginInitializer({
    stylesheet("hextant/lisp/style.css")
    registerCommand<SExprExpander, Unit> {
        name = "Evaluate"
        shortName = "eval"
        defaultShortcut("Ctrl?+Shift+E")
        applicableIf { p -> p.result.now.isValid }
        executing { e, _ ->
            val expr = e.result.now.force()
            val v = expr.evaluate()
            e.reconstruct(v)
        }
    }
    registerCommand<SExprExpander, Unit> {
        name = "Reduce"
        shortName = "reduce"
        applicableIf { p -> p.result.now.isValid }
        defaultShortcut("Ctrl?+E")
        executing { e, _ ->
            val expr = e.result.now.force()
            val (reduced, scope) = expr.reduce(e.scope)
            e.scope = scope
            e.reconstruct(reduced)
        }
    }
})