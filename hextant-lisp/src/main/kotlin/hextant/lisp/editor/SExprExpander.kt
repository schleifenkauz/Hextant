package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.Expander
import hextant.core.editor.ExpanderConfig
import hextant.lisp.SExpr
import hextant.lisp.Scalar
import hextant.serial.Snapshot
import hextant.undo.PropertyEdit
import hextant.undo.UndoManager
import javafx.css.PseudoClass
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean

class SExprExpander(context: Context, editor: SExprEditor<*>? = null) :
    ConfiguredExpander<SExpr, SExprEditor<*>>(config, Scalar, context, editor), SExprEditor<SExpr> {
    var isNormalized = false
        set(value) {
            context[UndoManager].record(PropertyEdit(this::isNormalized, field, value, "normalize"))
            field = value
            notifyViews {
                changePseudoClassState(PseudoClass.getPseudoClass("normalized"), value)
            }
        }

    override fun createSnapshot(): Snapshot<*> = Snap()

    private class Snap : Snapshot<SExprExpander>() {
        private val wrapped: Snapshot<Expander<*, *>> = Expander.Snap()
        private var isNormalized = false

        override fun doRecord(original: SExprExpander) {
            wrapped.record(original, false)
            isNormalized = original.isNormalized
        }

        override fun reconstructObject(original: SExprExpander) {
            wrapped.reconstructObject(original)
            original.isNormalized = isNormalized
        }

        override fun encode(builder: JsonObjectBuilder) {
            with(wrapped) { encode(builder) }
            builder.put("isNormalized", JsonPrimitive(this.isNormalized))
        }

        override fun decode(element: JsonObject) {
            wrapped.decode(element)
            isNormalized = (element["isNormalized"] as JsonPrimitive).boolean
        }
    }

    companion object {
        val config = ExpanderConfig<SExprEditor<*>>().apply {
            registerKey("'", create = ::QuotationEditor)
            registerKey("`", create = ::QuasiQuotationEditor)
            registerKey("," , create = ::UnquoteEditor)
            registerKey("(" , create = ::CallExprEditor)
            registerKey("[", create = ::MacroInvocationEditor)
        }
    }
}