/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package org.nikok.hextant.core.view.builder.gui

import javafx.scene.Node
import org.nikok.hextant.*
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.completion.*
import org.nikok.hextant.core.fx.*
import org.nikok.hextant.core.gui.SearchableListController
import org.nikok.hextant.core.view.builder.gui.impl.ClassFinder
import org.nikok.hextant.core.view.builder.gui.impl.ClassFinder.ClassInfo
import org.nikok.reaktive.event.subscribe
import kotlin.reflect.KClass

private object ClassNameCompleter : Completer<ClassInfo> {
    override fun completions(element: String, completionPool: Collection<ClassInfo>): Set<Completion<ClassInfo>> {
        return completionPool.asSequence().filter {
            val name = it.className
            CompletionStrategy.simple.isCompletable(element, name)
        }.map {
            SimpleCompletion(it, it.fullName)
        }.toSet()
    }
}

private fun showEditableClassInputPopup(
    classLoader: ClassLoader,
    parent: Node,
    use: (KClass<out Editable<Any>>) -> Unit
) {
    val finder = ClassFinder.get(classLoader)
    val source = finder.allTopLevel.filter { it.className.startsWith("Editable") }
    val controller = SearchableListController(source, ClassNameCompleter)
    val popup = SearchableListPopup(controller)
    popup.show(parent)
    controller.selectedItem.subscribe("Selected item handler") { selected ->
        val cls = selected.loadClass() as KClass<Editable<Any>>
        use(cls)
    }
}

fun showViewBuilderGui(parent: Node, classLoader: ClassLoader, platform: HextantPlatform) {
    showEditableClassInputPopup(classLoader, parent) { cls ->
        showViewBuilderGui(parent, cls, platform) { viewFactory ->
            platform[EditorViewFactory].registerFX(cls, viewFactory)
        }
    }
}

private fun showViewBuilderGui(
    parent: Node,
    cls: KClass<out Editable<Any>>,
    platform: HextantPlatform,
    use: ((Editable<Any>) -> FXEditorView) -> Unit
) {

}
