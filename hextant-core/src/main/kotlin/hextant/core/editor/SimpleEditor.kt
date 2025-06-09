package hextant.core.editor

import fxutils.undo.UndoManager
import fxutils.undo.VariableEdit
import hextant.core.Editor
import hextant.serial.JsonSerializer
import kotlinx.serialization.json.JsonElement
import reaktive.value.ReactiveValue
import reaktive.value.ReactiveVariable
import reaktive.value.now
import reaktive.value.reactiveVariable

abstract class SimpleEditor<R : Any> : AbstractEditor<R, SimpleEditor.View<R>>(), JsonSerializer<R> {
    private lateinit var _result: ReactiveVariable<R>

    override val result: ReactiveValue<R> get() = _result

    var customUpdateDescription: String? = null

    fun setInitialResult(value: R) {
        _result = reactiveVariable(value)
    }

    fun setResult(result: R) {
        if (this.result.now == result) return
        val oldResult = _result.now
        _result.set(result)
        val updateDescription = customUpdateDescription ?: "Update $accessor"
        context[UndoManager].record(VariableEdit(_result, oldResult, updateDescription))
        notifyViews { displayResult(result) }
    }

    override fun paste(editor: Editor<*>): Boolean {
        TODO("Not yet implemented")
    }

    override fun viewAdded(view: View<R>) {
        view.displayResult(result.now)
    }

    override fun toString(): String = "${javaClass.name}[${result.now}]"

    interface View<R : Any> {
        fun displayResult(result: R)
    }

    override fun serialize(): JsonElement = toJson(result.now)

    override fun deserialize(element: JsonElement) {
        setInitialResult(fromJson(element))
    }
}