/**
 *@author Nikolaus Knop
 */

package hextant.settings.editors

import hextant.*
import hextant.base.CompoundEditor
import hextant.core.Internal
import hextant.settings.model.*
import kserial.Input
import kserial.Output
import validated.reaktive.ReactiveValidated
import validated.reaktive.mapValidated

internal class SettingsEntryEditor private constructor(context: Context) : CompoundEditor<SettingsEntry>(context) {
    constructor(context: Context, property: ConfigurableProperty) : this(context) {
        init(property)
    }

    lateinit var property: ConfigurableProperty
        private set

    lateinit var value: BidirectionalEditor<*>
        private set

    private fun init(prop: ConfigurableProperty) {
        property = prop
        value = context.createEditor(prop.type) as? BidirectionalEditor
            ?: throw RuntimeException("Editors for property value must be bidirectional")
        addChild(value)
        result = value.result.mapValidated { v -> SettingsEntry(property.property, v) }
    }

    override fun serialize(output: Output) {
        output.writeString(property.property.name!!)
        output.writeObject(value)
    }

    override fun deserialize(input: Input) {
        val name = input.readString()
        val properties = this.context[Internal, ConfigurableProperties]
        val prop = properties.byName(name) ?: error("No property configurable with name '$name'")
        init(prop)
    }

    override lateinit var result: ReactiveValidated<SettingsEntry>
        private set
}