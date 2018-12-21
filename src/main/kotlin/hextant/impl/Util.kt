/**
 * @author Nikolaus Knop
 */

package hextant.impl

import java.util.logging.Logger
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

internal operator fun <T> KMutableProperty<T>.setValue(receiver: Any?, prop: KProperty<*>, value: T) =
    setter.call(value)

internal operator fun <T> KProperty<T>.getValue(receiver: Any?, prop: KProperty<*>): T = getter.call()

internal inline fun <reified T : Any> myLogger(): ReadOnlyProperty<T, Logger> {
    val cls = T::class
    return when {
        cls.isCompanion            -> {
            val name = cls.qualifiedName!!
            val outerName = name.removeSuffix(".Companion")
            val logger = Logger.getLogger(outerName)!!
            object : ReadOnlyProperty<T, Logger> {
                override fun getValue(thisRef: T, property: KProperty<*>): Logger = logger
            }
        }
        cls.objectInstance != null -> {
            val logger = Logger.getLogger(cls.qualifiedName!!)
            object : ReadOnlyProperty<T, Logger> {
                override fun getValue(thisRef: T, property: KProperty<*>): Logger = logger
            }
        }
        else                       -> throw IllegalArgumentException("Logger delegate must be invoked from an object")
    }
}