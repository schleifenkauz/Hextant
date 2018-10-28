/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.impl

import java.util.logging.Logger
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

internal operator fun <T> KMutableProperty<T>.setValue(receiver: Any?, prop: KProperty<*>, value: T) =
        setter.call(value)

internal operator fun <T> KProperty<T>.getValue(receiver: Any?, prop: KProperty<*>): T = getter.call()

internal inline fun <reified T : Any> myLogger(): ReadOnlyProperty<T, Logger> {
    val cls = T::class
    require(cls.isCompanion) { "Logger delegate must be invoked from companion object" }
    val name = cls.qualifiedName!!
    val outerName = name.removeSuffix(".Companion")
    val logger = Logger.getLogger(outerName)!!
    return object : ReadOnlyProperty<T, Logger> {
        override fun getValue(thisRef: T, property: KProperty<*>): Logger = logger
    }
}