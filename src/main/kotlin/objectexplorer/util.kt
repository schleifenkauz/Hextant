/**
 * @author Nikolaus Knop
 */

package objectexplorer

import objectexplorer.ObjectGraphMeasurer.Footprint

val Any.objectSize: Long get() = MemoryMeasurer.measureBytes(this)

val Any.footprint: Footprint get() = ObjectGraphMeasurer.measure(this)

val usedBytes get() = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

val totalBytes get() = Runtime.getRuntime().totalMemory()

val freeBytes get() = Runtime.getRuntime().freeMemory()