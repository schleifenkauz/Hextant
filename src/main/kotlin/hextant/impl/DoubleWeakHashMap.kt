/**
 *@author Nikolaus Knop
 */

package hextant.impl

import java.lang.ref.WeakReference
import java.util.*

internal class DoubleWeakHashMap<K, V> : WeakValuesMap<K, V>(WeakHashMap<K, WeakReference<V>>())