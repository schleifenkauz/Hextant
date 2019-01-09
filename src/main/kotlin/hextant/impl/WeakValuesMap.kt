/**
 *@author Nikolaus Knop
 */

package hextant.impl

import java.lang.ref.WeakReference
import kotlin.collections.MutableMap.MutableEntry

internal open class WeakValuesMap<K, V>(private val wrapped: MutableMap<K, WeakReference<V>> = HashMap()) :
    AbstractMutableMap<K, V>() {

    override val size: Int
        get() = wrapped.size

    override fun containsKey(key: K): Boolean {
        return wrapped.containsKey(key)
    }

    override fun containsValue(value: V): Boolean {
        return wrapped.containsValue(WeakReference(value))
    }

    override fun get(key: K): V? {
        val weak = wrapped[key] ?: return null
        if (weak.get() == null) {
            wrapped.remove(key)
        }
        return weak.get()
    }

    override fun isEmpty(): Boolean {
        return wrapped.isEmpty()
    }

    override val entries: MutableSet<MutableEntry<K, V>> get() = throw UnsupportedOperationException()

    override fun clear() {
        wrapped.clear()
    }

    override fun put(key: K, value: V): V? {
        return wrapped.put(key, WeakReference(value))?.get()
    }

    override fun putAll(from: Map<out K, V>) {
        wrapped.putAll(from.entries.associate { (k, v) -> k to WeakReference(v) })
    }

    override fun remove(key: K): V? {
        return wrapped.remove(key)?.get()
    }

}