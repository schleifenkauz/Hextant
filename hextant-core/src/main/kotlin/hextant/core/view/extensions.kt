/**
 * @author Nikolaus Knop
 */

package hextant.core.view

import hextant.core.EditorView

/**
 * Typesafe version of [EditorView.createSnapshot]
 */
@Suppress("UNCHECKED_CAST")
fun <V : EditorView> V.snapshot(): ViewSnapshot<V> = createSnapshot() as ViewSnapshot<V>