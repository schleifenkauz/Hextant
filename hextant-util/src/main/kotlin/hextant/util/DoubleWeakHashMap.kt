/**
 *@author Nikolaus Knop
 */

package hextant.util

import java.lang.ref.WeakReference
import java.util.*

class DoubleWeakHashMap<K, V> : WeakValuesMap<K, V>(WeakHashMap<K, WeakReference<V>>())