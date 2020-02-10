/**
 * @author Nikolaus Knop
 */

package hextant.impl

import javafx.beans.value.ObservableValue
import org.nikok.kref.weak
import reaktive.Observer
import reaktive.event.EventStream
import reaktive.event.Subscription
import reaktive.value.ReactiveValue
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

internal fun <T, R : Any> ReactiveValue<T>.observe(
    referent: R,
    handler: R.(changed: ReactiveValue<T>, old: T, new: T) -> Unit
): Observer {
    val ref = weak(referent)
    return observe { changed: ReactiveValue<T>, old: T, new: T ->
        ref.referent?.handler(changed, old, new)
    }
}

internal fun <T, R : Any> EventStream<T>.subscribe(
    referent: R,
    handler: R.(stream: EventStream<T>, value: T) -> Unit
): Subscription {
    val ref = weak(referent)
    return subscribe { stream: EventStream<T>, value: T ->
        ref.referent?.handler(stream, value)
    }
}

internal fun <R : Any, T : Any?> ObservableValue<T>.addListener(receiver: R, listener: R.(T) -> Unit) {
    val ref = weak(receiver)
    addListener { _, _, newValue -> ref.referent?.listener(newValue) }
}

internal fun <T : Any> iterate(start: T?, next: (T) -> T?): T? {
    var current = start ?: return null
    var nxt = next(current)
    while (nxt != null) {
        current = nxt
        nxt = next(current)
    }
    return current
}

@JvmName("iterateNonNullStart")
internal fun <T : Any> iterate(start: T, next: (T) -> T?): T {
    var current = start
    var nxt = next(current)
    while (nxt != null) {
        current = nxt
        nxt = next(current)
    }
    return current
}