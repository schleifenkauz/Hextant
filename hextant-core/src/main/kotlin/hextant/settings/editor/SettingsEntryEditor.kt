/**
 *@author Nikolaus Knop
 */

package hextant.settings.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.context.createEditor
import hextant.core.editor.*
import hextant.serial.PropertyAccessor
import hextant.settings.model.ConfigurableProperty
import hextant.settings.model.SettingsEntry
import reaktive.Observer
import reaktive.value.binding.map
import reaktive.value.reactiveVariable
import validated.invalidComponent
import validated.or
import validated.reaktive.ReactiveValidated
import validated.reaktive.mapValidated

@ProvideFeature
internal class SettingsEntryEditor private constructor(context: Context) : CompoundEditor<SettingsEntry>(context) {
    constructor(context: Context, property: ConfigurableProperty) : this(context) {
        init(property)
    }

    private var initialized = false
    private lateinit var observer: Observer

    lateinit var property: ConfigurableProperty
        private set

    lateinit var value: BidirectionalEditor<*>
        private set

    private fun init(prop: ConfigurableProperty) {
        property = prop
        val e = context.createEditor<Any>(prop.type) as? BidirectionalEditor
            ?: throw RuntimeException("Editors for property value must be bidirectional")
        setValueEditor(e)
    }

    private fun setValueEditor(editor: BidirectionalEditor<*>) {
        value = editor
        if (initialized) {
            removeChild(value)
            observer.kill()
        }
        addChild(value)
        @Suppress("DEPRECATION")
        value.initAccessor(PropertyAccessor("value"))
        val res = value.result
            .mapValidated { v -> SettingsEntry(property.property, v) }
            .map { it.or(invalidComponent) }
        observer = _result.bind(res)
        initialized = true
    }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    private class Snapshot(original: SettingsEntryEditor) : EditorSnapshot<SettingsEntryEditor>(original) {
        private val property = original.property
        private val value = original.value.snapshot()

        override fun reconstruct(editor: SettingsEntryEditor) {
            editor.property = property
            editor.setValueEditor(value.reconstruct(editor.context))
        }
    }

    private val _result = reactiveVariable(invalidComponent<SettingsEntry>())

    override val result: ReactiveValidated<SettingsEntry> get() = _result
}