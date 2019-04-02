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

inline fun <reified T : Any> myLogger(): ReadOnlyProperty<T, Logger> {
    val cls = T::class
    val qualifiedName = cls.qualifiedName ?: throw IllegalStateException("myLogger invoked from anonymous class")
    val ownerClsName = when {
        cls.isCompanion -> {
            qualifiedName.removeSuffix(".Companion")
        }
        else            -> qualifiedName
    }
    val logger = Logger.getLogger(ownerClsName)!!
    return object : ReadOnlyProperty<T, Logger> {
        override fun getValue(thisRef: T, property: KProperty<*>): Logger = logger
    }
}