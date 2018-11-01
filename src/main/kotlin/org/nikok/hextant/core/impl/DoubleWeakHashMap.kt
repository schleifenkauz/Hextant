/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.impl

import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.MutableMap.MutableEntry

internal class DoubleWeakHashMap<K, V> : AbstractMutableMap<K, V>() {
    private val singleWeak = WeakHashMap<K, WeakReference<V>>()

    override val size: Int
        get() = singleWeak.size

    override fun containsKey(key: K): Boolean {
        return singleWeak.containsKey(key)
    }

    override fun containsValue(value: V): Boolean {
        return singleWeak.containsValue(WeakReference(value))
    }

    override fun get(key: K): V? {
        val weak = singleWeak[key] ?: return null
        if (weak.get() == null) {
            singleWeak.remove(key)
        }
        return weak.get()
    }

    override fun isEmpty(): Boolean {
        return singleWeak.isEmpty()
    }

    override val entries: MutableSet<MutableEntry<K, V>> get() = throw UnsupportedOperationException()

    override fun clear() {
        singleWeak.clear()
    }

    override fun put(key: K, value: V): V? {
        return singleWeak.put(key, WeakReference(value))?.get()
    }

    override fun putAll(from: Map<out K, V>) {
        singleWeak.putAll(from.entries.associate { (k, v) -> k to WeakReference(v) })
    }

    override fun remove(key: K): V? {
        return singleWeak.remove(key)?.get()
    }
}