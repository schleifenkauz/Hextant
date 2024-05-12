/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.blocky.Program
import hextant.blocky.view.ProgramEditorView
import hextant.context.Context
import hextant.core.Editor
import hextant.core.editor.AbstractEditor
import hextant.serial.Snapshot
import hextant.serial.reconstructEditor
import hextant.serial.snapshot
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.jsonArray
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

class ProgramEditor(context: Context) :
    AbstractEditor<Program, ProgramEditorView>(context) {
    private val entry = EntryEditor(context)

    private val components = mutableListOf<Editor<*>>()

    init {
        addComponent(entry)
    }

    fun addComponent(comp: Editor<*>) {
        components.add(comp)
        views {
            addedComponent(components.size - 1, comp)
        }
    }

    fun removeComponent(comp: Editor<*>) {
        val idx = components.indexOf(comp)
        if (idx == -1) error("Component $comp not present")
        components.removeAt(idx)
        views {
            removedComponent(comp)
        }
    }

    override fun viewAdded(view: ProgramEditorView) {
        components.forEachIndexed { idx, comp -> view.addedComponent(idx, comp) }
    }

    override val result: ReactiveValue<Program> = entry.result.map { Program(it!!) }

    override fun createSnapshot(): Snapshot<*> = Snap()

    private class Snap : Snapshot<ProgramEditor>() {
        private lateinit var entry: Snapshot<EntryEditor>
        private lateinit var components: List<Snapshot<Editor<*>>>

        override fun doRecord(original: ProgramEditor) {
            entry = original.entry.snapshot()
            components = original.components.map { it.snapshot(recordClass = true) }
        }

        @Suppress("UNCHECKED_CAST")
        override fun reconstruct(original: ProgramEditor) {
            entry.reconstruct(original.entry)
            original.components.addAll(components.map { it.reconstructEditor(original.context) })
        }

        override fun JsonObjectBuilder.encode() {
            put("entry", entry.encodeToJson())
            put("components", JsonArray(components.map { it.encodeToJson() }))
        }

        override fun decode(element: JsonObject) {
            entry = decodeFromJson<EntryEditor>(element.getValue("entry"))
            components = element.getValue("components").jsonArray.map { decodeFromJson<Editor<*>>(it) }
        }
    }
}